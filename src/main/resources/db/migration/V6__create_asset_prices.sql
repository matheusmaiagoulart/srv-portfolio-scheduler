CREATE TABLE asset_prices (
    id           BIGINT        IDENTITY(1,1) PRIMARY KEY,
    trading_date DATE          NOT NULL,
    ticker       VARCHAR(10)   NOT NULL,
    open_price   DECIMAL(18,4) NOT NULL,
    close_price  DECIMAL(18,4) NOT NULL,
    max_price    DECIMAL(18,4) NOT NULL,
    min_price    DECIMAL(18,4) NOT NULL
);

CREATE INDEX IX_asset_prices_ticker ON asset_prices (ticker);

