package com.wealthlink.ledger.service;

import com.wealthlink.common.exception.ResourceNotFoundException;

import com.wealthlink.ledger.dto.CreateJournalEntryRequest;
import com.wealthlink.ledger.dto.CreateJournalRequest;
import com.wealthlink.ledger.dto.JournalResponse;
import com.wealthlink.ledger.dto.ReverseJournalRequest;
import com.wealthlink.ledger.dto.ReverseJournalResponse;
import com.wealthlink.ledger.entity.*;
import com.wealthlink.ledger.repository.JournalRepository;
import com.wealthlink.ledger.repository.LedgerAccountRepository;
import com.wealthlink.reference.entity.Currency;
import com.wealthlink.reference.repository.CurrencyRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class JournalServiceImpl implements JournalService {

    private final JournalRepository journalRepository;
    private final LedgerAccountRepository ledgerAccountRepository;
    private final CurrencyRepository currencyRepository;

    @Override
    @Transactional(readOnly = true)
    public List<JournalResponse> getAll() {
        return journalRepository.findAll().stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public JournalResponse getById(UUID id) {
        Journal journal = journalRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Journal", id));
        return mapToResponse(journal);
    }

    @Override
    @Transactional
    public JournalResponse createJournal(CreateJournalRequest request) {
        if (request.getIdempotencyKey() != null) {
            Optional<Journal> existing = journalRepository.findByIdempotencyKey(request.getIdempotencyKey());
            if (existing.isPresent()) {
                return mapToResponse(existing.get());
            }
        }

        validateDoubleEntry(request.getEntries());

        Journal journal = new Journal();
        journal.setJournalReference(UUID.randomUUID().toString());
        journal.setDescription(request.getDescription());
        journal.setStatus(JournalStatus.POSTED);
        journal.setJournalType(JournalType.valueOf(request.getJournalType()));
        journal.setJournalDate(request.getJournalDate() != null ? request.getJournalDate() : LocalDate.now());
        journal.setReferenceType(request.getReferenceType());
        journal.setReferenceId(request.getReferenceId());
        journal.setIdempotencyKey(request.getIdempotencyKey());

        List<JournalEntry> entries = new ArrayList<>();
        for (CreateJournalEntryRequest entryReq : request.getEntries()) {
            JournalEntry entry = new JournalEntry();
            entry.setJournal(journal);
            entry.setLedgerAccount(ledgerAccountRepository.findById(entryReq.getLedgerAccountId())
                    .orElseThrow(() -> new ResourceNotFoundException("LedgerAccount", entryReq.getLedgerAccountId())));
            entry.setDirection(JournalEntryDirection.valueOf(entryReq.getDirection()));
            entry.setAmount(entryReq.getAmount());
            entry.setCurrency(currencyRepository.findById(entryReq.getCurrencyId())
                    .orElseThrow(() -> new ResourceNotFoundException("Currency", entryReq.getCurrencyId())));
            entry.setDescription(entryReq.getDescription());
            entries.add(entry);
        }

        journal.setEntries(entries);
        Journal savedJournal = journalRepository.save(journal);
        return mapToResponse(savedJournal);
    }

    @Override
    @Transactional
    public ReverseJournalResponse reverseJournal(UUID id, ReverseJournalRequest request) {
        Journal original = journalRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Journal", id));

        if (original.getStatus() != JournalStatus.POSTED) {
            throw new IllegalStateException("Only POSTED journals can be reversed. Current status: " + original.getStatus());
        }

        // Build the reversal journal with inverted entry directions
        Journal reversal = new Journal();
        reversal.setJournalReference(UUID.randomUUID().toString());
        reversal.setDescription("Reversal of journal " + original.getJournalReference()
                + (request.getReason() != null ? " — " + request.getReason() : ""));
        reversal.setStatus(JournalStatus.POSTED);
        reversal.setJournalType(original.getJournalType());
        reversal.setJournalDate(LocalDate.now());
        reversal.setPostingDate(LocalDate.now());
        reversal.setReferenceType("REVERSAL");
        reversal.setReferenceId(original.getId());
        reversal.setReversedJournal(original);

        List<JournalEntry> reversalEntries = new ArrayList<>();
        for (JournalEntry originalEntry : original.getEntries()) {
            JournalEntry reversalEntry = new JournalEntry();
            reversalEntry.setJournal(reversal);
            reversalEntry.setLedgerAccount(originalEntry.getLedgerAccount());
            // Invert direction: DEBIT → CREDIT, CREDIT → DEBIT
            reversalEntry.setDirection(
                    originalEntry.getDirection() == JournalEntryDirection.DEBIT
                            ? JournalEntryDirection.CREDIT
                            : JournalEntryDirection.DEBIT
            );
            reversalEntry.setAmount(originalEntry.getAmount());
            reversalEntry.setCurrency(originalEntry.getCurrency());
            reversalEntry.setDescription("Reversal: " + originalEntry.getDescription());
            reversalEntries.add(reversalEntry);
        }
        reversal.setEntries(reversalEntries);

        Journal savedReversal = journalRepository.save(reversal);

        // Mark the original as REVERSED
        original.setStatus(JournalStatus.REVERSED);
        journalRepository.save(original);

        return ReverseJournalResponse.builder()
                .reversalJournalId(savedReversal.getId())
                .reversedJournalId(original.getId())
                .journalType(savedReversal.getJournalType().name())
                .status(savedReversal.getStatus().name())
                .postingDate(savedReversal.getPostingDate())
                .build();
    }

    // -------------------------------------------------------------------------
    // Private helpers
    // -------------------------------------------------------------------------

    private void validateDoubleEntry(List<CreateJournalEntryRequest> entries) {
        if (entries == null || entries.isEmpty()) {
            throw new IllegalArgumentException("Journal must have at least two entries");
        }

        Map<UUID, BigDecimal> currencyBalances = new HashMap<>();

        for (CreateJournalEntryRequest entry : entries) {
            if (entry.getAmount().compareTo(BigDecimal.ZERO) <= 0) {
                throw new IllegalArgumentException("Entry amount must be strictly positive");
            }

            BigDecimal current = currencyBalances.getOrDefault(entry.getCurrencyId(), BigDecimal.ZERO);
            if (JournalEntryDirection.DEBIT.name().equals(entry.getDirection())) {
                currencyBalances.put(entry.getCurrencyId(), current.add(entry.getAmount()));
            } else if (JournalEntryDirection.CREDIT.name().equals(entry.getDirection())) {
                currencyBalances.put(entry.getCurrencyId(), current.subtract(entry.getAmount()));
            } else {
                throw new IllegalArgumentException("Invalid direction: " + entry.getDirection());
            }
        }

        for (Map.Entry<UUID, BigDecimal> balanceEntry : currencyBalances.entrySet()) {
            if (balanceEntry.getValue().compareTo(BigDecimal.ZERO) != 0) {
                throw new IllegalArgumentException(
                        "Journal is unbalanced for currency ID: " + balanceEntry.getKey());
            }
        }
    }

    private JournalResponse mapToResponse(Journal journal) {
        return JournalResponse.builder()
                .id(journal.getId())
                .journalType(journal.getJournalType().name())
                .postingDate(journal.getPostingDate())
                .valueDate(journal.getValueDate())
                .status(journal.getStatus().name())
                .entries(journal.getEntries().stream().map(e ->
                        JournalResponse.JournalEntryResponse.builder()
                                .ledgerAccountId(e.getLedgerAccount().getId())
                                .direction(e.getDirection().name())
                                .amount(e.getAmount())
                                .currency(e.getCurrency().getIsoCode())
                                .build()
                ).collect(Collectors.toList()))
                .build();
    }
}
