package com.matheus.srv_portfolio_scheduler.fixtures;

import com.matheus.srv_portfolio_scheduler.domain.entities.Delivery;
import com.matheus.srv_portfolio_scheduler.domain.valueObject.Money;

public final class DeliveryFixture {

    private long purchaseOrderId = TestDefaults.PURCHASE_ORDER_ID;
    private long brokerageAccountId = TestDefaults.BROKERAGE_ACCOUNT_ID;
    private String ticker = TestDefaults.TICKER;
    private int quantity = TestDefaults.QUANTITY;
    private Money unitPrice = Money.create(TestDefaults.UNIT_PRICE);

    private DeliveryFixture() {
    }

    public static DeliveryFixture aDelivery() {
        return new DeliveryFixture();
    }

    public DeliveryFixture forPurchaseOrder(long purchaseOrderId) {
        this.purchaseOrderId = purchaseOrderId;
        return this;
    }

    public DeliveryFixture forBrokerageAccount(long brokerageAccountId) {
        this.brokerageAccountId = brokerageAccountId;
        return this;
    }

    public DeliveryFixture withTicker(String ticker) {
        this.ticker = ticker;
        return this;
    }

    public DeliveryFixture withQuantity(int quantity) {
        this.quantity = quantity;
        return this;
    }

    public DeliveryFixture withUnitPrice(String unitPrice) {
        this.unitPrice = TestDefaults.money(unitPrice);
        return this;
    }

    public Delivery build() {
        return Delivery.createDelivery(
                purchaseOrderId,
                brokerageAccountId,
                ticker,
                quantity,
                unitPrice);
    }
}