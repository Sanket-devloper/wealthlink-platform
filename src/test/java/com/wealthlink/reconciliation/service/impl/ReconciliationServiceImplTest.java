package com.wealthlink.reconciliation.service.impl;

import com.wealthlink.fund.entity.Provider;
import com.wealthlink.fund.repository.ProviderRepository;
import com.wealthlink.identity.entity.AppUser;
import com.wealthlink.identity.repository.AppUserRepository;
import com.wealthlink.reconciliation.dto.request.IngestExternalRecordRequest;
import com.wealthlink.reconciliation.dto.request.ResolveReconciliationItemRequest;
import com.wealthlink.reconciliation.dto.request.StartReconciliationRunRequest;
import com.wealthlink.reconciliation.dto.response.ReconciliationItemResponse;
import com.wealthlink.reconciliation.dto.response.ReconciliationResolutionResponse;
import com.wealthlink.reconciliation.dto.response.ReconciliationRunResponse;
import com.wealthlink.reconciliation.entity.*;
import com.wealthlink.reconciliation.exception.InvalidReconciliationStateException;
import com.wealthlink.reconciliation.exception.ReconciliationNotFoundException;
import com.wealthlink.reconciliation.repository.ExternalRecordRepository;
import com.wealthlink.reconciliation.repository.ReconciliationItemRepository;
import com.wealthlink.reconciliation.repository.ReconciliationResolutionRepository;
import com.wealthlink.reconciliation.repository.ReconciliationRunRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ReconciliationServiceImplTest {

    @Mock
    private ReconciliationRunRepository runRepository;

    @Mock
    private ExternalRecordRepository externalRecordRepository;

    @Mock
    private ReconciliationItemRepository itemRepository;

    @Mock
    private ReconciliationResolutionRepository resolutionRepository;

    @Mock
    private AppUserRepository appUserRepository;

    @Mock
    private ProviderRepository providerRepository;

    @InjectMocks
    private ReconciliationServiceImpl reconciliationService;

    private UUID runId;
    private UUID userId;
    private UUID providerId;
    private UUID itemId;
    private AppUser mockUser;
    private ReconciliationRun mockRun;
    private ReconciliationItem mockItem;

    @BeforeEach
    void setUp() {
        runId = UUID.randomUUID();
        userId = UUID.randomUUID();
        providerId = UUID.randomUUID();
        itemId = UUID.randomUUID();

        mockUser = AppUser.builder()
                .id(userId)
                .username("ops_admin")
                .build();

        mockRun = ReconciliationRun.builder()
                .id(runId)
                .runType("HOLDINGS")
                .businessDate(LocalDate.of(2026, 8, 19))
                .status(ReconciliationRunStatus.IN_PROGRESS)
                .initiatedBy(mockUser)
                .startedAt(Instant.now())
                .build();

        mockItem = ReconciliationItem.builder()
                .id(itemId)
                .reconciliationRun(mockRun)
                .internalReferenceType("POSITION")
                .internalReferenceId(UUID.randomUUID())
                .matchStatus(ReconciliationMatchStatus.AMOUNT_MISMATCH)
                .differenceDetails("{\"diff\": 150.00}")
                .createdAt(Instant.now())
                .build();
    }

    @Test
    @DisplayName("startRun: successfully starts reconciliation run with initiator")
    void startRun_WithInitiator_Success() {
        StartReconciliationRunRequest request = StartReconciliationRunRequest.builder()
                .runType("HOLDINGS")
                .businessDate(LocalDate.of(2026, 8, 19))
                .initiatedById(userId)
                .build();

        when(appUserRepository.findById(userId)).thenReturn(Optional.of(mockUser));
        when(runRepository.save(any(ReconciliationRun.class))).thenReturn(mockRun);

        ReconciliationRunResponse response = reconciliationService.startRun(request);

        assertThat(response).isNotNull();
        assertThat(response.getId()).isEqualTo(runId);
        assertThat(response.getStatus()).isEqualTo(ReconciliationRunStatus.IN_PROGRESS);
        assertThat(response.getInitiatedByUsername()).isEqualTo("ops_admin");
        verify(runRepository, times(1)).save(any(ReconciliationRun.class));
    }

    @Test
    @DisplayName("startRun: successfully starts reconciliation run without initiator")
    void startRun_WithoutInitiator_Success() {
        StartReconciliationRunRequest request = StartReconciliationRunRequest.builder()
                .runType("CASH")
                .businessDate(LocalDate.of(2026, 8, 19))
                .build();

        ReconciliationRun anonymousRun = ReconciliationRun.builder()
                .id(runId)
                .runType("CASH")
                .businessDate(LocalDate.of(2026, 8, 19))
                .status(ReconciliationRunStatus.IN_PROGRESS)
                .build();

        when(runRepository.save(any(ReconciliationRun.class))).thenReturn(anonymousRun);

        ReconciliationRunResponse response = reconciliationService.startRun(request);

        assertThat(response).isNotNull();
        assertThat(response.getInitiatedById()).isNull();
        assertThat(response.getInitiatedByUsername()).isNull();
    }

    @Test
    @DisplayName("completeRun: successfully updates status to COMPLETED and sets completedAt")
    void completeRun_Success() {
        when(runRepository.findById(runId)).thenReturn(Optional.of(mockRun));
        when(runRepository.save(any(ReconciliationRun.class))).thenAnswer(invocation -> invocation.getArgument(0));

        ReconciliationRunResponse response = reconciliationService.completeRun(runId);

        assertThat(response.getStatus()).isEqualTo(ReconciliationRunStatus.COMPLETED);
        assertThat(response.getCompletedAt()).isNotNull();
        verify(runRepository, times(1)).save(mockRun);
    }

    @Test
    @DisplayName("completeRun: throws InvalidReconciliationStateException if already COMPLETED")
    void completeRun_AlreadyCompleted_ThrowsException() {
        mockRun.setStatus(ReconciliationRunStatus.COMPLETED);
        when(runRepository.findById(runId)).thenReturn(Optional.of(mockRun));

        assertThatThrownBy(() -> reconciliationService.completeRun(runId))
                .isInstanceOf(InvalidReconciliationStateException.class)
                .hasMessageContaining("Reconciliation run is already completed");

        verify(runRepository, never()).save(any(ReconciliationRun.class));
    }

    @Test
    @DisplayName("completeRun: throws ReconciliationNotFoundException when run does not exist")
    void completeRun_NotFound_ThrowsException() {
        when(runRepository.findById(runId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> reconciliationService.completeRun(runId))
                .isInstanceOf(ReconciliationNotFoundException.class)
                .hasMessageContaining("Reconciliation run not found with ID");
    }

    @Test
    @DisplayName("getRunById: returns matching run details")
    void getRunById_Success() {
        when(runRepository.findById(runId)).thenReturn(Optional.of(mockRun));

        ReconciliationRunResponse response = reconciliationService.getRunById(runId);

        assertThat(response.getId()).isEqualTo(runId);
        assertThat(response.getRunType()).isEqualTo("HOLDINGS");
    }

    @Test
    @DisplayName("getRunsByBusinessDate: returns list of runs for the given date")
    void getRunsByBusinessDate_Success() {
        LocalDate date = LocalDate.of(2026, 8, 19);
        when(runRepository.findByBusinessDate(date)).thenReturn(List.of(mockRun));

        List<ReconciliationRunResponse> responses = reconciliationService.getRunsByBusinessDate(date);

        assertThat(responses).hasSize(1);
        assertThat(responses.get(0).getBusinessDate()).isEqualTo(date);
    }

    @Test
    @DisplayName("ingestExternalRecord: successfully saves record")
    void ingestExternalRecord_Success() {
        IngestExternalRecordRequest request = IngestExternalRecordRequest.builder()
                .reconciliationRunId(runId)
                .providerId(providerId)
                .externalReference("EXT-TXN-101")
                .rawPayload("{\"holding\": 1000}")
                .build();

        Provider mockProvider = Provider.builder().id(providerId).build();

        when(runRepository.findById(runId)).thenReturn(Optional.of(mockRun));
        when(providerRepository.findById(providerId)).thenReturn(Optional.of(mockProvider));

        reconciliationService.ingestExternalRecord(request);

        verify(externalRecordRepository, times(1)).save(any(ExternalRecord.class));
    }

    @Test
    @DisplayName("ingestExternalRecord: throws ReconciliationNotFoundException when provider does not exist")
    void ingestExternalRecord_ProviderNotFound_ThrowsException() {
        IngestExternalRecordRequest request = IngestExternalRecordRequest.builder()
                .reconciliationRunId(runId)
                .providerId(providerId)
                .externalReference("EXT-TXN-101")
                .rawPayload("{}")
                .build();

        when(runRepository.findById(runId)).thenReturn(Optional.of(mockRun));
        when(providerRepository.findById(providerId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> reconciliationService.ingestExternalRecord(request))
                .isInstanceOf(ReconciliationNotFoundException.class)
                .hasMessageContaining("Provider not found");

        verify(externalRecordRepository, never()).save(any(ExternalRecord.class));
    }

    @Test
    @DisplayName("getItemsByRunAndStatus: filters by matchStatus when present")
    void getItemsByRunAndStatus_WithStatusFilter() {
        when(itemRepository.findByReconciliationRunIdAndMatchStatus(runId, ReconciliationMatchStatus.AMOUNT_MISMATCH))
                .thenReturn(List.of(mockItem));

        List<ReconciliationItemResponse> result = reconciliationService.getItemsByRunAndStatus(runId, ReconciliationMatchStatus.AMOUNT_MISMATCH);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getMatchStatus()).isEqualTo(ReconciliationMatchStatus.AMOUNT_MISMATCH);
        verify(itemRepository, times(1)).findByReconciliationRunIdAndMatchStatus(runId, ReconciliationMatchStatus.AMOUNT_MISMATCH);
    }

    @Test
    @DisplayName("getItemsByRunAndStatus: returns all items when status is null")
    void getItemsByRunAndStatus_WithoutStatusFilter() {
        when(itemRepository.findByReconciliationRunId(runId)).thenReturn(List.of(mockItem));

        List<ReconciliationItemResponse> result = reconciliationService.getItemsByRunAndStatus(runId, null);

        assertThat(result).hasSize(1);
        verify(itemRepository, times(1)).findByReconciliationRunId(runId);
    }

    @Test
    @DisplayName("resolveItem: successfully resolves reconciliation item")
    void resolveItem_Success() {
        ResolveReconciliationItemRequest request = ResolveReconciliationItemRequest.builder()
                .resolvedById(userId)
                .resolutionType("MANUAL_ADJUSTMENT")
                .resolutionNotes("Verified with custodian statement")
                .build();

        UUID resId = UUID.randomUUID();
        ReconciliationResolution resolution = ReconciliationResolution.builder()
                .id(resId)
                .reconciliationItem(mockItem)
                .resolvedBy(mockUser)
                .resolutionType("MANUAL_ADJUSTMENT")
                .resolutionNotes("Verified with custodian statement")
                .resolvedAt(Instant.now())
                .build();

        when(itemRepository.findById(itemId)).thenReturn(Optional.of(mockItem));
        when(resolutionRepository.findByReconciliationItemId(itemId)).thenReturn(Optional.empty());
        when(appUserRepository.findById(userId)).thenReturn(Optional.of(mockUser));
        when(resolutionRepository.save(any(ReconciliationResolution.class))).thenReturn(resolution);

        ReconciliationResolutionResponse response = reconciliationService.resolveItem(itemId, request);

        assertThat(response).isNotNull();
        assertThat(response.getId()).isEqualTo(resId);
        assertThat(response.getResolvedByUsername()).isEqualTo("ops_admin");
        assertThat(response.getResolutionType()).isEqualTo("MANUAL_ADJUSTMENT");
        verify(resolutionRepository, times(1)).save(any(ReconciliationResolution.class));
    }

    @Test
    @DisplayName("resolveItem: throws InvalidReconciliationStateException when item already resolved")
    void resolveItem_AlreadyResolved_ThrowsException() {
        ResolveReconciliationItemRequest request = ResolveReconciliationItemRequest.builder()
                .resolvedById(userId)
                .resolutionType("FORCE_MATCH")
                .build();

        when(itemRepository.findById(itemId)).thenReturn(Optional.of(mockItem));
        when(resolutionRepository.findByReconciliationItemId(itemId))
                .thenReturn(Optional.of(ReconciliationResolution.builder().build()));

        assertThatThrownBy(() -> reconciliationService.resolveItem(itemId, request))
                .isInstanceOf(InvalidReconciliationStateException.class)
                .hasMessageContaining("Reconciliation item has already been resolved");

        verify(resolutionRepository, never()).save(any(ReconciliationResolution.class));
    }
}