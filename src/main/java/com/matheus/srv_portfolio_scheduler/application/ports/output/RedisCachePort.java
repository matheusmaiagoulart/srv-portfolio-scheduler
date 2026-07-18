package com.matheus.srv_portfolio_scheduler.application.ports.output;

import java.util.Optional;

public interface RedisCachePort {

    <T> Optional<T> get(String key, Class<T> desserializationClass);

    void save(String key, Object value, int cacheDurationHours);
}
