CREATE TABLE ir_dedo_duro_outbox
(
    id            BIGINT IDENTITY (1,1) PRIMARY KEY,
    payload       NVARCHAR(MAX) NOT NULL,
    status        VARCHAR(20)   NOT NULL DEFAULT 'PENDING',
    created_at    DATETIME2(7)  NOT NULL DEFAULT SYSDATETIME(),
    published_at  DATETIME2(7)  NULL,
    error_message NVARCHAR(500) NULL
);