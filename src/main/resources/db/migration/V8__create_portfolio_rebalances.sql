CREATE TABLE portfolio_rebalances (
    id             BIGINT IDENTITY(1,1) NOT NULL,
    customer_id    BIGINT               NOT NULL,
    rebalance_type NVARCHAR(50)         NOT NULL,
    sold_ticker    NVARCHAR(10)         NOT NULL,
    bought_ticker  NVARCHAR(10)         NOT NULL,
    sold_amount    DECIMAL(18,2)        NOT NULL,
    rebalance_date DATETIME2(7)         NOT NULL,
    CONSTRAINT PK_portfolio_rebalances PRIMARY KEY (id)
);

-- Indexes: portfolio_rebalances
CREATE INDEX IX_portfolio_rebalances_customer_id ON portfolio_rebalances(customer_id);

