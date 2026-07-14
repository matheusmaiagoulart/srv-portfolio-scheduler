CREATE TABLE purchase_orders (
    id                BIGINT IDENTITY(1,1) NOT NULL,
    master_account_id BIGINT               NOT NULL,
    ticker            NVARCHAR(10)         NOT NULL,
    quantity          INT                  NOT NULL,
    unit_price        DECIMAL(18,4)        NOT NULL,
    market_type       NVARCHAR(50)         NOT NULL,
    execution_date    DATETIME             NOT NULL,
    CONSTRAINT PK_purchase_orders PRIMARY KEY (id)
);

-- Indexes: purchase_orders
CREATE INDEX IX_PurchaseOrder_Ticker        ON purchase_orders(ticker);
CREATE INDEX IX_PurchaseOrder_ExecutionDate ON purchase_orders(execution_date);

CREATE TABLE deliveries (
    id                  BIGINT IDENTITY(1,1) NOT NULL,
    purchase_order_id   BIGINT               NOT NULL,
    custody_customer_id BIGINT               NOT NULL,
    ticker              NVARCHAR(10)         NOT NULL,
    quantity            INT                  NOT NULL,
    unit_price          DECIMAL(18,2)        NOT NULL,
    delivery_date       DATETIME             NOT NULL,
    CONSTRAINT PK_deliveries PRIMARY KEY (id),
    CONSTRAINT FK_deliveries_purchase_orders
        FOREIGN KEY (purchase_order_id)
        REFERENCES purchase_orders(id)
        ON DELETE CASCADE
);

-- Indexes: deliveries
CREATE INDEX IX_Deliveries_PurchaseOrderId   ON deliveries(purchase_order_id);
CREATE INDEX IX_Deliveries_CustodyCustomerId ON deliveries(custody_customer_id);

