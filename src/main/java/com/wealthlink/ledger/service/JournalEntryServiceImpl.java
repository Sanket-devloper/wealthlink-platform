package com.wealthlink.ledger.service;

import com.wealthlink.ledger.dto.JournalResponse;
import com.wealthlink.ledger.entity.JournalEntry;
import com.wealthlink.ledger.repository.JournalEntryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class JournalEntryServiceImpl implements JournalEntryService {

    private final JournalEntryRepository journalEntryRepository;

    @Override
    public List<JournalResponse.JournalEntryResponse> getAll() {
        return journalEntryRepository.findAll().stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    public JournalResponse.JournalEntryResponse getById(UUID id) {
        JournalEntry entry = journalEntryRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Journal Entry not found"));
        return mapToResponse(entry);
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
