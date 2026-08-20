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
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Unit tests for {@link JournalServiceImpl}.
 * All collaborators are mocked — no Spring context or database required.
 */
@ExtendWith(MockitoExtension.class)
class JournalServiceImplTest {

    @Mock JournalRepository journalRepository;
    @Mock LedgerAccountRepository ledgerAccountRepository;
    @Mock CurrencyRepository currencyRepository;

    @InjectMocks JournalServiceImpl service;

    // -------------------------------------------------------------------------
    // Helpers
    // -------------------------------------------------------------------------

    private Currency currency(UUID id, String isoCode) {
        Currency c = new Currency();
        c.setId(id);
        c.setIsoCode(isoCode);
        return c;
    }

    private LedgerAccount ledgerAccount(UUID id) {
        Currency usd = currency(UUID.randomUUID(), "USD");
        return LedgerAccount.builder()
                .id(id)
                .accountCode("ACC-" + id)
                .accountName("Test Account")
                .ledgerAccountType(LedgerAccountType.CASH)
                .currency(usd)
                .balance(BigDecimal.ZERO)
                .build();
    }

    private Journal postedJournal(UUID id, UUID currencyId) {
        Currency usd = currency(currencyId, "USD");
        LedgerAccount debitAcct = ledgerAccount(UUID.randomUUID());
        LedgerAccount creditAcct = ledgerAccount(UUID.randomUUID());

        JournalEntry debit = JournalEntry.builder()
                .id(UUID.randomUUID())
                .direction(JournalEntryDirection.DEBIT)
                .amount(BigDecimal.valueOf(500))
                .currency(usd)
                .ledgerAccount(debitAcct)
                .build();

        JournalEntry credit = JournalEntry.builder()
                .id(UUID.randomUUID())
                .direction(JournalEntryDirection.CREDIT)
                .amount(BigDecimal.valueOf(500))
                .currency(usd)
                .ledgerAccount(creditAcct)
                .build();

        return Journal.builder()
                .id(id)
                .journalReference("REF-" + id)
                .description("Test journal")
                .status(JournalStatus.POSTED)
                .journalType(JournalType.TRADE)
                .journalDate(LocalDate.now())
                .entries(Arrays.asList(debit, credit))
                .build();
    }

    // -------------------------------------------------------------------------
    // getById
    // -------------------------------------------------------------------------

    @Test
    void getById_returnsMappedResponse_whenJournalExists() {
        UUID id = UUID.randomUUID();
        UUID currencyId = UUID.randomUUID();
        Journal journal = postedJournal(id, currencyId);
        when(journalRepository.findById(id)).thenReturn(Optional.of(journal));

        JournalResponse response = service.getById(id);

        assertThat(response.getId()).isEqualTo(id);
        assertThat(response.getStatus()).isEqualTo("POSTED");
        assertThat(response.getJournalType()).isEqualTo("TRADE");
    }

    @Test
    void getById_throwsResourceNotFoundException_whenJournalNotFound() {
        UUID id = UUID.randomUUID();
        when(journalRepository.findById(id)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.getById(id))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("not found");
    }

    // -------------------------------------------------------------------------
    // createJournal — validation
    // -------------------------------------------------------------------------

    @Test
    void createJournal_throwsIllegalArgument_whenEntriesListIsEmpty() {
        CreateJournalRequest request = new CreateJournalRequest();
        request.setDescription("Test");
        request.setJournalType("TRADE");
        request.setEntries(List.of());

        assertThatThrownBy(() -> service.createJournal(request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("at least two entries");
    }

    @Test
    void createJournal_throwsIllegalArgument_whenAmountIsZero() {
        UUID currencyId = UUID.randomUUID();
        UUID accountId = UUID.randomUUID();

        CreateJournalEntryRequest entry = new CreateJournalEntryRequest();
        entry.setDirection("DEBIT");
        entry.setAmount(BigDecimal.ZERO);
        entry.setCurrencyId(currencyId);
        entry.setLedgerAccountId(accountId);

        CreateJournalRequest request = new CreateJournalRequest();
        request.setDescription("Test");
        request.setJournalType("TRADE");
        request.setEntries(List.of(entry));

        assertThatThrownBy(() -> service.createJournal(request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("strictly positive");
    }

    @Test
    void createJournal_throwsIllegalArgument_whenEntriesAreUnbalanced() {
        UUID currencyId = UUID.randomUUID();
        UUID acct1 = UUID.randomUUID();
        UUID acct2 = UUID.randomUUID();

        CreateJournalEntryRequest debit = new CreateJournalEntryRequest();
        debit.setDirection("DEBIT");
        debit.setAmount(BigDecimal.valueOf(1000));
        debit.setCurrencyId(currencyId);
        debit.setLedgerAccountId(acct1);

        CreateJournalEntryRequest credit = new CreateJournalEntryRequest();
        credit.setDirection("CREDIT");
        credit.setAmount(BigDecimal.valueOf(500)); // intentionally unbalanced
        credit.setCurrencyId(currencyId);
        credit.setLedgerAccountId(acct2);

        CreateJournalRequest request = new CreateJournalRequest();
        request.setDescription("Unbalanced");
        request.setJournalType("TRADE");
        request.setEntries(Arrays.asList(debit, credit));

        assertThatThrownBy(() -> service.createJournal(request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("unbalanced");
    }

    @Test
    void createJournal_throwsIllegalArgument_forInvalidDirection() {
        UUID currencyId = UUID.randomUUID();

        CreateJournalEntryRequest entry = new CreateJournalEntryRequest();
        entry.setDirection("SIDEWAYS");
        entry.setAmount(BigDecimal.valueOf(100));
        entry.setCurrencyId(currencyId);
        entry.setLedgerAccountId(UUID.randomUUID());

        CreateJournalRequest request = new CreateJournalRequest();
        request.setDescription("Invalid");
        request.setJournalType("TRADE");
        request.setEntries(List.of(entry));

        assertThatThrownBy(() -> service.createJournal(request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Invalid direction");
    }

    // -------------------------------------------------------------------------
    // createJournal — idempotency
    // -------------------------------------------------------------------------

    @Test
    void createJournal_returnsExistingJournal_whenIdempotencyKeyAlreadyExists() {
        UUID currencyId = UUID.randomUUID();
        String idempotencyKey = "idem-key-123";
        UUID existingId = UUID.randomUUID();
        Journal existing = postedJournal(existingId, currencyId);
        existing.setIdempotencyKey(idempotencyKey);

        when(journalRepository.findByIdempotencyKey(idempotencyKey))
                .thenReturn(Optional.of(existing));

        CreateJournalRequest request = new CreateJournalRequest();
        request.setDescription("Duplicate");
        request.setJournalType("TRADE");
        request.setIdempotencyKey(idempotencyKey);
        request.setEntries(List.of()); // not reached due to idempotency short-circuit

        JournalResponse response = service.createJournal(request);

        assertThat(response.getId()).isEqualTo(existingId);
        verify(journalRepository, never()).save(any());
    }

    // -------------------------------------------------------------------------
    // createJournal — happy path
    // -------------------------------------------------------------------------

    @Test
    void createJournal_savesJournal_forValidBalancedEntries() {
        UUID currencyId = UUID.randomUUID();
        UUID acct1 = UUID.randomUUID();
        UUID acct2 = UUID.randomUUID();

        Currency usd = currency(currencyId, "USD");
        LedgerAccount la1 = ledgerAccount(acct1);
        LedgerAccount la2 = ledgerAccount(acct2);

        when(currencyRepository.findById(currencyId)).thenReturn(Optional.of(usd));
        when(ledgerAccountRepository.findById(acct1)).thenReturn(Optional.of(la1));
        when(ledgerAccountRepository.findById(acct2)).thenReturn(Optional.of(la2));

        CreateJournalEntryRequest debit = new CreateJournalEntryRequest();
        debit.setDirection("DEBIT");
        debit.setAmount(BigDecimal.valueOf(200));
        debit.setCurrencyId(currencyId);
        debit.setLedgerAccountId(acct1);

        CreateJournalEntryRequest credit = new CreateJournalEntryRequest();
        credit.setDirection("CREDIT");
        credit.setAmount(BigDecimal.valueOf(200));
        credit.setCurrencyId(currencyId);
        credit.setLedgerAccountId(acct2);

        CreateJournalRequest request = new CreateJournalRequest();
        request.setDescription("Valid journal");
        request.setJournalType("TRADE");
        request.setJournalDate(LocalDate.now());
        request.setEntries(Arrays.asList(debit, credit));

        ArgumentCaptor<Journal> captor = ArgumentCaptor.forClass(Journal.class);
        when(journalRepository.save(captor.capture())).thenAnswer(inv -> inv.getArgument(0));

        service.createJournal(request);

        Journal saved = captor.getValue();
        assertThat(saved.getStatus()).isEqualTo(JournalStatus.POSTED);
        assertThat(saved.getEntries()).hasSize(2);
    }

    // -------------------------------------------------------------------------
    // reverseJournal
    // -------------------------------------------------------------------------

    @Test
    void reverseJournal_throwsIllegalState_whenJournalIsNotPosted() {
        UUID id = UUID.randomUUID();
        Journal draft = Journal.builder()
                .id(id)
                .journalReference("REF-DRAFT")
                .description("Draft")
                .status(JournalStatus.DRAFT)
                .journalType(JournalType.TRADE)
                .journalDate(LocalDate.now())
                .build();

        when(journalRepository.findById(id)).thenReturn(Optional.of(draft));

        ReverseJournalRequest req = new ReverseJournalRequest();
        req.setReason("Test");

        assertThatThrownBy(() -> service.reverseJournal(id, req))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Only POSTED journals can be reversed");
    }

    @Test
    void reverseJournal_createsReversalWithInvertedDirections_andMarksOriginalReversed() {
        UUID id = UUID.randomUUID();
        UUID currencyId = UUID.randomUUID();
        Journal original = postedJournal(id, currencyId);

        when(journalRepository.findById(id)).thenReturn(Optional.of(original));
        when(journalRepository.save(any(Journal.class))).thenAnswer(inv -> inv.getArgument(0));

        ReverseJournalRequest req = new ReverseJournalRequest();
        req.setReason("Error correction");

        ReverseJournalResponse response = service.reverseJournal(id, req);

        assertThat(response.getReversedJournalId()).isEqualTo(id);
        assertThat(response.getStatus()).isEqualTo("POSTED");

        // Original must be REVERSED
        assertThat(original.getStatus()).isEqualTo(JournalStatus.REVERSED);

        // Verify two saves: one for reversal, one for original
        verify(journalRepository, times(2)).save(any(Journal.class));
    }

    @Test
    void reverseJournal_invertedEntryDirections_debitBecomesCredit() {
        UUID id = UUID.randomUUID();
        UUID currencyId = UUID.randomUUID();
        Journal original = postedJournal(id, currencyId);

        when(journalRepository.findById(id)).thenReturn(Optional.of(original));
        ArgumentCaptor<Journal> captor = ArgumentCaptor.forClass(Journal.class);
        when(journalRepository.save(captor.capture())).thenAnswer(inv -> inv.getArgument(0));

        service.reverseJournal(id, new ReverseJournalRequest());

        // First captured call is the reversal journal
        Journal reversal = captor.getAllValues().get(0);
        List<JournalEntry> reversalEntries = reversal.getEntries();

        // Original was DEBIT+CREDIT → reversal must be CREDIT+DEBIT
        assertThat(reversalEntries).hasSize(2);
        boolean hasCredit = reversalEntries.stream()
                .anyMatch(e -> e.getDirection() == JournalEntryDirection.CREDIT);
        boolean hasDebit = reversalEntries.stream()
                .anyMatch(e -> e.getDirection() == JournalEntryDirection.DEBIT);
        assertThat(hasCredit).isTrue();
        assertThat(hasDebit).isTrue();
    }
}
