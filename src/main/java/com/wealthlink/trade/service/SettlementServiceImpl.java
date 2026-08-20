package com.wealthlink.trade.service;

import com.wealthlink.common.exception.ResourceNotFoundException;

import com.wealthlink.ledger.dto.CreateJournalEntryRequest;
import com.wealthlink.ledger.dto.CreateJournalRequest;
import com.wealthlink.ledger.entity.JournalEntryDirection;
import com.wealthlink.ledger.entity.LedgerAccount;
import com.wealthlink.ledger.entity.LedgerAccountType;
import com.wealthlink.ledger.repository.LedgerAccountRepository;
import com.wealthlink.ledger.service.JournalService;
import com.wealthlink.portfolio.service.PositionService;
import com.wealthlink.reference.repository.CurrencyRepository;
import com.wealthlink.trade.dto.CreateSettlementRequest;
import com.wealthlink.trade.dto.RetrySettlementResponse;
import com.wealthlink.trade.dto.SettlementResponse;
import com.wealthlink.trade.entity.TradeOrderType;
import com.wealthlink.trade.entity.*;
import com.wealthlink.trade.repository.SettlementRepository;
import com.wealthlink.trade.repository.TradeExecutionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Arrays;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class SettlementServiceImpl implements SettlementService {

    private final SettlementRepository settlementRepository;
    private final TradeExecutionRepository tradeExecutionRepository;
    private final CurrencyRepository currencyRepository;
    private final JournalService journalService;
    private final LedgerAccountRepository ledgerAccountRepository;
    private final PositionService positionService;

    @Transactional
    public SettlementResponse createSettlement(UUID executionId, CreateSettlementRequest request) {
        TradeExecution execution = tradeExecutionRepository.findById(executionId)
                .orElseThrow(() -> new ResourceNotFoundException("TradeExecution", executionId));
                
        // Settlement check removed; in real world, check if a Settlement entity already exists for this executionId.

        Settlement settlement = new Settlement();
        settlement.setTradeExecution(execution);
        settlement.setSettlementReference(UUID.randomUUID().toString());
        settlement.setStatus(SettlementStatus.SETTLED); // immediate settlement for now
        settlement.setSettlementDate(request.getSettlementDate());
        settlement.setSettledAt(Instant.now());
        settlement.setCurrency(currencyRepository.findById(request.getCurrencyId())
                .orElseThrow(() -> new ResourceNotFoundException("Currency", request.getCurrencyId())));
        settlement.setSettledAmount(request.getSettlementAmount());
        
        Settlement savedSettlement = settlementRepository.save(settlement);

        // Generate Ledger Journal using Double-Entry
        generateSettlementJournal(execution, savedSettlement);
        
        // Update Position
        positionService.updatePositionFromExecution(execution);

        return mapToResponse(savedSettlement);
    }

    @Override
    @Transactional
    public RetrySettlementResponse retrySettlement(UUID settlementId) {
        Settlement settlement = settlementRepository.findById(settlementId)
                .orElseThrow(() -> new ResourceNotFoundException("Settlement", settlementId));
        
        if (settlement.getStatus() == SettlementStatus.SETTLED) {
            throw new IllegalStateException("Settlement is already SETTLED.");
        }
        
        settlement.setStatus(SettlementStatus.SETTLED);
        settlement.setSettledAt(Instant.now());
        
        generateSettlementJournal(settlement.getTradeExecution(), settlement);
        positionService.updatePositionFromExecution(settlement.getTradeExecution());
        
        Settlement saved = settlementRepository.save(settlement);
        return RetrySettlementResponse.builder()
                .settlementId(saved.getId())
                .settlementStatus(saved.getStatus().name())
                .build();
    }

    private void generateSettlementJournal(TradeExecution execution, Settlement settlement) {
        TradeOrder order = execution.getTradeOrder();
        
        // Fetch Cash Account for this client
        LedgerAccount cashAccount = ledgerAccountRepository.findByAccountIdAndLedgerAccountTypeAndCurrencyId(
                order.getPortfolio().getAccount().getId(), LedgerAccountType.CASH, execution.getCurrency().getId()
        ).orElseThrow(() -> new ResourceNotFoundException("Ledger Account", order.getPortfolio().getAccount().getId()));
        
        // Fetch Position Account for this portfolio
        LedgerAccount positionAccount = ledgerAccountRepository.findByPortfolioIdAndLedgerAccountTypeAndCurrencyId(
                order.getPortfolio().getId(), LedgerAccountType.POSITION, execution.getCurrency().getId()
        ).orElseThrow(() -> new ResourceNotFoundException("Ledger Account", order.getPortfolio().getId()));

        CreateJournalEntryRequest debit = new CreateJournalEntryRequest();
        debit.setCurrencyId(execution.getCurrency().getId());
        debit.setAmount(settlement.getSettledAmount());
        debit.setDescription("Settlement " + settlement.getSettlementReference());

        CreateJournalEntryRequest credit = new CreateJournalEntryRequest();
        credit.setCurrencyId(execution.getCurrency().getId());
        credit.setAmount(settlement.getSettledAmount());
        credit.setDescription("Settlement " + settlement.getSettlementReference());

        // For BUY: We increase position (DEBIT) and decrease cash (CREDIT).
        // For SELL: We decrease position (CREDIT) and increase cash (DEBIT).
        if (TradeOrderType.BUY.name().equals(order.getOrderType().name())) {
            debit.setLedgerAccountId(positionAccount.getId());
            debit.setDirection(JournalEntryDirection.DEBIT.name());
            
            credit.setLedgerAccountId(cashAccount.getId());
            credit.setDirection(JournalEntryDirection.CREDIT.name());
        } else {
            debit.setLedgerAccountId(cashAccount.getId());
            debit.setDirection(JournalEntryDirection.DEBIT.name());
            
            credit.setLedgerAccountId(positionAccount.getId());
            credit.setDirection(JournalEntryDirection.CREDIT.name());
        }

        CreateJournalRequest journalReq = new CreateJournalRequest();
        journalReq.setDescription("Settlement for Trade " + order.getId());
        journalReq.setJournalType("TRADE");
        journalReq.setReferenceType("SETTLEMENT");
        journalReq.setReferenceId(settlement.getId());
        journalReq.setEntries(Arrays.asList(debit, credit));

        journalService.createJournal(journalReq);
    }

    @Override
    @Transactional(readOnly = true)
    public SettlementResponse getSettlementById(UUID settlementId) {
        Settlement settlement = settlementRepository.findById(settlementId)
                .orElseThrow(() -> new com.wealthlink.common.exception.ResourceNotFoundException("Settlement", settlementId));
        return mapToResponse(settlement);
    }

    private SettlementResponse mapToResponse(Settlement settlement) {
        return SettlementResponse.builder()
                .id(settlement.getId())
                .tradeExecutionId(settlement.getTradeExecution().getId())
                .settlementDate(settlement.getSettlementDate())
                .settlementAmount(settlement.getSettledAmount())
                .currency(settlement.getCurrency().getIsoCode())
                .settlementStatus(settlement.getStatus().name())
                .build();
    }
}
