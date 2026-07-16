package com.matheus.srv_portfolio_scheduler.domain.valueObject;

import com.matheus.srv_portfolio_scheduler.domain.exceptions.BusinessException;
import jakarta.persistence.Embeddable;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.math.RoundingMode;

@Getter
@Embeddable
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Money {

    private static final int SCALE = 2;
    private static final RoundingMode ROUNDING_MODE = RoundingMode.HALF_UP;

    private BigDecimal amount;

    private Money(BigDecimal amount) {
        this.amount = amount.setScale(SCALE, ROUNDING_MODE);
    }
    public static Money create(BigDecimal amount) {
        return new Money(amount);
    }

    public Money add(Money other) {
        if (other == null || other.getAmount().compareTo(BigDecimal.ZERO) < 0) {
            throw new BusinessException("INVALID_AMOUNT", "Amount must be greater than zero");
        }

        return new Money(this.amount.add(other.getAmount()));
    }
}
