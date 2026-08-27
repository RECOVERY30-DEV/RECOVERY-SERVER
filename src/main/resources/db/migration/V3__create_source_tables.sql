CREATE TABLE source_bank_accounts (
    id                BIGINT      NOT NULL AUTO_INCREMENT,
    business_id       BIGINT      NOT NULL,
    institution_name  VARCHAR(100) NOT NULL,
    account_no_masked VARCHAR(50),
    created_at        DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    PRIMARY KEY (id),
    KEY idx_source_bank_accounts_business (business_id),
    CONSTRAINT fk_source_bank_accounts_business FOREIGN KEY (business_id) REFERENCES core_businesses (id)
) ENGINE = InnoDB;

CREATE TABLE source_transactions (
    id            BIGINT      NOT NULL AUTO_INCREMENT,
    business_id   BIGINT      NOT NULL,
    account_id    BIGINT      NOT NULL,
    txn_date      DATE        NOT NULL,
    direction     CHAR(1)     NOT NULL,
    amount        BIGINT      NOT NULL,
    balance_after BIGINT,
    category      VARCHAR(30),
    counterparty  VARCHAR(100),
    is_confirmed  BOOLEAN     NOT NULL DEFAULT TRUE,
    PRIMARY KEY (id),
    KEY idx_source_transactions_business_date (business_id, txn_date),
    CONSTRAINT fk_source_transactions_business FOREIGN KEY (business_id) REFERENCES core_businesses (id),
    CONSTRAINT fk_source_transactions_account FOREIGN KEY (account_id) REFERENCES source_bank_accounts (id),
    CONSTRAINT chk_source_transactions_direction CHECK (direction IN ('I', 'O')),
    CONSTRAINT chk_source_transactions_amount CHECK (amount > 0)
) ENGINE = InnoDB;

CREATE TABLE source_card_settlements (
    id                 BIGINT      NOT NULL AUTO_INCREMENT,
    business_id        BIGINT      NOT NULL,
    card_company       VARCHAR(50) NOT NULL,
    sales_date         DATE        NOT NULL,
    settlement_date    DATE        NOT NULL,
    sales_amount       BIGINT      NOT NULL,
    fee_amount         BIGINT      NOT NULL DEFAULT 0,
    settlement_amount  BIGINT      NOT NULL,
    status             VARCHAR(20) NOT NULL,
    PRIMARY KEY (id),
    KEY idx_source_card_settlements_business_date (business_id, settlement_date),
    CONSTRAINT fk_source_card_settlements_business FOREIGN KEY (business_id) REFERENCES core_businesses (id),
    CONSTRAINT chk_source_card_settlements_status CHECK (status IN ('CONFIRMED', 'EXPECTED'))
) ENGINE = InnoDB;

CREATE TABLE source_loans (
    id                  BIGINT      NOT NULL AUTO_INCREMENT,
    business_id         BIGINT      NOT NULL,
    institution         VARCHAR(100) NOT NULL,
    loan_type           VARCHAR(50),
    outstanding_balance BIGINT      NOT NULL,
    interest_rate       DECIMAL(5, 2),
    rate_type           VARCHAR(20),
    repayment_type      VARCHAR(30),
    created_at          DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    PRIMARY KEY (id),
    KEY idx_source_loans_business (business_id),
    CONSTRAINT fk_source_loans_business FOREIGN KEY (business_id) REFERENCES core_businesses (id)
) ENGINE = InnoDB;

CREATE TABLE source_loan_schedules (
    id               BIGINT      NOT NULL AUTO_INCREMENT,
    loan_id          BIGINT      NOT NULL,
    due_date         DATE        NOT NULL,
    principal_amount BIGINT      NOT NULL,
    interest_amount  BIGINT      NOT NULL DEFAULT 0,
    total_amount     BIGINT      NOT NULL,
    status           VARCHAR(20) NOT NULL DEFAULT 'SCHEDULED',
    PRIMARY KEY (id),
    KEY idx_source_loan_schedules_loan_due (loan_id, due_date),
    CONSTRAINT fk_source_loan_schedules_loan FOREIGN KEY (loan_id) REFERENCES source_loans (id) ON DELETE CASCADE,
    CONSTRAINT chk_source_loan_schedules_status CHECK (status IN ('SCHEDULED', 'PAID', 'OVERDUE'))
) ENGINE = InnoDB;

CREATE TABLE source_recurring_expenses (
    id              BIGINT      NOT NULL AUTO_INCREMENT,
    business_id     BIGINT      NOT NULL,
    expense_type    VARCHAR(30) NOT NULL,
    amount          BIGINT      NOT NULL,
    recurrence_rule VARCHAR(100),
    next_due_date   DATE,
    is_auto_debit   BOOLEAN     NOT NULL DEFAULT TRUE,
    confidence      DECIMAL(5, 2),
    PRIMARY KEY (id),
    KEY idx_source_recurring_expenses_business (business_id),
    CONSTRAINT fk_source_recurring_expenses_business FOREIGN KEY (business_id) REFERENCES core_businesses (id),
    CONSTRAINT chk_source_recurring_expenses_type
        CHECK (expense_type IN ('RENT', 'UTILITY', 'PAYROLL', 'INSURANCE', 'TAX', 'SUBSCRIPTION'))
) ENGINE = InnoDB;
