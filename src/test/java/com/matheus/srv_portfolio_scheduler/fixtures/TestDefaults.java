package com.matheus.srv_portfolio_scheduler.fixtures;

import com.matheus.srv_portfolio_scheduler.domain.valueObject.Money;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;

public final class TestDefaults {

    public static final long CUSTOMER_ID = 1L;
    public static final long BROKERAGE_ACCOUNT_ID = 10L;
    public static final long PURCHASE_ORDER_ID = 100L;
    public static final String CUSTOMER_NAME = "Test Customer";
    public static final String CPF = "10230456077";
    public static final String EMAIL = "customer@test.com";
    public static final String ACCOUNT_NUMBER = "TEST-ACCOUNT-001";
    public static final String TICKER = "PETR4";
    public static final int QUANTITY = 10;
    public static final BigDecimal MONTHLY_AMOUNT = new BigDecimal("1000.00");
    public static final BigDecimal UNIT_PRICE = new BigDecimal("49.41");
    public static final BigDecimal THIRD_PARTY_BALANCE = new BigDecimal("1000.00");
    public static final OffsetDateTime OFFSET_DATE_TIME =
            OffsetDateTime.of(2026, 1, 15, 10, 0, 0, 0, ZoneOffset.UTC);
    public static final LocalDateTime LOCAL_DATE_TIME =
            LocalDateTime.of(2026, 1, 15, 10, 0);

    private TestDefaults() {
    }

    public static Money money(String amount) {
        return Money.create(new BigDecimal(amount));
    }
}