package com.lean.lean.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.lean.lean.dao.ExpenseInsightReport;
import com.lean.lean.dao.IncomeInsightReport;
import com.lean.lean.dao.LeanTransactionReport;
import com.lean.lean.repository.ExpenseInsightReportRepository;
import com.lean.lean.repository.IncomeInsightReportRepository;
import com.lean.lean.repository.LeanTransactionReportRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.FillPatternType;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;

import java.io.FileOutputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class LeanReportService {

    private static final DateTimeFormatter FILE_TIMESTAMP = DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss");
    private static final long REPORT_URL_EXPIRY_MINUTES = 10;
    private static final int EXCEL_CELL_CHAR_LIMIT = 32767;
    private static final int RAW_PAYLOAD_CHUNK_SIZE = 16000;
    private static final int RAW_PAYLOAD_COLUMNS = 2;
    private static final ObjectMapper JSON_MAPPER = new ObjectMapper()
            .enable(SerializationFeature.INDENT_OUTPUT);

    private final S3Service s3Service;
    private final LeanTransactionReportRepository leanTransactionReportRepository;
    private final IncomeInsightReportRepository incomeInsightReportRepository;
    private final ExpenseInsightReportRepository expenseInsightReportRepository;

    public ReportLink captureTransactionReport(Long userId,
                                               String accountId,
                                               LocalDate fromDate,
                                               LocalDate toDate,
                                               Object responseObj) {
        try {
            Map<String, Object> response = asMap(responseObj);
            Map<String, Object> payload = asMap(response.get("payload"));
            List<Map<String, Object>> transactions = asMapList(payload.get("transactions"));
            if (transactions.isEmpty()) {
                log.info("No transactions to persist for user {} account {}", userId, accountId);
                return null;
            }
            TransactionReportBundle bundle = buildTransactionBundle(transactions);
            String reportKey = generateTransactionWorkbook(userId, accountId, bundle, responseObj);
            if (reportKey == null) {
                return null;
            }
            persistTransactionReport(userId, accountId, fromDate, toDate, bundle, reportKey);
            return buildReportLink(reportKey);
        } catch (Exception ex) {
            log.error("Failed to capture transaction report for user {} account {}", userId, accountId, ex);
            return null;
        }
    }

    public ReportLink captureIncomeReport(Long userId,
                                          LocalDate startDate,
                                          String incomeType,
                                          Object responseObj) {
        try {
            Map<String, Object> response = asMap(responseObj);
            Map<String, Object> insights = asMap(response.get("insights"));
            if (insights.isEmpty()) {
                log.info("No income insights present for user {}", userId);
                return null;
            }
            IncomeReportBundle bundle = buildIncomeBundle(insights);
            if (bundle.summaries().isEmpty() &&
                    bundle.transactions().isEmpty() &&
                    bundle.monthlyRows().isEmpty()) {
                log.info("Income response had no actionable data for user {}", userId);
                return null;
            }
            String reportKey = generateIncomeWorkbook(userId, bundle, responseObj);
            if (reportKey == null) {
                return null;
            }
            persistIncomeReports(userId, bundle, reportKey);
            return buildReportLink(reportKey);
        } catch (Exception ex) {
            log.error("Failed to capture income report for user {} (type={}, startDate={})",
                    userId, incomeType, startDate, ex);
            return null;
        }
    }

    public ReportLink captureExpenseReport(Long userId,
                                           LocalDate startDate,
                                           Object responseObj) {
        try {
            Map<String, Object> response = asMap(responseObj);
            Map<String, Object> insights = asMap(response.get("insights"));
            if (insights.isEmpty()) {
                log.info("No expense insights present for user {}", userId);
                return null;
            }
            ExpenseReportBundle bundle = buildExpenseBundle(insights);
            if (bundle.summary() == null &&
                    bundle.categories().isEmpty() &&
                    bundle.monthlyTotals().isEmpty()) {
                log.info("Expense response had no actionable data for user {}", userId);
                return null;
            }
            String reportKey = generateExpenseWorkbook(userId, bundle, responseObj);
            if (reportKey == null) {
                return null;
            }
            persistExpenseReport(userId, bundle, reportKey);
            return buildReportLink(reportKey);
        } catch (Exception ex) {
            log.error("Failed to capture expense report for user {} startDate={}", userId, startDate, ex);
            return null;
        }
    }

    // Helper methods and DTOs are defined below.

    private TransactionReportBundle buildTransactionBundle(List<Map<String, Object>> transactions) {
        List<TransactionRow> rows = new ArrayList<>();
        Map<String, CategoryAccumulator> categoryAggregator = new LinkedHashMap<>();
        BigDecimal totalIncome = BigDecimal.ZERO;
        BigDecimal totalExpenses = BigDecimal.ZERO;
        LocalDateTime minTimestamp = null;
        LocalDateTime maxTimestamp = null;

        int index = 1;
        for (Map<String, Object> txn : transactions) {
            BigDecimal amount = toBigDecimal(txn.get("amount"));
            String transactionId = toStringValue(txn.get("id"));
            String description = toStringValue(txn.get("description"));
            String currency = toStringValue(txn.get("currency_code"));
            LocalDateTime timestamp = parseDateTime(txn.get("timestamp"));

            Map<String, Object> insightMap = asMap(txn.get("insights"));
            String category = toStringValue(insightMap.get("category"));
            BigDecimal confidence = toBigDecimal(insightMap.get("category_confidence"));
            String normalizedCategory = (category == null || category.isBlank()) ? "Unknown" : category;

            if (amount != null) {
                if (amount.signum() >= 0) {
                    totalIncome = totalIncome.add(amount);
                } else {
                    totalExpenses = totalExpenses.add(amount);
                }
            }
            if (timestamp != null) {
                minTimestamp = minTimestamp == null ? timestamp : minTimestamp.isAfter(timestamp) ? timestamp : minTimestamp;
                maxTimestamp = maxTimestamp == null ? timestamp : maxTimestamp.isBefore(timestamp) ? timestamp : maxTimestamp;
            }

            CategoryAccumulator accumulator =
                    categoryAggregator.computeIfAbsent(normalizedCategory, key -> new CategoryAccumulator());
            accumulator.increment(amount);

            rows.add(new TransactionRow(
                    index++,
                    transactionId,
                    timestamp,
                    description,
                    amount,
                    currency,
                    normalizedCategory,
                    confidence
            ));
        }

        rows.sort(Comparator.comparing(
                row -> Optional.ofNullable(row.timestamp()).orElse(LocalDateTime.MAX)
        ));

        List<CategoryRow> categories = categoryAggregator.entrySet()
                .stream()
                .map(entry -> new CategoryRow(
                        entry.getKey(),
                        entry.getValue().count,
                        entry.getValue().totalAmount
                ))
                .collect(Collectors.toList());

        BigDecimal netBalance = totalIncome.add(totalExpenses);
        return new TransactionReportBundle(rows, categories, totalIncome, totalExpenses, netBalance,
                rows.size(), minTimestamp, maxTimestamp);
    }

    private String generateTransactionWorkbook(Long userId,
                                               String accountId,
                                               TransactionReportBundle bundle,
                                               Object rawResponse) {
        List<List<Object>> summaryRows = buildTransactionSummaryRows(bundle);
        List<List<Object>> categoryRows = buildTransactionCategoryRows(bundle.categories());
        List<List<Object>> transactionRows = buildTransactionRows(bundle.transactions());

        try {
            return writeWorkbookToS3(
                    "lean_transactions/user_" + userId + "_acct_" + sanitizeForKey(accountId),
                    workbook -> {
                        SheetWriter writer = SheetWriter.create(workbook, "Transactions");
                        writer.writeSection("Summary", List.of("Metric", "Value"), summaryRows);
                        writer.writeSection("Category Breakdown",
                                List.of("Category", "Count", "Total Amount"), categoryRows);
                        writer.writeSection("Transactions", Arrays.asList(
                                "Sr No.",
                                "Transaction ID",
                                "Timestamp",
                                "Description",
                                "Amount",
                                "Currency",
                                "Category",
                                "Category Confidence"
                        ), transactionRows);
                        writer.autoSize();
                        addRawPayloadSheet(workbook, rawResponse);
                    }
            );
        } catch (IOException ex) {
            log.error("Unable to generate/upload transaction workbook for user {} account {}", userId, accountId, ex);
            return null;
        }
    }

    private void persistTransactionReport(Long userId,
                                          String accountId,
                                          LocalDate fromDate,
                                          LocalDate toDate,
                                          TransactionReportBundle bundle,
                                          String reportKey) {
        LocalDateTime now = LocalDateTime.now();
        LeanTransactionReport report = new LeanTransactionReport();
        report.setUserId(userId);
        report.setAccountId(accountId);
        report.setFromDate(resolveRange(bundle.fromTimestamp(), fromDate));
        report.setToDate(resolveRange(bundle.toTimestamp(), toDate));
        report.setTotalTransactions(bundle.totalCount());
        report.setTotalIncome(scaleMoney(bundle.totalIncome()));
        report.setTotalExpenses(scaleMoney(bundle.totalExpenses()));
        report.setNetBalance(scaleMoney(bundle.netBalance()));
        report.setReportFileUrl(reportKey);
        report.setRemarks("Auto-generated on " + now);
        report.setCreatedAt(now);
        report.setUpdatedAt(now);
        leanTransactionReportRepository.save(report);
    }

    private List<List<Object>> buildTransactionSummaryRows(TransactionReportBundle bundle) {
        List<List<Object>> rows = new ArrayList<>();
        rows.add(Arrays.asList("From Date", bundle.fromTimestamp()));
        rows.add(Arrays.asList("To Date", bundle.toTimestamp()));
        rows.add(Arrays.asList("Total Transactions", bundle.totalCount()));
        rows.add(Arrays.asList("Total Income", scaleMoney(bundle.totalIncome())));
        rows.add(Arrays.asList("Total Expenses", scaleMoney(bundle.totalExpenses())));
        rows.add(Arrays.asList("Net Balance", scaleMoney(bundle.netBalance())));
        return rows;
    }

    private List<List<Object>> buildTransactionCategoryRows(List<CategoryRow> categories) {
        List<List<Object>> rows = new ArrayList<>();
        for (CategoryRow category : categories) {
            rows.add(Arrays.asList(
                    category.name(),
                    category.count(),
                    scaleMoney(category.totalAmount())
            ));
        }
        return rows;
    }

    private List<List<Object>> buildTransactionRows(List<TransactionRow> transactions) {
        List<List<Object>> rows = new ArrayList<>();
        for (TransactionRow txn : transactions) {
            rows.add(Arrays.asList(
                    txn.index(),
                    txn.transactionId(),
                    txn.timestamp(),
                    txn.description(),
                    scaleMoney(txn.amount()),
                    txn.currency(),
                    txn.category(),
                    txn.categoryConfidence()
            ));
        }
        return rows;
    }

    private IncomeReportBundle buildIncomeBundle(Map<String, Object> insights) {
        List<IncomeSummaryRow> summaries = new ArrayList<>();
        List<IncomeTransactionRow> transactions = new ArrayList<>();
        List<IncomeMonthlyRow> monthlyRows = new ArrayList<>();

        insights.forEach((incomeTypeRaw, payloadObj) -> {
            if ("income_factors".equalsIgnoreCase(incomeTypeRaw)) {
                return;
            }
            Map<String, Object> payload = asMap(payloadObj);
            if (payload.isEmpty()) {
                return;
            }
            boolean hasMeaningfulData = payload.containsKey("total")
                    || payload.containsKey("transactions")
                    || payload.containsKey("monthly_totals");
            if (!hasMeaningfulData) {
                return;
            }
            String displayLabel = formatIncomeLabel(incomeTypeRaw);
            Map<String, Object> total = asMap(payload.get("total"));
            if (!total.isEmpty()) {
                summaries.add(new IncomeSummaryRow(
                        incomeTypeRaw,
                        displayLabel,
                        scaleMoney(toBigDecimal(total.get("amount"))),
                        scaleMoney(toBigDecimal(total.get("average_monthly_amount"))),
                        toInteger(total.get("count")),
                        parseDateTime(total.get("first_date_time")),
                        parseDateTime(total.get("last_date_time")),
                        toStringValue(payload.get("currency"))
                ));
            }

            List<Map<String, Object>> txnList = asMapList(payload.get("transactions"));
            for (Map<String, Object> txn : txnList) {
                Map<String, Object> source = asMap(txn.get("income_source"));
                transactions.add(new IncomeTransactionRow(
                        displayLabel,
                        toStringValue(txn.get("transaction_id")),
                        scaleMoney(toBigDecimal(txn.get("amount"))),
                        parseDateTime(txn.get("booking_date_time")),
                        toStringValue(txn.get("transaction_information")),
                        toStringValue(txn.get("account_id")),
                        toStringValue(source.get("name")),
                        toStringValue(source.get("type"))
                ));
            }

            List<Map<String, Object>> monthlyList = asMapList(payload.get("monthly_totals"));
            for (Map<String, Object> monthEntry : monthlyList) {
                monthlyRows.add(new IncomeMonthlyRow(
                        displayLabel,
                        toInteger(monthEntry.get("year")),
                        toInteger(monthEntry.get("month")),
                        scaleMoney(toBigDecimal(monthEntry.get("amount"))),
                        toInteger(monthEntry.get("count")),
                        toBoolean(monthEntry.get("is_month_complete"))
                ));
            }
        });

        transactions.sort(Comparator.comparing(
                row -> Optional.ofNullable(row.bookingDate()).orElse(LocalDateTime.MAX)
        ));

        monthlyRows.sort(Comparator
                .comparing(IncomeMonthlyRow::year, Comparator.nullsLast(Integer::compareTo))
                .thenComparing(IncomeMonthlyRow::month, Comparator.nullsLast(Integer::compareTo)));

        IncomeFactors incomeFactors = extractIncomeFactors(insights.get("income_factors"));

        return new IncomeReportBundle(summaries, transactions, monthlyRows, incomeFactors);
    }

    private String generateIncomeWorkbook(Long userId, IncomeReportBundle bundle, Object rawResponse) {
        List<List<Object>> summaryRows = new ArrayList<>();
        for (IncomeSummaryRow summary : bundle.summaries()) {
            summaryRows.add(Arrays.asList(
                    summary.displayType(),
                    summary.totalAmount(),
                    summary.averageMonthlyAmount(),
                    summary.count(),
                    summary.firstDate(),
                    summary.lastDate(),
                    summary.currency()
            ));
        }

        List<List<Object>> transactionRows = new ArrayList<>();
        for (IncomeTransactionRow row : bundle.transactions()) {
            transactionRows.add(Arrays.asList(
                    row.incomeType(),
                    row.transactionId(),
                    row.amount(),
                    row.bookingDate(),
                    row.information(),
                    row.accountId(),
                    row.sourceName(),
                    row.sourceType()
            ));
        }

        List<List<Object>> monthlyRows = new ArrayList<>();
        for (IncomeMonthlyRow month : bundle.monthlyRows()) {
            monthlyRows.add(Arrays.asList(
                    month.incomeType(),
                    month.year(),
                    month.month(),
                    month.amount(),
                    month.count(),
                    month.monthComplete()
            ));
        }

        List<List<Object>> factorSummaryRows = new ArrayList<>();
        List<List<Object>> factorPeriodRows = new ArrayList<>();
        IncomeFactors factors = bundle.incomeFactors();
        if (factors != null) {
            if (factors.deltaMinMax() != null) {
                factorSummaryRows.add(Arrays.asList("Delta Min Max", scaleMoney(factors.deltaMinMax())));
            }
            if (factors.averageMonthlyIncomeChange() != null) {
                factorSummaryRows.add(Arrays.asList("Average Monthly Income Change", scaleMoney(factors.averageMonthlyIncomeChange())));
            }
            for (IncomeFactorPeriod period : factors.periods()) {
                factorPeriodRows.add(Arrays.asList(
                        period.period(),
                        scaleMoney(period.incomeVariation()),
                        scaleMoney(period.averageIncomeAmount()),
                        period.averageIncomeCurrency()
                ));
            }
        }

        try {
            return writeWorkbookToS3(
                    "income_insights/user_" + userId,
                    workbook -> {
                        SheetWriter writer = SheetWriter.create(workbook, "Income Insights");
                        writer.writeSection("Income Totals", Arrays.asList(
                                "Income Type",
                                "Total Amount",
                                "Average Monthly Amount",
                                "Count",
                                "First Date",
                                "Last Date",
                                "Currency"
                        ), summaryRows);
                        writer.writeSection("Income Transactions", Arrays.asList(
                                "Income Type",
                                "Transaction ID",
                                "Amount",
                                "Booking Date",
                                "Information",
                                "Account ID",
                                "Source Name",
                                "Source Type"
                        ), transactionRows);
                        writer.writeSection("Monthly Totals", Arrays.asList(
                                "Income Type",
                                "Year",
                                "Month",
                                "Amount",
                                "Count",
                                "Month Complete"
                        ), monthlyRows);
                        writer.writeSection("Income Factors", List.of("Metric", "Value"), factorSummaryRows);
                        writer.writeSection("Income Factor Periods", Arrays.asList(
                                "Period",
                                "Income Variation",
                                "Average Income Amount",
                                "Average Income Currency"
                        ), factorPeriodRows);
                        writer.autoSize();
                        addRawPayloadSheet(workbook, rawResponse);
                    }
            );
        } catch (IOException ex) {
            log.error("Unable to generate/upload income workbook for user {}", userId, ex);
            return null;
        }
    }

    private void persistIncomeReports(Long userId,
                                      IncomeReportBundle bundle,
                                      String reportKey) {
        if (bundle.summaries().isEmpty()) {
            return;
        }
        LocalDateTime now = LocalDateTime.now();
        List<IncomeInsightReport> entities = new ArrayList<>();
        for (IncomeSummaryRow summary : bundle.summaries()) {
            IncomeInsightReport entity = new IncomeInsightReport();
            entity.setUserId(userId);
            entity.setIncomeType(summary.rawType());
            entity.setTotalAmount(summary.totalAmount());
            entity.setAverageMonthlyAmount(summary.averageMonthlyAmount());
            entity.setTxnCount(summary.count());
            entity.setFirstDate(summary.firstDate());
            entity.setLastDate(summary.lastDate());
            entity.setCurrency(summary.currency());
            entity.setReportFileUrl(reportKey);
            entity.setRemarks("Income report generated on " + now);
            entity.setCreatedAt(now);
            entity.setUpdatedAt(now);
            entities.add(entity);
        }
        incomeInsightReportRepository.saveAll(entities);
    }

    private IncomeFactors extractIncomeFactors(Object node) {
        Map<String, Object> factorsMap = asMap(node);
        if (factorsMap.isEmpty()) {
            return null;
        }
        BigDecimal delta = scaleMoney(toBigDecimal(factorsMap.get("delta_min_max")));
        BigDecimal averageChange = scaleMoney(toBigDecimal(factorsMap.get("average_monthly_income_change")));
        List<IncomeFactorPeriod> periods = new ArrayList<>();
        List<Map<String, Object>> periodList = asMapList(factorsMap.get("periods"));
        for (Map<String, Object> periodEntry : periodList) {
            Map<String, Object> avgIncome = asMap(periodEntry.get("average_income"));
            periods.add(new IncomeFactorPeriod(
                    toStringValue(periodEntry.get("period")),
                    scaleMoney(toBigDecimal(periodEntry.get("income_variation"))),
                    scaleMoney(toBigDecimal(avgIncome.get("amount"))),
                    toStringValue(avgIncome.get("currency"))
            ));
        }
        return new IncomeFactors(delta, averageChange, periods);
    }

    private ExpenseReportBundle buildExpenseBundle(Map<String, Object> insights) {
        Map<String, Object> total = asMap(insights.get("total"));
        ExpenseSummaryRow summaryRow = null;
        if (!total.isEmpty()) {
            summaryRow = new ExpenseSummaryRow(
                    scaleMoney(toBigDecimal(total.get("amount"))),
                    scaleMoney(toBigDecimal(total.get("average_monthly_amount"))),
                    toInteger(total.get("count")),
                    parseDateTime(total.get("first_date_time")),
                    parseDateTime(total.get("last_date_time")),
                    toStringValue(insights.get("currency"))
            );
        }

        List<ExpenseCategoryRow> categories = new ArrayList<>();
        List<Map<String, Object>> breakdowns = asMapList(total.get("breakdown"));
        for (Map<String, Object> breakdown : breakdowns) {
            String by = toStringValue(breakdown.get("by"));
            if (!"category".equalsIgnoreCase(by)) {
                continue;
            }
            List<Map<String, Object>> inner = asMapList(breakdown.get("breakdowns"));
            for (Map<String, Object> entry : inner) {
                categories.add(new ExpenseCategoryRow(
                        defaultString(entry.get("category"), "Unknown"),
                        scaleMoney(toBigDecimal(entry.get("amount"))),
                        scaleMoney(toBigDecimal(entry.get("fraction_of_total_expenses"))),
                        toInteger(entry.get("count")),
                        scaleMoney(toBigDecimal(entry.get("average_monthly_amount"))),
                        scaleMoney(toBigDecimal(entry.get("average_monthly_count"))),
                        scaleMoney(toBigDecimal(entry.get("average_days_between_transactions"))),
                        parseDateTime(entry.get("last_date_time")),
                        parseDateTime(entry.get("first_date_time"))
                ));
            }
        }

        List<ExpenseMonthlyRow> monthlyRows = new ArrayList<>();
        List<ExpenseMonthlyBreakdownRow> monthlyBreakdowns = new ArrayList<>();
        List<Map<String, Object>> monthly = asMapList(insights.get("monthly_totals"));
        for (Map<String, Object> entry : monthly) {
            Integer year = toInteger(entry.get("year"));
            Integer monthValue = toInteger(entry.get("month"));
            monthlyRows.add(new ExpenseMonthlyRow(
                    year,
                    monthValue,
                    scaleMoney(toBigDecimal(entry.get("amount"))),
                    toInteger(entry.get("count")),
                    toBoolean(entry.get("is_month_complete"))
            ));
            List<Map<String, Object>> breakdownList = asMapList(entry.get("breakdown"));
            for (Map<String, Object> breakdown : breakdownList) {
                String by = toStringValue(breakdown.get("by"));
                if (!"category".equalsIgnoreCase(by)) {
                    continue;
                }
                List<Map<String, Object>> categoryEntries = asMapList(breakdown.get("breakdowns"));
                for (Map<String, Object> categoryEntry : categoryEntries) {
                    monthlyBreakdowns.add(new ExpenseMonthlyBreakdownRow(
                            year,
                            monthValue,
                            defaultString(categoryEntry.get("category"), "Unknown"),
                            scaleMoney(toBigDecimal(categoryEntry.get("amount"))),
                            scaleMoney(toBigDecimal(categoryEntry.get("fraction_of_total_expenses"))),
                            toInteger(categoryEntry.get("count"))
                    ));
                }
            }
        }

        monthlyRows.sort(Comparator
                .comparing(ExpenseMonthlyRow::year, Comparator.nullsLast(Integer::compareTo))
                .thenComparing(ExpenseMonthlyRow::month, Comparator.nullsLast(Integer::compareTo)));

        monthlyBreakdowns.sort(Comparator
                .comparing(ExpenseMonthlyBreakdownRow::year, Comparator.nullsLast(Integer::compareTo))
                .thenComparing(ExpenseMonthlyBreakdownRow::month, Comparator.nullsLast(Integer::compareTo))
                .thenComparing(ExpenseMonthlyBreakdownRow::category, Comparator.nullsLast(String::compareTo)));

        return new ExpenseReportBundle(summaryRow, categories, monthlyRows, monthlyBreakdowns);
    }

    private String generateExpenseWorkbook(Long userId, ExpenseReportBundle bundle, Object rawResponse) {
        List<List<Object>> summaryRows = new ArrayList<>();
        if (bundle.summary() != null) {
            ExpenseSummaryRow summary = bundle.summary();
            summaryRows.add(Arrays.asList(
                    summary.totalAmount(),
                    summary.averageMonthlyAmount(),
                    summary.count(),
                    summary.firstDate(),
                    summary.lastDate(),
                    summary.currency()
            ));
        }

        List<List<Object>> categoryRows = new ArrayList<>();
        for (ExpenseCategoryRow category : bundle.categories()) {
            categoryRows.add(Arrays.asList(
                    category.category(),
                    category.amount(),
                    category.fractionOfTotal(),
                    category.count(),
                    category.averageMonthlyAmount(),
                    category.averageMonthlyCount(),
                    category.averageDaysBetween(),
                    category.lastDate(),
                    category.firstDate()
            ));
        }

        List<List<Object>> monthlyRows = new ArrayList<>();
        for (ExpenseMonthlyRow monthly : bundle.monthlyTotals()) {
            monthlyRows.add(Arrays.asList(
                    monthly.year(),
                    monthly.month(),
                    monthly.amount(),
                    monthly.count(),
                    monthly.monthComplete()
            ));
        }

        List<List<Object>> monthlyBreakdownRows = new ArrayList<>();
        for (ExpenseMonthlyBreakdownRow breakdown : bundle.monthlyCategoryBreakdowns()) {
            monthlyBreakdownRows.add(Arrays.asList(
                    breakdown.year(),
                    breakdown.month(),
                    breakdown.category(),
                    breakdown.amount(),
                    breakdown.fractionOfTotal(),
                    breakdown.count()
            ));
        }

        try {
            return writeWorkbookToS3(
                    "expense_insights/user_" + userId,
                    workbook -> {
                        SheetWriter writer = SheetWriter.create(workbook, "Expense Insights");
                        writer.writeSection("Expense Totals", Arrays.asList(
                                "Total Amount",
                                "Average Monthly Amount",
                                "Count",
                                "First Date",
                                "Last Date",
                                "Currency"
                        ), summaryRows);
                        writer.writeSection("Category Breakdown", Arrays.asList(
                                "Category",
                                "Amount",
                                "Fraction Of Total",
                                "Count",
                                "Average Monthly Amount",
                                "Average Monthly Count",
                                "Average Days Between",
                                "Last Date",
                                "First Date"
                        ), categoryRows);
                        writer.writeSection("Monthly Totals", Arrays.asList(
                                "Year",
                                "Month",
                                "Amount",
                                "Count",
                                "Month Complete"
                        ), monthlyRows);
                        writer.writeSection("Monthly Category Breakdown", Arrays.asList(
                                "Year",
                                "Month",
                                "Category",
                                "Amount",
                                "Fraction Of Total",
                                "Count"
                        ), monthlyBreakdownRows);
                        writer.autoSize();
                        addRawPayloadSheet(workbook, rawResponse);
                    }
            );
        } catch (IOException ex) {
            log.error("Unable to generate/upload expense workbook for user {}", userId, ex);
            return null;
        }
    }

    private void persistExpenseReport(Long userId,
                                      ExpenseReportBundle bundle,
                                      String reportKey) {
        ExpenseSummaryRow summary = bundle.summary();
        if (summary == null) {
            return;
        }
        LocalDateTime now = LocalDateTime.now();
        ExpenseInsightReport entity = new ExpenseInsightReport();
        entity.setUserId(userId);
        entity.setTotalAmount(summary.totalAmount());
        entity.setAverageMonthlyAmount(summary.averageMonthlyAmount());
        entity.setTxnCount(summary.count());
        entity.setFirstDate(summary.firstDate());
        entity.setLastDate(summary.lastDate());
        entity.setCurrency(summary.currency());
        entity.setReportFileUrl(reportKey);
        entity.setRemarks("Expense report generated on " + now);
        entity.setCreatedAt(now);
        entity.setUpdatedAt(now);
        expenseInsightReportRepository.save(entity);
    }

    private ReportLink buildReportLink(String objectKey) {
        if (objectKey == null) {
            return null;
        }
        String signedUrl = s3Service.generateFileUrl(objectKey, REPORT_URL_EXPIRY_MINUTES);
        return new ReportLink(objectKey, signedUrl);
    }

    private String writeWorkbookToS3(String keyPrefix, Consumer<Workbook> writer) throws IOException {
        Path tempFile = Files.createTempFile("lean_report_", ".xlsx");
        try {
            try (Workbook workbook = new XSSFWorkbook()) {
                writer.accept(workbook);
                try (FileOutputStream outputStream = new FileOutputStream(tempFile.toFile())) {
                    workbook.write(outputStream);
                }
            }
            String objectKey = String.format("reports/%s_%s.xlsx",
                    keyPrefix,
                    FILE_TIMESTAMP.format(LocalDateTime.now()));
            return s3Service.uploadFile(tempFile.toFile(), objectKey);
        } finally {
            try {
                Files.deleteIfExists(tempFile);
            } catch (IOException ex) {
                log.warn("Failed to delete temporary report file {}", tempFile, ex);
            }
        }
    }

    private void addRawPayloadSheet(Workbook workbook, Object payload) {
        String json = toJsonString(payload);
        if (json == null || json.isEmpty()) {
            return;
        }
        Sheet sheet = workbook.createSheet("Raw Payload");
        CellStyle style = workbook.createCellStyle();
        style.setWrapText(true);

        int pointer = 0;
        int rowIndex = 0;
        while (pointer < json.length()) {
            Row row = sheet.createRow(rowIndex++);
            int charsInRow = 0;
            for (int col = 0; col < RAW_PAYLOAD_COLUMNS && pointer < json.length(); col++) {
                int remaining = json.length() - pointer;
                int chunk = Math.min(RAW_PAYLOAD_CHUNK_SIZE, remaining);
                chunk = Math.min(chunk, EXCEL_CELL_CHAR_LIMIT);
                String slice = json.substring(pointer, pointer + chunk);
                pointer += chunk;
                charsInRow += slice.length();

                Cell cell = row.createCell(col);
                cell.setCellValue(slice);
                cell.setCellStyle(style);
                sheet.setColumnWidth(col, 80 * 256);
            }
            float height = Math.min(8192f, Math.max(row.getHeightInPoints(), charsInRow / 80f * 15f));
            row.setHeightInPoints(height);
        }
    }

    private String toJsonString(Object payload) {
        if (payload == null) {
            return "{}";
        }
        try {
            return JSON_MAPPER.writerWithDefaultPrettyPrinter().writeValueAsString(payload);
        } catch (JsonProcessingException ex) {
            log.warn("Failed to serialize raw payload, storing plain text copy", ex);
            return String.valueOf(payload);
        }
    }

    private Map<String, Object> asMap(Object value) {
        if (value instanceof Map<?, ?> source) {
            Map<String, Object> result = new LinkedHashMap<>();
            for (Map.Entry<?, ?> entry : source.entrySet()) {
                Object key = entry.getKey();
                if (key != null) {
                    result.put(key.toString(), entry.getValue());
                }
            }
            return result;
        }
        return Collections.emptyMap();
    }

    private List<Map<String, Object>> asMapList(Object value) {
        if (value instanceof List<?> list) {
            List<Map<String, Object>> result = new ArrayList<>();
            for (Object entry : list) {
                Map<String, Object> map = asMap(entry);
                if (!map.isEmpty()) {
                    result.add(map);
                }
            }
            return result;
        }
        return Collections.emptyList();
    }

    private String toStringValue(Object value) {
        if (value == null) {
            return null;
        }
        String str = value.toString().trim();
        return str.isEmpty() ? null : str;
    }

    private String defaultString(Object value, String fallback) {
        String str = toStringValue(value);
        return str == null ? fallback : str;
    }

    private Integer toInteger(Object value) {
        if (value == null) {
            return null;
        }
        if (value instanceof Number number) {
            return number.intValue();
        }
        if (value instanceof String str && !str.isBlank()) {
            try {
                return Integer.parseInt(str);
            } catch (NumberFormatException ignored) {
            }
        }
        return null;
    }

    private Boolean toBoolean(Object value) {
        if (value instanceof Boolean bool) {
            return bool;
        }
        if (value instanceof Number number) {
            return number.intValue() != 0;
        }
        if (value instanceof String str) {
            String normalized = str.trim().toLowerCase(Locale.ROOT);
            if (normalized.equals("true") || normalized.equals("yes") || normalized.equals("1")) {
                return true;
            }
            if (normalized.equals("false") || normalized.equals("no") || normalized.equals("0")) {
                return false;
            }
        }
        return null;
    }

    private BigDecimal toBigDecimal(Object value) {
        if (value == null) {
            return null;
        }
        if (value instanceof BigDecimal bigDecimal) {
            return bigDecimal;
        }
        if (value instanceof Number number) {
            return BigDecimal.valueOf(number.doubleValue());
        }
        if (value instanceof String str && !str.isBlank()) {
            try {
                return new BigDecimal(str);
            } catch (NumberFormatException ignored) {
            }
        }
        return null;
    }

    private BigDecimal scaleMoney(BigDecimal value) {
        if (value == null) {
            return null;
        }
        return value.setScale(2, RoundingMode.HALF_UP);
    }

    private LocalDateTime resolveRange(LocalDateTime actual, LocalDate fallbackDate) {
        if (actual != null) {
            return actual;
        }
        return fallbackDate != null ? fallbackDate.atStartOfDay() : null;
    }

    private LocalDateTime parseDateTime(Object value) {
        if (value == null) {
            return null;
        }
        if (value instanceof LocalDateTime localDateTime) {
            return localDateTime;
        }
        if (value instanceof LocalDate localDate) {
            return localDate.atStartOfDay();
        }
        if (value instanceof OffsetDateTime offsetDateTime) {
            return offsetDateTime.toLocalDateTime();
        }
        if (value instanceof Instant instant) {
            return LocalDateTime.ofInstant(instant, ZoneOffset.UTC);
        }
        if (value instanceof String str) {
            String trimmed = str.trim();
            if (trimmed.isEmpty()) {
                return null;
            }
            try {
                return OffsetDateTime.parse(trimmed).toLocalDateTime();
            } catch (DateTimeParseException ignored) {
            }
            try {
                return LocalDateTime.parse(trimmed);
            } catch (DateTimeParseException ignored) {
            }
            try {
                return LocalDateTime.ofInstant(Instant.parse(trimmed), ZoneOffset.UTC);
            } catch (DateTimeParseException ignored) {
            }
        }
        return null;
    }

    private String formatIncomeLabel(String raw) {
        if (raw == null || raw.isBlank()) {
            return "Unknown";
        }
        String normalized = raw.replace('_', ' ').toLowerCase(Locale.ROOT);
        return Arrays.stream(normalized.split(" "))
                .filter(token -> !token.isBlank())
                .map(token -> Character.toUpperCase(token.charAt(0)) + token.substring(1))
                .collect(Collectors.joining(" "));
    }

    private String sanitizeForKey(String text) {
        if (text == null || text.isBlank()) {
            return "unknown";
        }
        String sanitized = text.replaceAll("[^a-zA-Z0-9_-]", "");
        return sanitized.isBlank() ? "value" : sanitized;
    }

    private static class SheetWriter {
        private final Sheet sheet;
        private final CellStyle titleStyle;
        private final CellStyle headerStyle;
        private final CellStyle dateStyle;
        private int rowIndex = 0;
        private int maxColumns = 0;

        private SheetWriter(Workbook workbook, String sheetName) {
            this.sheet = workbook.createSheet(sheetName);
            this.titleStyle = buildTitleStyle(workbook);
            this.headerStyle = buildHeaderStyle(workbook);
            this.dateStyle = buildDateStyle(workbook);
        }

        static SheetWriter create(Workbook workbook, String sheetName) {
            return new SheetWriter(workbook, sheetName);
        }

        void writeSection(String title, List<String> headers, List<List<Object>> rows) {
            if (rows == null || rows.isEmpty()) {
                return;
            }
            Row titleRow = sheet.createRow(rowIndex++);
            Cell titleCell = titleRow.createCell(0);
            titleCell.setCellValue(title);
            titleCell.setCellStyle(titleStyle);

            rowIndex++;

            Row headerRow = sheet.createRow(rowIndex++);
            for (int i = 0; i < headers.size(); i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(headers.get(i));
                cell.setCellStyle(headerStyle);
            }
            maxColumns = Math.max(maxColumns, headers.size());

            for (List<Object> rowData : rows) {
                Row row = sheet.createRow(rowIndex++);
                writeRow(row, rowData);
            }

            rowIndex++;
        }

        private void writeRow(Row row, List<Object> values) {
            if (values == null) {
                return;
            }
            for (int i = 0; i < values.size(); i++) {
                Object value = values.get(i);
                Cell cell = row.createCell(i);
                if (value == null) {
                    continue;
                }
                if (value instanceof Number number) {
                    cell.setCellValue(number.doubleValue());
                } else if (value instanceof LocalDateTime dateTime) {
                    cell.setCellValue(dateTime);
                    cell.setCellStyle(dateStyle);
                } else if (value instanceof Boolean bool) {
                    cell.setCellValue(bool);
                } else {
                    cell.setCellValue(value.toString());
                }
            }
        }

        void autoSize() {
            for (int i = 0; i < maxColumns; i++) {
                sheet.autoSizeColumn(i);
            }
        }

        private CellStyle buildTitleStyle(Workbook workbook) {
            CellStyle style = workbook.createCellStyle();
            Font font = workbook.createFont();
            font.setBold(true);
            font.setFontHeightInPoints((short) 12);
            style.setFont(font);
            return style;
        }

        private CellStyle buildHeaderStyle(Workbook workbook) {
            CellStyle style = workbook.createCellStyle();
            Font font = workbook.createFont();
            font.setBold(true);
            style.setFont(font);
            style.setFillForegroundColor(IndexedColors.LIGHT_CORNFLOWER_BLUE.getIndex());
            style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
            return style;
        }

        private CellStyle buildDateStyle(Workbook workbook) {
            CellStyle style = workbook.createCellStyle();
            style.setDataFormat(workbook.getCreationHelper()
                    .createDataFormat()
                    .getFormat("yyyy-mm-dd hh:mm:ss"));
            return style;
        }
    }

    private record TransactionReportBundle(
            List<TransactionRow> transactions,
            List<CategoryRow> categories,
            BigDecimal totalIncome,
            BigDecimal totalExpenses,
            BigDecimal netBalance,
            int totalCount,
            LocalDateTime fromTimestamp,
            LocalDateTime toTimestamp
    ) {
    }

    private record TransactionRow(
            int index,
            String transactionId,
            LocalDateTime timestamp,
            String description,
            BigDecimal amount,
            String currency,
            String category,
            BigDecimal categoryConfidence
    ) {
    }

    private record CategoryRow(
            String name,
            int count,
            BigDecimal totalAmount
    ) {
    }

    private static class CategoryAccumulator {
        private int count = 0;
        private BigDecimal totalAmount = BigDecimal.ZERO;

        void increment(BigDecimal amount) {
            count++;
            if (amount != null) {
                totalAmount = totalAmount.add(amount);
            }
        }
    }

    private record IncomeReportBundle(
            List<IncomeSummaryRow> summaries,
            List<IncomeTransactionRow> transactions,
            List<IncomeMonthlyRow> monthlyRows,
            IncomeFactors incomeFactors
    ) {
    }

    private record IncomeFactors(
            BigDecimal deltaMinMax,
            BigDecimal averageMonthlyIncomeChange,
            List<IncomeFactorPeriod> periods
    ) {
    }

    private record IncomeFactorPeriod(
            String period,
            BigDecimal incomeVariation,
            BigDecimal averageIncomeAmount,
            String averageIncomeCurrency
    ) {
    }

    private record IncomeSummaryRow(
            String rawType,
            String displayType,
            BigDecimal totalAmount,
            BigDecimal averageMonthlyAmount,
            Integer count,
            LocalDateTime firstDate,
            LocalDateTime lastDate,
            String currency
    ) {
    }

    private record IncomeTransactionRow(
            String incomeType,
            String transactionId,
            BigDecimal amount,
            LocalDateTime bookingDate,
            String information,
            String accountId,
            String sourceName,
            String sourceType
    ) {
    }

    private record IncomeMonthlyRow(
            String incomeType,
            Integer year,
            Integer month,
            BigDecimal amount,
            Integer count,
            Boolean monthComplete
    ) {
    }

    private record ExpenseReportBundle(
            ExpenseSummaryRow summary,
            List<ExpenseCategoryRow> categories,
            List<ExpenseMonthlyRow> monthlyTotals,
            List<ExpenseMonthlyBreakdownRow> monthlyCategoryBreakdowns
    ) {
    }

    private record ExpenseSummaryRow(
            BigDecimal totalAmount,
            BigDecimal averageMonthlyAmount,
            Integer count,
            LocalDateTime firstDate,
            LocalDateTime lastDate,
            String currency
    ) {
    }

    private record ExpenseCategoryRow(
            String category,
            BigDecimal amount,
            BigDecimal fractionOfTotal,
            Integer count,
            BigDecimal averageMonthlyAmount,
            BigDecimal averageMonthlyCount,
            BigDecimal averageDaysBetween,
            LocalDateTime lastDate,
            LocalDateTime firstDate
    ) {
    }

    private record ExpenseMonthlyRow(
            Integer year,
            Integer month,
            BigDecimal amount,
            Integer count,
            Boolean monthComplete
    ) {
    }

    private record ExpenseMonthlyBreakdownRow(
            Integer year,
            Integer month,
            String category,
            BigDecimal amount,
            BigDecimal fractionOfTotal,
            Integer count
    ) {
    }

    public record ReportLink(
            String objectKey,
            String signedUrl
    ) {
    }
}
