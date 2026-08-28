package com.matheus.srv_portfolio_scheduler.src.domain.entities;

import com.matheus.srv_portfolio_scheduler.domain.entities.BrokerageAccount;
import com.matheus.srv_portfolio_scheduler.domain.entities.Custody;
import com.matheus.srv_portfolio_scheduler.domain.entities.Customer;
import com.matheus.srv_portfolio_scheduler.domain.entities.PortfolioItem;
import com.matheus.srv_portfolio_scheduler.domain.enums.BrokerageAccountType;
import com.matheus.srv_portfolio_scheduler.domain.exceptions.BusinessException;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;

class BrokerageAccountTest {

    @Test
    void deveCriarContaParaCliente() {
        Customer customer = mock(Customer.class);

        BrokerageAccount account = BrokerageAccount.create(customer);

        assertNotNull(account.getAccountNumber());
        assertEquals(BrokerageAccountType.CLIENT, account.getAccountType());
        assertNotNull(account.getCreatedAt());
        assertTrue(account.getCustodies().isEmpty());
    }

    @Test
    void deveGerarNumeroDeContaUnico() {
        Customer customer = mock(Customer.class);

        BrokerageAccount account1 = BrokerageAccount.create(customer);
        BrokerageAccount account2 = BrokerageAccount.create(customer);

        assertNotEquals(account1.getAccountNumber(), account2.getAccountNumber());
    }

    @Test
    void deveCriarCustodiasIniciaisComCincoItens() {
        Customer customer = mock(Customer.class);
        BrokerageAccount account = BrokerageAccount.create(customer);

        List<PortfolioItem> items = List.of(
                PortfolioItem.create("PETR4", 30),
                PortfolioItem.create("VALE3", 25),
                PortfolioItem.create("ITUB4", 20),
                PortfolioItem.create("BBDC4", 15),
                PortfolioItem.create("WEGE3", 10));

        account.createInitialCustodies(items);

        assertEquals(5, account.getCustodies().size());
        assertTrue(account.getCustodies().stream().anyMatch(c -> c.getTicker().equals("PETR4")));
        assertTrue(account.getCustodies().stream().anyMatch(c -> c.getTicker().equals("VALE3")));
    }

    @Test
    void deveLancarExcecaoQuandoItensNulo() {
        Customer customer = mock(Customer.class);
        BrokerageAccount account = BrokerageAccount.create(customer);

        BusinessException ex = assertThrows(BusinessException.class,
                () -> account.createInitialCustodies(null));

        assertEquals("INVALID_PORTFOLIO_ITEMS", ex.getErrorCode());
    }

    @Test
    void deveLancarExcecaoQuandoItensMenorQueCinco() {
        Customer customer = mock(Customer.class);
        BrokerageAccount account = BrokerageAccount.create(customer);

        List<PortfolioItem> items = List.of(
                PortfolioItem.create("PETR4", 50),
                PortfolioItem.create("VALE3", 50));

        BusinessException ex = assertThrows(BusinessException.class,
                () -> account.createInitialCustodies(items));

        assertEquals("INVALID_PORTFOLIO_ITEMS", ex.getErrorCode());
    }

    @Test
    void deveLancarExcecaoQuandoItensMaiorQueCinco() {
        Customer customer = mock(Customer.class);
        BrokerageAccount account = BrokerageAccount.create(customer);

        List<PortfolioItem> items = List.of(
                PortfolioItem.create("PETR4", 20),
                PortfolioItem.create("VALE3", 20),
                PortfolioItem.create("ITUB4", 15),
                PortfolioItem.create("BBDC4", 15),
                PortfolioItem.create("WEGE3", 15),
                PortfolioItem.create("ABEV3", 15));

        BusinessException ex = assertThrows(BusinessException.class,
                () -> account.createInitialCustodies(items));

        assertEquals("INVALID_PORTFOLIO_ITEMS", ex.getErrorCode());
    }

    @Test
    void deveReconstruirContaComCustodias() {
        List<Custody> custodies = new ArrayList<>();
        custodies.add(Custody.reconstruct(1L, null, "PETR4", 100, null, null));

        BrokerageAccount account = BrokerageAccount.reconstruct(
                1L, null, "ACC123", BrokerageAccountType.MASTER, null, custodies);

        assertEquals(1L, account.getId());
        assertEquals("ACC123", account.getAccountNumber());
        assertEquals(BrokerageAccountType.MASTER, account.getAccountType());
        assertEquals(1, account.getCustodies().size());
    }

    @Test
    void deveCriarCustodiasComQuantidadeZero() {
        Customer customer = mock(Customer.class);
        BrokerageAccount account = BrokerageAccount.create(customer);

        List<PortfolioItem> items = List.of(
                PortfolioItem.create("PETR4", 30),
                PortfolioItem.create("VALE3", 25),
                PortfolioItem.create("ITUB4", 20),
                PortfolioItem.create("BBDC4", 15),
                PortfolioItem.create("WEGE3", 10));

        account.createInitialCustodies(items);

        assertTrue(account.getCustodies().stream().allMatch(c -> c.getQuantity() == 0));
    }
}
