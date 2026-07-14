-- Customer MASTER (Corretora)
SET IDENTITY_INSERT customers ON;
INSERT INTO customers (id, name, cpf, email, monthly_amount, active, joining_date)
VALUES (1, N'CORRETORA - CONTA MASTER', N'00000000000', N'master@itaucorretora.com.br', 0.00, 1, '2026-01-01');
SET IDENTITY_INSERT customers OFF;

-- BrokerageAccount MASTER
SET IDENTITY_INSERT brokerage_accounts ON;
INSERT INTO brokerage_accounts (id, customer_id, account_number, account_type, created_at)
VALUES (1, 1, N'MASTER-ACCOUNT-0001', N'MASTER', '2026-01-01');
SET IDENTITY_INSERT brokerage_accounts OFF;

-- Custodies iniciais (MASTER)
SET IDENTITY_INSERT custodies ON;
INSERT INTO custodies (id, brokerage_account_id, ticker, quantity, average_price, last_update)
VALUES
    (1, 1, N'PETR4', 0, 0.00, '2026-01-01'),
    (2, 1, N'VALE3', 0, 0.00, '2026-01-01'),
    (3, 1, N'ITUB4', 0, 0.00, '2026-01-01'),
    (4, 1, N'BBDC4', 0, 0.00, '2026-01-01'),
    (5, 1, N'WEGE3', 0, 0.00, '2026-01-01');
SET IDENTITY_INSERT custodies OFF;
