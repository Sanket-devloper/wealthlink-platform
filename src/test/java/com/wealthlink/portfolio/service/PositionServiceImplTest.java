package com.wealthlink.portfolio.service;

import com.wealthlink.fund.entity.FundShareClass;
import com.wealthlink.ledger.repository.JournalEntryRepository;
import com.wealthlink.ledger.repository.LedgerAccountRepository;
import com.wealthlink.portfolio.entity.Portfolio;
import com.wealthlink.portfolio.entity.Position;
import com.wealthlink.portfolio.entity.PositionStatus;
import com.wealthlink.portfolio.repository.PositionRepository;
import com.wealthlink.reference.entity.Currency;
import com.wealthlink.trade.entity.TradeExecution;
import com.wealthlink.trade.entity.TradeOrder;
import com.wealthlink.trade.entity.TradeOrderType;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Unit tests for {@link PositionServiceImpl}.
 * All collaborators are mocked — no Spring context or database required.
 */
@ExtendWith(MockitoExtension.class)
class PositionServiceImplTest {

    @Mock PositionRepository positionRepository;
    @Mock LedgerAccountRepository ledgerAccountRepository;
    @Mock JournalEntryRepository journalEntryRepository;

    @InjectMocks PositionServiceImpl service;

    // -------------------------------------------------------------------------
    // Helpers
    // -------------------------------------------------------------------------

    private Currency usd() {
        Currency c = new Currency();
        c.setId(UUID.randomUUID());
        c.setIsoCode("USD");
        return c;
    }

    private FundShareClass fundShareClass(UUID id) {
        FundShareClass fsc = new FundShareClass();
        fsc.setId(id);
        return fsc;
    }

    private Portfolio portfolio(UUID id) {
        Portfolio p = new Portfolio();
        p.setId(id);
        return p;
    }

    private TradeExecution execution(TradeOrderType type, BigDecimal qty, BigDecimal price) {
        UUID portfolioId = UUID.randomUUID();
        UUID fundShareClassId = UUID.randomUUID();
        Currency currency = usd();

        TradeOrder order = new TradeOrder();
        order.setId(UUID.randomUUID());
        order.setOrderType(type);
        order.setPortfolio(portfolio(portfolioId));
        order.setFundShareClass(fundShareClass(fundShareClassId));
        order.setRequestedQuantity(qty);

        return TradeExecution.builder()
                .id(UUID.randomUUID())
                .tradeOrder(order)
                .executedQuantity(qty)
                .executionPrice(price)
                .tradeDate(LocalDate.now())
                .currency(currency)
                .build();
    }

    // -------------------------------------------------------------------------
    // updatePositionFromExecution — BUY
    // -------------------------------------------------------------------------

    @Test
    void updatePositionFromExecution_createsNewPosition_forBuyOrder_whenNoneExists() {
        TradeExecution exec = execution(TradeOrderType.BUY, BigDecimal.valueOf(100), BigDecimal.valueOf(10));
        TradeOrder order = exec.getTradeOrder();

        when(positionRepository.findByPortfolioIdAndFundShareClassIdAndPositionDate(
                order.getPortfolio().getId(),
                order.getFundShareClass().getId(),
                exec.getTradeDate()
        )).thenReturn(Optional.empty());

        ArgumentCaptor<Position> captor = ArgumentCaptor.forClass(Position.class);
        when(positionRepository.save(captor.capture())).thenAnswer(inv -> inv.getArgument(0));

        service.updatePositionFromExecution(exec);

        Position saved = captor.getValue();
        assertThat(saved.getQuantity()).isEqualByComparingTo(BigDecimal.valueOf(100));
        assertThat(saved.getStatus()).isEqualTo(PositionStatus.OPEN);
    }

    @Test
    void updatePositionFromExecution_addsQuantity_forBuyOrder_whenPositionExists() {
        TradeExecution exec = execution(TradeOrderType.BUY, BigDecimal.valueOf(50), BigDecimal.valueOf(12));
        TradeOrder order = exec.getTradeOrder();

        Position existing = new Position();
        existing.setId(UUID.randomUUID());
        existing.setPortfolio(order.getPortfolio());
        existing.setFundShareClass(order.getFundShareClass());
        existing.setPositionDate(exec.getTradeDate());
        existing.setQuantity(BigDecimal.valueOf(100));
        existing.setAverageCost(BigDecimal.valueOf(10));
        existing.setCostBasisCurrency(exec.getCurrency());
        existing.setMarketValue(BigDecimal.valueOf(1000));
        existing.setCurrency(exec.getCurrency());
        existing.setStatus(PositionStatus.OPEN);

        when(positionRepository.findByPortfolioIdAndFundShareClassIdAndPositionDate(
                order.getPortfolio().getId(),
                order.getFundShareClass().getId(),
                exec.getTradeDate()
        )).thenReturn(Optional.of(existing));

        ArgumentCaptor<Position> captor = ArgumentCaptor.forClass(Position.class);
        when(positionRepository.save(captor.capture())).thenAnswer(inv -> inv.getArgument(0));

        service.updatePositionFromExecution(exec);

        Position saved = captor.getValue();
        assertThat(saved.getQuantity()).isEqualByComparingTo(BigDecimal.valueOf(150)); // 100 + 50
        assertThat(saved.getStatus()).isEqualTo(PositionStatus.OPEN);
    }

    // -------------------------------------------------------------------------
    // updatePositionFromExecution — SELL
    // -------------------------------------------------------------------------

    @Test
    void updatePositionFromExecution_reducesQuantity_forSellOrder() {
        TradeExecution exec = execution(TradeOrderType.SELL, BigDecimal.valueOf(30), BigDecimal.valueOf(15));
        TradeOrder order = exec.getTradeOrder();

        Position existing = new Position();
        existing.setId(UUID.randomUUID());
        existing.setPortfolio(order.getPortfolio());
        existing.setFundShareClass(order.getFundShareClass());
        existing.setPositionDate(exec.getTradeDate());
        existing.setQuantity(BigDecimal.valueOf(100));
        existing.setAverageCost(BigDecimal.valueOf(10));
        existing.setCostBasisCurrency(exec.getCurrency());
        existing.setMarketValue(BigDecimal.valueOf(1000));
        existing.setCurrency(exec.getCurrency());
        existing.setStatus(PositionStatus.OPEN);

        when(positionRepository.findByPortfolioIdAndFundShareClassIdAndPositionDate(
                order.getPortfolio().getId(),
                order.getFundShareClass().getId(),
                exec.getTradeDate()
        )).thenReturn(Optional.of(existing));

        ArgumentCaptor<Position> captor = ArgumentCaptor.forClass(Position.class);
        when(positionRepository.save(captor.capture())).thenAnswer(inv -> inv.getArgument(0));

        service.updatePositionFromExecution(exec);

        Position saved = captor.getValue();
        assertThat(saved.getQuantity()).isEqualByComparingTo(BigDecimal.valueOf(70)); // 100 - 30
        assertThat(saved.getStatus()).isEqualTo(PositionStatus.OPEN);
    }

    @Test
    void updatePositionFromExecution_marksPositionClosed_whenQuantityReachesZero() {
        TradeExecution exec = execution(TradeOrderType.SELL, BigDecimal.valueOf(100), BigDecimal.valueOf(15));
        TradeOrder order = exec.getTradeOrder();

        Position existing = new Position();
        existing.setId(UUID.randomUUID());
        existing.setPortfolio(order.getPortfolio());
        existing.setFundShareClass(order.getFundShareClass());
        existing.setPositionDate(exec.getTradeDate());
        existing.setQuantity(BigDecimal.valueOf(100));
        existing.setAverageCost(BigDecimal.valueOf(10));
        existing.setCostBasisCurrency(exec.getCurrency());
        existing.setMarketValue(BigDecimal.valueOf(1000));
        existing.setCurrency(exec.getCurrency());
        existing.setStatus(PositionStatus.OPEN);

        when(positionRepository.findByPortfolioIdAndFundShareClassIdAndPositionDate(
                order.getPortfolio().getId(),
                order.getFundShareClass().getId(),
                exec.getTradeDate()
        )).thenReturn(Optional.of(existing));

        ArgumentCaptor<Position> captor = ArgumentCaptor.forClass(Position.class);
        when(positionRepository.save(captor.capture())).thenAnswer(inv -> inv.getArgument(0));

        service.updatePositionFromExecution(exec);

        Position saved = captor.getValue();
        assertThat(saved.getQuantity()).isEqualByComparingTo(BigDecimal.ZERO);
        assertThat(saved.getStatus()).isEqualTo(PositionStatus.CLOSED);
    }
}
