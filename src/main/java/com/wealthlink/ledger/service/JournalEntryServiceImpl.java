package com.wealthlink.ledger.service;

import com.wealthlink.common.exception.ResourceNotFoundException;

import com.wealthlink.ledger.dto.JournalResponse;
import com.wealthlink.ledger.entity.JournalEntry;
import com.wealthlink.ledger.repository.JournalEntryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Transactional(readOnly = true)
@Service
@RequiredArgsConstructor
public class JournalEntryServiceImpl implements JournalEntryService {

    private final JournalEntryRepository journalEntryRepository;

    @Override
    @Transactional(readOnly = true)
    public List<JournalResponse.JournalEntryResponse> getAll() {
        return journalEntryRepository.findAll().stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public JournalResponse.JournalEntryResponse getById(UUID id) {
        JournalEntry entry = journalEntryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("JournalEntry", id));
        return mapToResponse(entry);
    }

    @Override
    @Transactional(readOnly = true)
    public List<JournalResponse.JournalEntryResponse> getEntriesByLedgerAccountId(UUID ledgerAccountId) {
        return journalEntryRepository.findByLedgerAccountId(ledgerAccountId).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<JournalResponse.JournalEntryResponse> getEntriesByJournalId(UUID journalId) {
        return journalEntryRepository.findByJournalId(journalId).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    private JournalResponse.JournalEntryResponse mapToResponse(JournalEntry entry) {
        return JournalResponse.JournalEntryResponse.builder()
                .ledgerAccountId(entry.getLedgerAccount().getId())
                .direction(entry.getDirection().name())
                .amount(entry.getAmount())
                .currency(entry.getCurrency().getIsoCode())
                .build();
    }
}
