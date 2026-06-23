package com.matheus.srv_portfolio_scheduler.adapters.utils;

import org.slf4j.MDC;

import java.util.UUID;

public class CorrelationId {

    private static final String CORRELATION_ID = "correlationId";

    public static String generate() {
        String correlationId = UUID.randomUUID().toString();
        MDC.put(CORRELATION_ID, correlationId);
        return correlationId;
    }

    public static String get() {
        return MDC.get(CORRELATION_ID);
    }

    public static void clear() {
        MDC.clear();
    }

}
