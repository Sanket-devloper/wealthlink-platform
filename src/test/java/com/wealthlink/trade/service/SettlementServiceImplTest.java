package com.wealthlink.trade.service;

import com.wealthlink.account.entity.Account;
import com.wealthlink.fund.entity.FundShareClass;
import com.wealthlink.ledger.dto.CreateJournalRequest;
import com.wealthlink.ledger.entity.LedgerAccount;
import com.wealthlink.ledger.entity.LedgerAccountStatus;
import com.wealthlink.ledger.entity.LedgerAccountType;
import com.wealthlink.ledger.repository.LedgerAccountRepository;
import com.wealthlink.ledger.service.JournalService;
import com.wealthlink.portfolio.entity.Portfolio;
import com.wealthlink.portfolio.service.PositionService;
import com.wealthlink.reference.entity.Currency;
import com.wealthlink.reference.repository.CurrencyRepository;
import com.wealthlink.trade.dto.CreateSettlementRequest;
import com.wealthlink.trade.dto.RetrySettlementResponse;
import com.wealthlink.trade.dto.SettlementResponse;
import com.wealthlink.trade.entity.*;
import com.wealthlink.trade.repository.SettlementRepository;
import com.wealthlink.trade.repository.TradeExecutionRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Unit tests for {@link SettlementServiceImpl}.
 * All collaborators are mocked — no Spring context or database required.
 */
@ExtendWith(MockitoExtension.class)
class SettlementServiceImplTest {

    @Mock SettlementRepository settlementRepository;
    @Mock TradeExecutionRepository tradeExecutionRepository;
    @Mock CurrencyRepository currencyRepository;
    @Mock JournalService journalService;
    @Mock LedgerAccountRepository ledgerAccountRepository;
    @Mock PositionService positionService;

    @InjectMocks SettlementServiceImpl service;

    // -------------------------------------------------------------------------
    // Helpers
    // -------------------------------------------------------------------------

    private Currency usd(UUID id) {
        Currency c = new Currency();
        c.setId(id);
        c.setIsoCode("USD");
        return c;
    }

    private LedgerAccount ledgerAccount(UUID id, LedgerAccountType type, UUID currencyId) {
        return LedgerAccount.builder()
                .id(id)
                .accountCode(type.name() + "-001")
                .accountName(type.name() + " Account")
                .ledgerAccountType(type)
                .status(LedgerAccountStatus.ACTIVE)
                .currency(usd(currencyId))
                .balance(BigDecimal.ZERO)
                .build();
    }

    private TradeExecution buildExecution(TradeOrderType orderType, UUID currencyId) {
        UUID accountId = UUID.randomUUID();
        UUID portfolioId = UUID.randomUUID();

        Account account = new Account();
        account.setId(accountId);

        Portfolio portfolio = new Portfolio();
        portfolio.setId(portfolioId);
        portfolio.setAccount(account);

        FundShareClass fsc = new FundShareClass();
        fsc.setId(UUID.randomUUID());

        TradeOrder order = new TradeOrder();
        order.setId(UUID.randomUUID());
        order.setOrderType(orderType);
        order.setPortfolio(portfolio);
        order.setFundShareClass(fsc);
        order.setRequestedQuantity(BigDecimal.valueOf(100));
        order.setStatus(TradeOrderStatus.FILLED);

        return TradeExecution.builder()
                .id(UUID.randomUUID())
                .tradeOrder(order)
                .executionReference("EXEC-001")
                .status(TradeExecutionStatus.CONFIRMED)
                .tradeDate(LocalDate.now())
                .executedQuantity(BigDecimal.valueOf(100))
                .executionPrice(BigDecimal.valueOf(10))
                .grossAmount(BigDecimal.valueOf(1000))
                .netAmount(BigDecimal.valueOf(1000))
                .currency(usd(currencyId))
                .executedAt(Instant.now())
                .build();
    }

    private CreateSettlementRequest settlementRequest(UUID currencyId) {
        CreateSettlementRequest req = new CreateSettlementRequest();
        req.setSettlementDate(LocalDate.now());
        req.setSettlementAmount(BigDecimal.valueOf(1000));
        req.setCurrencyId(currencyId);
        return req;
    }

    // -------------------------------------------------------------------------
    // createSettlement — orchestration
    // -------------------------------------------------------------------------

    @Test
    void createSettlement_savesSettlement_andCallsJournalAndPositionService() {
        UUID currencyId = UUID.randomUUID();
        TradeExecution execution = buildExecution(TradeOrderType.BUY, currencyId);
        UUID executionId = execution.getId();
        UUID accountId = execution.getTradeOrder().getPortfolio().getAccount().getId();
        UUID portfolioId = execution.getTradeOrder().getPortfolio().getId();

        when(tradeExecutionRepository.findById(executionId)).thenReturn(Optional.of(execution));
        when(currencyRepository.findById(currencyId)).thenReturn(Optional.of(usd(currencyId)));
        when(settlementRepository.save(any(Settlement.class))).thenAnswer(inv -> {
            Settlement s = inv.getArgument(0);
            s.setId(UUID.randomUUID());
            return s;
        });

        UUID cashAccountId = UUID.randomUUID();
        UUID positionAccountId = UUID.randomUUID();
        when(ledgerAccountRepository.findByAccountIdAndLedgerAccountTypeAndCurrencyId(accountId, LedgerAccountType.CASH, currencyId))
                .thenReturn(Optional.of(ledgerAccount(cashAccountId, LedgerAccountType.CASH, currencyId)));
        when(ledgerAccountRepository.findByPortfolioIdAndLedgerAccountTypeAndCurrencyId(portfolioId, LedgerAccountType.POSITION, currencyId))
                .thenReturn(Optional.of(ledgerAccount(positionAccountId, LedgerAccountType.POSITION, currencyId)));

        CreateSettlementRequest req = settlementRequest(currencyId);
        SettlementResponse response = service.createSettlement(executionId, req);

        assertThat(response).isNotNull();
        assertThat(response.getSettlementStatus()).isEqualTo("SETTLED");

        verify(journalService, times(1)).createJournal(any(CreateJournalRequest.class));
        verify(positionService, times(1)).updatePositionFromExecution(execution);
    }

    // -------------------------------------------------------------------------
    // createSettlement — BUY journal (DEBIT position, CREDIT cash)
    // -------------------------------------------------------------------------

    @Test
    void createSettlement_generatesCorrectJournalEntries_forBuyOrder() {
        UUID currencyId = UUID.randomUUID();
        TradeExecution execution = buildExecution(TradeOrderType.BUY, currencyId);
        UUID executionId = execution.getId();
        UUID accountId = execution.getTradeOrder().getPortfolio().getAccount().getId();
        UUID portfolioId = execution.getTradeOrder().getPortfolio().getId();

        UUID cashAccountId = UUID.randomUUID();
        UUID positionAccountId = UUID.randomUUID();

        when(tradeExecutionRepository.findById(executionId)).thenReturn(Optional.of(execution));
        when(currencyRepository.findById(currencyId)).thenReturn(Optional.of(usd(currencyId)));
        when(settlementRepository.save(any(Settlement.class))).thenAnswer(inv -> {
            Settlement s = inv.getArgument(0);
            s.setId(UUID.randomUUID());
            return s;
        });
        when(ledgerAccountRepository.findByAccountIdAndLedgerAccountTypeAndCurrencyId(accountId, LedgerAccountType.CASH, currencyId))
                .thenReturn(Optional.of(ledgerAccount(cashAccountId, LedgerAccountType.CASH, currencyId)));
        when(ledgerAccountRepository.findByPortfolioIdAndLedgerAccountTypeAndCurrencyId(portfolioId, LedgerAccountType.POSITION, currencyId))
                .thenReturn(Optional.of(ledgerAccount(positionAccountId, LedgerAccountType.POSITION, currencyId)));

        ArgumentCaptor<CreateJournalRequest> journalCaptor = ArgumentCaptor.forClass(CreateJournalRequest.class);
        when(journalService.createJournal(journalCaptor.capture())).thenReturn(null);

        service.createSettlement(executionId, settlementRequest(currencyId));

        CreateJournalRequest journal = journalCaptor.getValue();
        assertThat(journal.getEntries()).hasSize(2);

        // BUY: DEBIT position account, CREDIT cash account
        boolean hasPositionDebit = journal.getEntries().stream()
                .anyMatch(e -> e.getLedgerAccountId().equals(positionAccountId) && "DEBIT".equals(e.getDirection()));
        boolean hasCashCredit = journal.getEntries().stream()
                .anyMatch(e -> e.getLedgerAccountId().equals(cashAccountId) && "CREDIT".equals(e.getDirection()));

        assertThat(hasPositionDebit).as("BUY should DEBIT the position account").isTrue();
        assertThat(hasCashCredit).as("BUY should CREDIT the cash account").isTrue();
    }

    // -------------------------------------------------------------------------
    // createSettlement — SELL journal (DEBIT cash, CREDIT position)
    // -------------------------------------------------------------------------

    @Test
    void createSettlement_generatesCorrectJournalEntries_forSellOrder() {
        UUID currencyId = UUID.randomUUID();
        TradeExecution execution = buildExecution(TradeOrderType.SELL, currencyId);
        UUID executionId = execution.getId();
        UUID accountId = execution.getTradeOrder().getPortfolio().getAccount().getId();
        UUID portfolioId = execution.getTradeOrder().getPortfolio().getId();

        UUID cashAccountId = UUID.randomUUID();
        UUID positionAccountId = UUID.randomUUID();

        when(tradeExecutionRepository.findById(executionId)).thenReturn(Optional.of(execution));
        when(currencyRepository.findById(currencyId)).thenReturn(Optional.of(usd(currencyId)));
        when(settlementRepository.save(any(Settlement.class))).thenAnswer(inv -> {
            Settlement s = inv.getArgument(0);
            s.setId(UUID.randomUUID());
            return s;
        });
        when(ledgerAccountRepository.findByAccountIdAndLedgerAccountTypeAndCurrencyId(accountId, LedgerAccountType.CASH, currencyId))
                .thenReturn(Optional.of(ledgerAccount(cashAccountId, LedgerAccountType.CASH, currencyId)));
        when(ledgerAccountRepository.findByPortfolioIdAndLedgerAccountTypeAndCurrencyId(portfolioId, LedgerAccountType.POSITION, currencyId))
                .thenReturn(Optional.of(ledgerAccount(positionAccountId, LedgerAccountType.POSITION, currencyId)));

        ArgumentCaptor<CreateJournalRequest> journalCaptor = ArgumentCaptor.forClass(CreateJournalRequest.class);
        when(journalService.createJournal(journalCaptor.capture())).thenReturn(null);

        service.createSettlement(executionId, settlementRequest(currencyId));

        CreateJournalRequest journal = journalCaptor.getValue();

        // SELL: DEBIT cash account, CREDIT position account
        boolean hasCashDebit = journal.getEntries().stream()
                .anyMatch(e -> e.getLedgerAccountId().equals(cashAccountId) && "DEBIT".equals(e.getDirection()));
        boolean hasPositionCredit = journal.getEntries().stream()
                .anyMatch(e -> e.getLedgerAccountId().equals(positionAccountId) && "CREDIT".equals(e.getDirection()));

        assertThat(hasCashDebit).as("SELL should DEBIT the cash account").isTrue();
        assertThat(hasPositionCredit).as("SELL should CREDIT the position account").isTrue();
    }

    // -------------------------------------------------------------------------
    // retrySettlement
    // -------------------------------------------------------------------------

    @Test
    void retrySettlement_throwsIllegalState_whenSettlementAlreadySettled() {
        UUID settlementId = UUID.randomUUID();

        Settlement settlement = new Settlement();
        settlement.setId(settlementId);
        settlement.setStatus(SettlementStatus.SETTLED);

        when(settlementRepository.findById(settlementId)).thenReturn(Optional.of(settlement));

        assertThatThrownBy(() -> service.retrySettlement(settlementId))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("already SETTLED");
    }

    @Test
    void retrySettlement_settlesSettlement_andTriggersJournalAndPosition() {
        UUID currencyId = UUID.randomUUID();
        TradeExecution execution = buildExecution(TradeOrderType.BUY, currencyId);
        UUID accountId = execution.getTradeOrder().getPortfolio().getAccount().getId();
        UUID portfolioId = execution.getTradeOrder().getPortfolio().getId();

        UUID settlementId = UUID.randomUUID();
        Settlement settlement = new Settlement();
        settlement.setId(settlementId);
        settlement.setStatus(SettlementStatus.PENDING);
        settlement.setTradeExecution(execution);
        settlement.setSettledAmount(BigDecimal.valueOf(1000));
        settlement.setSettlementReference("SETTLE-001");
        settlement.setCurrency(usd(currencyId));

        UUID cashAccountId = UUID.randomUUID();
        UUID positionAccountId = UUID.randomUUID();

        when(settlementRepository.findById(settlementId)).thenReturn(Optional.of(settlement));
        when(settlementRepository.save(any(Settlement.class))).thenAnswer(inv -> inv.getArgument(0));
        when(ledgerAccountRepository.findByAccountIdAndLedgerAccountTypeAndCurrencyId(accountId, LedgerAccountType.CASH, currencyId))
                .thenReturn(Optional.of(ledgerAccount(cashAccountId, LedgerAccountType.CASH, currencyId)));
        when(ledgerAccountRepository.findByPortfolioIdAndLedgerAccountTypeAndCurrencyId(portfolioId, LedgerAccountType.POSITION, currencyId))
                .thenReturn(Optional.of(ledgerAccount(positionAccountId, LedgerAccountType.POSITION, currencyId)));

        RetrySettlementResponse response = service.retrySettlement(settlementId);

        assertThat(response.getSettlementId()).isEqualTo(settlementId);
        assertThat(response.getSettlementStatus()).isEqualTo("SETTLED");

        verify(journalService, times(1)).createJournal(any(CreateJournalRequest.class));
        verify(positionService, times(1)).updatePositionFromExecution(execution);
    }
}
