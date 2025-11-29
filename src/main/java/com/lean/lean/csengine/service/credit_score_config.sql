-- ============================================
-- CREDIT SCORE ENGINE CONFIGURATION DATA
-- Based on Rent-Savvy Scoring Matrix V6.1
-- ============================================

-- 0. ENGINE CONFIG
-- Base configuration for the scoring engine
INSERT INTO
    "EngineConfig" (
        "Id",
        "Name",
        "Description",
        "MinScore",
        "MaxScore",
        "ScoreCap",
        "TotalWeightage",
        "Status",
        "CreatedAt",
        "CreatedBy",
        "UpdatedAt",
        "UpdatedBy"
    )
VALUES (
        1,
        'ScoreEngine',
        'Rent-Savvy scoring config for v6.1',
        0,
        10,
        10,
        100,
        1,
        CURRENT_TIMESTAMP,
        NULL,
        CURRENT_TIMESTAMP,
        NULL
    );

-- ============================================
-- 1. SCORING MAIN CATEGORY
-- Main scoring categories with weights
-- ============================================
INSERT INTO
    "ScoringMainCategory" (
        "Id",
        "EngineConfigID",
        "Name",
        "DisplayName",
        "Weightage",
        "DisplayOrder",
        "CreatedAt",
        "CreatedBy",
        "UpdatedAt",
        "UpdatedBy"
    )
VALUES (
        1,
        1,
        'IncomeStability',
        'Income Stability',
        30.00,
        1,
        CURRENT_TIMESTAMP,
        NULL,
        CURRENT_TIMESTAMP,
        NULL
    ),
    (
        2,
        1,
        'AECB',
        'AECB Credit Score',
        25.00,
        2,
        CURRENT_TIMESTAMP,
        NULL,
        CURRENT_TIMESTAMP,
        NULL
    ),
    (
        3,
        1,
        'DebtToIncome',
        'Debt-to-Income',
        20.00,
        3,
        CURRENT_TIMESTAMP,
        NULL,
        CURRENT_TIMESTAMP,
        NULL
    ),
    (
        4,
        1,
        'ExpenseVariance',
        'Expense Variance',
        10.00,
        4,
        CURRENT_TIMESTAMP,
        NULL,
        CURRENT_TIMESTAMP,
        NULL
    ),
    (
        5,
        1,
        'BehaviouralFraud',
        'Behavioural Fraud',
        10.00,
        5,
        CURRENT_TIMESTAMP,
        NULL,
        CURRENT_TIMESTAMP,
        NULL
    ),
    (
        6,
        1,
        'Dependents',
        'Dependents',
        5.00,
        6,
        CURRENT_TIMESTAMP,
        NULL,
        CURRENT_TIMESTAMP,
        NULL
    );

-- ============================================
-- 2. SCORING CATEGORY TYPE
-- (8 types as per simplified structure)
-- ============================================
INSERT INTO
    "ScoringCategoryType" (
        "Id",
        "Name",
        "DisplayName",
        "CreatedBy",
        "CreatedAt",
        "UpdatedBy",
        "UpdatedAt"
    )
VALUES (
        1,
        'Salary',
        'Monthly Salary',
        1,
        CURRENT_TIMESTAMP,
        1,
        CURRENT_TIMESTAMP
    ),
    (
        2,
        'EmploymentTenure',
        'Employment Tenure (Months)',
        1,
        CURRENT_TIMESTAMP,
        1,
        CURRENT_TIMESTAMP
    ),
    (
        3,
        'DebtToIncome',
        'Monthly Debt To Income %',
        1,
        CURRENT_TIMESTAMP,
        1,
        CURRENT_TIMESTAMP
    ),
    (
        4,
        'AECBScore',
        'AECB Credit Score',
        1,
        CURRENT_TIMESTAMP,
        1,
        CURRENT_TIMESTAMP
    ),
    (
        5,
        'ExpenseVariance',
        'Expense Variance %',
        1,
        CURRENT_TIMESTAMP,
        1,
        CURRENT_TIMESTAMP
    ),
    (
        6,
        'SalaryMismatch',
        'Declared vs Lean Salary Mismatch',
        1,
        CURRENT_TIMESTAMP,
        1,
        CURRENT_TIMESTAMP
    ),
    (
        7,
        'ExpenseMismatch',
        'Expense Mismatch',
        1,
        CURRENT_TIMESTAMP,
        1,
        CURRENT_TIMESTAMP
    ),
    (
        8,
        'NumberOfDependents',
        'Number of Dependents',
        1,
        CURRENT_TIMESTAMP,
        1,
        CURRENT_TIMESTAMP
    );

-- ============================================
-- 3. SCORING CATEGORY CONFIG MASTER
-- Maps category types to main categories with calculation formulas
-- ============================================
INSERT INTO
    "ScoringCategoryConfigMaster" (
        "Id",
        "EngineConfigID",
        "MainCategoryID",
        "CategoryTypeID",
        "Weightage",
        "MinScore",
        "MaxScore",
        "CalculationFormula",
        "Status",
        "CreatedBy",
        "CreatedAt",
        "UpdatedBy",
        "UpdatedAt"
    )
VALUES
    -- Income Stability (MainCategoryID = 1, Weight = 30%)
    -- Salary component (60% of Income Stability)
    (
        1,
        1,
        1,
        1,
        60.00,
        0.00,
        10.00,
        '{"type": "salary_variance_scoring", "description": "Salary variance calculation - 0% var=9.5, 5%=8.5, 10-20%=6.5, 20%+=3.5"}',
        1,
        1,
        CURRENT_TIMESTAMP,
        1,
        CURRENT_TIMESTAMP
    ),

-- Employment Tenure (40% of Income Stability)
(
    2,
    1,
    1,
    2,
    40.00,
    0.00,
    10.00,
    '{"type": "employment_tenure_scoring", "description": "Tenure scoring - <3m=2, 3-6m=4, 6-12m=6.5, 1-2y=8, 2y+=9.5"}',
    1,
    1,
    CURRENT_TIMESTAMP,
    1,
    CURRENT_TIMESTAMP
),

-- Debt-to-Income (MainCategoryID = 3, Weight = 20%, 100%)
(
    3,
    1,
    3,
    3,
    100.00,
    0.00,
    10.00,
    '{"type": "debt_to_income_ratio_scoring", "description": "DTI calculation - <25%=10, 25-35%=9, 35-45%=8, 45-55%=7, 55-65%=6, 65-75%=5, 75%+=4"}',
    1,
    1,
    CURRENT_TIMESTAMP,
    1,
    CURRENT_TIMESTAMP
),

-- AECB Credit Score (MainCategoryID = 2, Weight = 25%, 100%)
(
    4,
    1,
    2,
    4,
    100.00,
    0.00,
    10.00,
    '{"type": "aecb_score_mapping", "description": "AECB to score conversion - 850+=10, 800-849=9.5, 750-799=8, 700-749=8, 650-699=6.5, 600-649=5.5, 550-599=4, <550=1.5"}',
    1,
    1,
    CURRENT_TIMESTAMP,
    1,
    CURRENT_TIMESTAMP
),

-- Expense Variance (MainCategoryID = 4, Weight = 10%, 100%)
(
    5,
    1,
    4,
    5,
    100.00,
    -5.00,
    10.00,
    '{"type": "expense_variance_percentage", "description": "Variance penalty - 0-20%=10, 20-30%=-1, 30-40%=-2, 40%+=-3"}',
    1,
    1,
    CURRENT_TIMESTAMP,
    1,
    CURRENT_TIMESTAMP
),

-- Behavioral/Fraud (MainCategoryID = 5, Weight = 10%)
-- Salary Mismatch (50% of Behavioral)
(
    6,
    1,
    5,
    6,
    50.00,
    -4.00,
    10.00,
    '{"type": "salary_mismatch_penalty", "description": "Declared vs Lean mismatch - 0-10%=0, 10-25%=-1, 25-40%=-2, 40%+=-4"}',
    1,
    1,
    CURRENT_TIMESTAMP,
    1,
    CURRENT_TIMESTAMP
),

-- Expense Mismatch (50% of Behavioral)
(
    7,
    1,
    5,
    7,
    50.00,
    -5.00,
    10.00,
    '{"type": "expense_mismatch_penalty", "description": "Device/IP anomalies - No issue=10, Minor=5, Major=0"}',
    1,
    1,
    CURRENT_TIMESTAMP,
    1,
    CURRENT_TIMESTAMP
),

-- Dependents (MainCategoryID = 6, Weight = 5%, 100%)
(
    8,
    1,
    6,
    8,
    100.00,
    0.00,
    10.00,
    '{"type": "dependents_scoring", "description": "Dependents score - 0=10, 1=9, 2=7, 3=5, 4=3, 5+=1"}',
    1,
    1,
    CURRENT_TIMESTAMP,
    1,
    CURRENT_TIMESTAMP
);

-- ============================================
-- 4. SCORING CATEGORY CONFIG THRESHOLD
-- Score thresholds and ranges for each category type
-- ============================================
INSERT INTO
    "ScoringCategoryConfigThreshold" (
        "Id",
        "CategoryConfigMasterID",
        "MinScore",
        "MaxScore",
        "ScoreValue",
        "ScoreValueType"
    )
VALUES
    -- INCOME STABILITY - SALARY VARIANCE (CategoryConfigMasterID = 1)
    -- Input: salary variance %, Output: score (0-10)
    (
        1,
        1,
        0.00,
        5.00,
        9.50,
        'Fixed'
    ), -- 0-5% variance = 9.5/10
    (
        2,
        1,
        5.00,
        10.00,
        8.50,
        'Fixed'
    ), -- 5-10% variance = 8.5/10
    (
        3,
        1,
        10.00,
        20.00,
        6.50,
        'Fixed'
    ), -- 10-20% variance = 6.5/10
    (
        4,
        1,
        20.00,
        100.00,
        3.50,
        'Fixed'
    ), -- 20%+ variance = 3.5/10

-- INCOME STABILITY - EMPLOYMENT TENURE (CategoryConfigMasterID = 2)
-- Input: tenure in months, Output: score (0-10)
(
    5,
    2,
    0.00,
    3.00,
    2.00,
    'Fixed'
), -- Under 3 months = 2/10
(
    6,
    2,
    3.00,
    6.00,
    4.00,
    'Fixed'
), -- 3-6 months = 4/10
(
    7,
    2,
    6.00,
    12.00,
    6.50,
    'Fixed'
), -- 6-12 months = 6.5/10
(
    8,
    2,
    12.00,
    24.00,
    8.00,
    'Fixed'
), -- 1-2 years = 8/10
(
    9,
    2,
    24.00,
    999.00,
    9.50,
    'Fixed'
), -- 2+ years = 9.5/10

-- DEBT-TO-INCOME RATIO (CategoryConfigMasterID = 3)
-- Input: DTI %, Output: score (0-10)
(
    10,
    3,
    0.00,
    25.00,
    10.00,
    'Fixed'
), -- DTI < 25% = 10/10
(
    11,
    3,
    25.00,
    35.00,
    9.00,
    'Fixed'
), -- DTI 25-35% = 9/10
(
    12,
    3,
    35.00,
    45.00,
    8.00,
    'Fixed'
), -- DTI 35-45% = 8/10
(
    13,
    3,
    45.00,
    55.00,
    7.00,
    'Fixed'
), -- DTI 45-55% = 7/10
(
    14,
    3,
    55.00,
    65.00,
    6.00,
    'Fixed'
), -- DTI 55-65% = 6/10
(
    15,
    3,
    65.00,
    75.00,
    5.00,
    'Fixed'
), -- DTI 65-75% = 5/10
(
    16,
    3,
    75.00,
    100.00,
    4.00,
    'Fixed'
), -- DTI 75%+ = 4/10

-- AECB CREDIT SCORE (CategoryConfigMasterID = 4)
-- Input: AECB score, Output: mapped score (0-10)
(
    17,
    4,
    850.00,
    900.00,
    10.00,
    'Fixed'
), -- AECB 850+ = 10/10
(
    18,
    4,
    800.00,
    849.00,
    9.50,
    'Fixed'
), -- AECB 800-849 = 9.5/10
(
    19,
    4,
    750.00,
    799.00,
    8.00,
    'Fixed'
), -- AECB 750-799 = 8/10
(
    20,
    4,
    700.00,
    749.00,
    8.00,
    'Fixed'
), -- AECB 700-749 = 8/10
(
    21,
    4,
    650.00,
    699.00,
    6.50,
    'Fixed'
), -- AECB 650-699 = 6.5/10
(
    22,
    4,
    600.00,
    649.00,
    5.50,
    'Fixed'
), -- AECB 600-649 = 5.5/10
(
    23,
    4,
    550.00,
    599.00,
    4.00,
    'Fixed'
), -- AECB 550-599 = 4/10
(
    24,
    4,
    0.00,
    549.00,
    1.50,
    'Fixed'
), -- AECB < 550 = 1.5/10

-- EXPENSE VARIANCE (CategoryConfigMasterID = 5)
-- Input: variance %, Output: penalty/bonus score (-5 to 10)
(
    25,
    5,
    0.00,
    20.00,
    10.00,
    'Fixed'
), -- 0-20% variance = +10 (no penalty)
(
    26,
    5,
    20.00,
    30.00,
    -1.00,
    'Fixed'
), -- 20-30% variance = -1 penalty
(
    27,
    5,
    30.00,
    40.00,
    -2.00,
    'Fixed'
), -- 30-40% variance = -2 penalty
(
    28,
    5,
    40.00,
    100.00,
    -3.00,
    'Fixed'
), -- 40%+ variance = -3 penalty

-- BEHAVIORAL/FRAUD - SALARY MISMATCH (CategoryConfigMasterID = 6)
-- Input: mismatch %, Output: penalty score (-4 to 0)
(
    29,
    6,
    0.00,
    10.00,
    0.00,
    'Fixed'
), -- 0-10% mismatch = 0 (no penalty)
(
    30,
    6,
    10.00,
    25.00,
    -1.00,
    'Fixed'
), -- 10-25% mismatch = -1
(
    31,
    6,
    25.00,
    40.00,
    -2.00,
    'Fixed'
), -- 25-40% mismatch = -2
(
    32,
    6,
    40.00,
    100.00,
    -4.00,
    'Fixed'
), -- 40%+ mismatch = -4

-- BEHAVIORAL/FRAUD - EXPENSE MISMATCH (CategoryConfigMasterID = 7)
-- Input: anomaly count/level, Output: penalty score (-5 to 10)
(
    33,
    7,
    0.00,
    1.00,
    10.00,
    'Fixed'
), -- No anomalies = 10
(
    34,
    7,
    1.00,
    2.00,
    5.00,
    'Fixed'
), -- Minor anomalies = 5
(
    35,
    7,
    2.00,
    100.00,
    0.00,
    'Fixed'
), -- Major anomalies = 0

-- DEPENDENTS SCORING (CategoryConfigMasterID = 8)
-- Input: number of dependents, Output: score (0-10)
(
    36,
    8,
    0.00,
    1.00,
    10.00,
    'Fixed'
), -- 0 dependents = 10/10
(
    37,
    8,
    1.00,
    2.00,
    9.00,
    'Fixed'
), -- 1 dependent = 9/10
(
    38,
    8,
    2.00,
    3.00,
    7.00,
    'Fixed'
), -- 2 dependents = 7/10
(
    39,
    8,
    3.00,
    4.00,
    5.00,
    'Fixed'
), -- 3 dependents = 5/10
(
    40,
    8,
    4.00,
    5.00,
    3.00,
    'Fixed'
), -- 4 dependents = 3/10
(
    41,
    8,
    5.00,
    100.00,
    1.00,
    'Fixed'
);
-- 5+ dependents = 1/10

-- ============================================
-- 5. CREDIT SCORE ADJUSTMENT RULES
-- Bonus and Penalty Rules
-- ============================================
INSERT INTO
    "CreditScoreAdjustmentRules" (
        "Id",
        "EngineConfigID",
        "Key",
        "VendorKey",
        "DisplayName",
        "Points",
        "DisplayOrder"
    )
VALUES
    -- POSITIVE ADJUSTMENTS (Bonuses)
    (
        1,
        1,
        'SavingsRateBonus',
        'savings_rate_above_20',
        'Savings Rate > 20%',
        1.50,
        1
    ),
    (
        2,
        1,
        'RentalIncomeBonus',
        'verified_rental_income',
        'Verified Rental Income',
        1.00,
        2
    ),
    (
        3,
        1,
        'InvestmentIncomeBonus',
        'verified_investment_income',
        'Verified Investment Income',
        1.00,
        3
    ),
    (
        4,
        1,
        'FamilyAllowanceBonus',
        'family_allowance_income',
        'Family Allowance Income',
        0.50,
        4
    ),

-- NEGATIVE ADJUSTMENTS (Penalties)
(
    5,
    1,
    'HighExpenseVariancePenalty',
    'high_expense_variance',
    'High Expense Variance > 40%',
    -1.00,
    5
);

-- ============================================
-- 6. CREDIT SCORE ADJUSTMENT RULES CONFIG
-- Configuration for when each adjustment rule applies
-- ============================================
INSERT INTO
    "CreditScoreAdjustmentRulesConfig" (
        "Id",
        "AdjustmentRulesID",
        "Min",
        "Max",
        "Value",
        "ValueType",
        "Mode"
    )
VALUES
    -- Savings Rate Bonus: 20% to 100% = +1.5 points
    (
        1,
        1,
        20.00,
        100.00,
        1.50,
        'Fixed',
        'Positive'
    ),

-- Rental Income Bonus: Flat +1 point
( 2, 2, 0.00, 999999.00, 1.00, 'Fixed', 'Positive' ),

-- Investment Income Bonus: Flat +1 point
( 3, 3, 0.00, 999999.00, 1.00, 'Fixed', 'Positive' ),

-- Family Allowance Bonus: Flat +0.5 point
( 4, 4, 0.00, 999999.00, 0.50, 'Fixed', 'Positive' ),

-- High Expense Variance Penalty: > 40% variance = -1 point
( 5, 5, 40.00, 100.00, 1.00, 'Fixed', 'Negative' );

-- ============================================
-- 7. RISK TIER CONFIG
-- Risk tier configuration for final score assignment
-- ============================================
INSERT INTO
    "RiskTierConfig" (
        "Id",
        "EngineConfigId",
        "RiskTier",
        "MinScore",
        "MaxScore",
        "RiskLevel",
        "RentLimitPercentage",
        "Status",
        "CreatedAt",
        "CreatedBy",
        "UpdatedAt",
        "UpdatedBy"
    )
VALUES (
        1,
        1,
        'A',
        9.5,
        10.0,
        'Minimal',
        35.00,
        1,
        CURRENT_TIMESTAMP,
        NULL,
        CURRENT_TIMESTAMP,
        NULL
    ),
    (
        2,
        1,
        'A',
        8.5,
        9.49,
        'Low',
        33.00,
        1,
        CURRENT_TIMESTAMP,
        NULL,
        CURRENT_TIMESTAMP,
        NULL
    ),
    (
        3,
        1,
        'B',
        7.5,
        8.49,
        'Medium',
        30.00,
        1,
        CURRENT_TIMESTAMP,
        NULL,
        CURRENT_TIMESTAMP,
        NULL
    ),
    (
        4,
        1,
        'B',
        6.5,
        7.49,
        'Elevated',
        28.00,
        1,
        CURRENT_TIMESTAMP,
        NULL,
        CURRENT_TIMESTAMP,
        NULL
    ),
    (
        5,
        1,
        'C',
        5.0,
        6.49,
        'Borderline',
        25.00,
        1,
        CURRENT_TIMESTAMP,
        NULL,
        CURRENT_TIMESTAMP,
        NULL
    ),
    (
        6,
        1,
        'D',
        0.0,
        4.99,
        'High',
        20.00,
        1,
        CURRENT_TIMESTAMP,
        NULL,
        CURRENT_TIMESTAMP,
        NULL
    );

-- ============================================
-- COMPLETE CONFIGURATION SUMMARY
-- ============================================
-- EngineConfig: 1 engine configuration
-- ScoringMainCategory: 6 main categories (30%, 25%, 20%, 10%, 10%, 5%)
-- ScoringCategoryType: 8 input types
-- ScoringCategoryConfigMaster: 8 mappings with formulas
-- ScoringCategoryConfigThreshold: 41 threshold ranges
-- CreditScoreAdjustmentRules: 5 rules (4 bonus + 1 penalty)
-- CreditScoreAdjustmentRulesConfig: 5 rule configurations
-- RiskTierConfig: 6 risk tiers (A, A, B, B, C, D)
--
-- Total Configuration Records: 81 rows
-- All mappings based on:
-- - Rent-Savvy Scoring Matrix V6.1
-- - Sample Calculation Document (Nov 19, 2025)
-- ============================================