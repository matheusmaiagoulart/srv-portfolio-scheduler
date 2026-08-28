package com.matheus.srv_portfolio_scheduler.fixtures;

import com.matheus.srv_portfolio_scheduler.domain.entities.PortfolioItem;
import com.matheus.srv_portfolio_scheduler.domain.entities.RecommendedPortfolio;

import java.time.OffsetDateTime;
import java.util.List;

public final class RecommendedPortfolioFixture {

    private String name = "Test Portfolio";
    private List<PortfolioItem> items = defaultItems();
    private OffsetDateTime terminationDate;

    private RecommendedPortfolioFixture() {
    }

    public static RecommendedPortfolioFixture aRecommendedPortfolio() {
        return new RecommendedPortfolioFixture();
    }

    public RecommendedPortfolioFixture withName(String name) {
        this.name = name;
        return this;
    }

    public RecommendedPortfolioFixture withItems(List<PortfolioItem> items) {
        this.items = items;
        return this;
    }

    public RecommendedPortfolioFixture terminatingAt(OffsetDateTime terminationDate) {
        this.terminationDate = terminationDate;
        return this;
    }

    public RecommendedPortfolio build() {
        return RecommendedPortfolio.create(name, items, terminationDate);
    }

    public static List<PortfolioItem> defaultItems() {
        return List.of(
                PortfolioItem.create("PETR4", 20),
                PortfolioItem.create("VALE3", 20),
                PortfolioItem.create("ITUB4", 20),
                PortfolioItem.create("BBDC4", 20),
                PortfolioItem.create("WEGE3", 20));
    }
}
