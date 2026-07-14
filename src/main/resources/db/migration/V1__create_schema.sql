CREATE TABLE recommended_portfolios (
    id               BIGINT IDENTITY(1,1) NOT NULL,
    name             NVARCHAR(100)        NOT NULL,
    active           BIT                  NOT NULL,
    created_at       DATETIME2(7)         NOT NULL,
    termination_date DATETIME2(7)         NULL,
    CONSTRAINT PK_recommended_portfolios PRIMARY KEY (id)
);

CREATE TABLE portfolio_items (
    id                        BIGINT IDENTITY(1,1) NOT NULL,
    recommended_portfolio_id  BIGINT               NOT NULL,
    ticker                    NVARCHAR(MAX)        NOT NULL,
    percentage                DECIMAL(5,2)         NOT NULL,
    CONSTRAINT PK_portfolio_items PRIMARY KEY (id),
    CONSTRAINT FK_portfolio_items_recommended_portfolios
        FOREIGN KEY (recommended_portfolio_id)
        REFERENCES recommended_portfolios(id)
        ON DELETE CASCADE
);

CREATE TABLE customers (
    id             BIGINT IDENTITY(1,1) NOT NULL,
    name           NVARCHAR(100)        NOT NULL,
    cpf            NVARCHAR(11)         NOT NULL,
    email          NVARCHAR(200)        NOT NULL,
    monthly_amount DECIMAL(18,2)        NOT NULL,
    active         BIT                  NOT NULL,
    joining_date   DATETIME2(7)         NOT NULL,
    CONSTRAINT PK_customers PRIMARY KEY (id)
);

-- Indexes: customers
CREATE UNIQUE INDEX IX_customers_cpf    ON customers(cpf);
CREATE UNIQUE INDEX IX_customers_email  ON customers(email);
CREATE INDEX        IX_customers_active ON customers(active);

CREATE TABLE brokerage_accounts (
    id             BIGINT IDENTITY(1,1) NOT NULL,
    customer_id    BIGINT               NOT NULL,
    account_number NVARCHAR(450)        NOT NULL,
    account_type   NVARCHAR(50)         NOT NULL,
    created_at     DATETIME2(7)         NOT NULL,
    CONSTRAINT PK_brokerage_accounts PRIMARY KEY (id),
    CONSTRAINT FK_brokerage_accounts_customers
        FOREIGN KEY (customer_id)
        REFERENCES customers(id)
        ON DELETE CASCADE
);

-- Indexes: brokerage_accounts
CREATE INDEX IX_brokerage_accounts_customer_id    ON brokerage_accounts(customer_id);
CREATE INDEX IX_brokerage_accounts_account_number ON brokerage_accounts(account_number);

CREATE TABLE custodies (
    id                   BIGINT IDENTITY(1,1) NOT NULL,
    brokerage_account_id BIGINT               NOT NULL,
    ticker               NVARCHAR(10)         NOT NULL,
    quantity             INT                  NOT NULL,
    average_price        DECIMAL(18,2)        NOT NULL,
    last_update          DATETIME             NOT NULL,
    CONSTRAINT PK_custodies PRIMARY KEY (id),
    CONSTRAINT FK_custodies_brokerage_accounts
        FOREIGN KEY (brokerage_account_id)
        REFERENCES brokerage_accounts(id)
        ON DELETE NO ACTION
);

-- Indexes: custodies
CREATE INDEX IX_custodies_brokerage_account_id ON custodies(brokerage_account_id);
