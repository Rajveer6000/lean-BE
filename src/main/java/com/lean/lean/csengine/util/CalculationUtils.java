package com.lean.lean.csengine.util;

import com.lean.lean.csengine.dto.CreditScoreConfigDTO;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Utility class for credit score calculations
 */
public class CalculationUtils {

    /**
     * Calculate variance (coefficient of variation) from a list of values
     * CV = (Standard Deviation / Mean) × 100
     */
    public static BigDecimal calculateVariance(List<BigDecimal> values) {
        if (values == null || values.isEmpty()) {
            return BigDecimal.ZERO;
        }

        // Filter out null and zero values
        List<BigDecimal> nonZeroValues = filterNonZeroValues(values);

        if (nonZeroValues.size() < 2) {
            return BigDecimal.ZERO;
        }

        // Calculate mean
        BigDecimal sum = nonZeroValues.stream()
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal mean = sum.divide(BigDecimal.valueOf(nonZeroValues.size()), 4, RoundingMode.HALF_UP);

        if (mean.compareTo(BigDecimal.ZERO) == 0) {
            return BigDecimal.ZERO;
        }

        // Calculate variance
        BigDecimal variance = nonZeroValues.stream()
                .map(value -> value.subtract(mean).pow(2))
                .reduce(BigDecimal.ZERO, BigDecimal::add)
                .divide(BigDecimal.valueOf(nonZeroValues.size()), 4, RoundingMode.HALF_UP);

        // Calculate standard deviation
        double stdDev = Math.sqrt(variance.doubleValue());

        // Calculate coefficient of variation (CV) as percentage
        BigDecimal cv = BigDecimal.valueOf(stdDev)
                .divide(mean, 4, RoundingMode.HALF_UP)
                .multiply(BigDecimal.valueOf(100))
                .setScale(2, RoundingMode.HALF_UP);

        return cv;
    }

    /**
     * Find the score for a given value using threshold configuration
     */
    public static BigDecimal findThresholdScore(BigDecimal value,
                                                List<CreditScoreConfigDTO.ThresholdInfo> thresholds) {
        if (value == null || thresholds == null || thresholds.isEmpty()) {
            return BigDecimal.ZERO;
        }

        for (CreditScoreConfigDTO.ThresholdInfo threshold : thresholds) {
            // Handle null values for minScore and maxScore
            BigDecimal minScore = threshold.getMinScore();
            BigDecimal maxScore = threshold.getMaxScore();

            // If minScore is null, assume no lower limit
            boolean minScoreValid = minScore == null || value.compareTo(minScore) >= 0;

            // If maxScore is null, assume no upper limit
            boolean maxScoreValid = maxScore == null || value.compareTo(maxScore) < 0;

            if (minScoreValid && maxScoreValid) {
                return threshold.getScoreValue();
            }
        }

        return BigDecimal.ZERO;
    }


    /**
     * Apply weightage to a score
     * Returns: (score × weightage) / 100
     */
    public static BigDecimal applyWeightage(BigDecimal score, BigDecimal weightage) {
        if (score == null || weightage == null) {
            return BigDecimal.ZERO;
        }

        return score.multiply(weightage)
                .divide(BigDecimal.valueOf(100), 4, RoundingMode.HALF_UP);
    }

    /**
     * Filter out null and zero values from a list
     */
    public static List<BigDecimal> filterNonZeroValues(List<BigDecimal> values) {
        if (values == null) {
            return List.of();
        }

        return values.stream()
                .filter(v -> v != null && v.compareTo(BigDecimal.ZERO) > 0)
                .collect(Collectors.toList());
    }

    /**
     * Calculate average of non-zero values
     */
    public static BigDecimal calculateAverage(List<BigDecimal> values) {
        List<BigDecimal> nonZeroValues = filterNonZeroValues(values);

        if (nonZeroValues.isEmpty()) {
            return BigDecimal.ZERO;
        }

        BigDecimal sum = nonZeroValues.stream()
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        return sum.divide(BigDecimal.valueOf(nonZeroValues.size()), 2, RoundingMode.HALF_UP);
    }

    /**
     * Calculate percentage difference between two values
     * Returns: |value1 - value2| / value2 × 100
     */
    public static BigDecimal calculatePercentageDifference(BigDecimal value1, BigDecimal value2) {
        if (value2 == null || value2.compareTo(BigDecimal.ZERO) == 0) {
            return BigDecimal.ZERO;
        }

        if (value1 == null) {
            return BigDecimal.valueOf(100);
        }

        BigDecimal difference = value1.subtract(value2).abs();
        return difference.divide(value2, 4, RoundingMode.HALF_UP)
                .multiply(BigDecimal.valueOf(100))
                .setScale(2, RoundingMode.HALF_UP);
    }

    /**
     * Cap a score to maximum value
     */
    public static BigDecimal capScore(BigDecimal score, BigDecimal maxScore) {
        if (score == null) {
            return BigDecimal.ZERO;
        }

        if (maxScore == null) {
            return score;
        }

        return score.compareTo(maxScore) > 0 ? maxScore : score;
    }
}
