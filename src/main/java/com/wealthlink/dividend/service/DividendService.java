package com.wealthlink.dividend.service;

import com.wealthlink.dividend.dto.request.DeclareDividendRequest;
import com.wealthlink.dividend.dto.response.DividendAllocationResponse;
import com.wealthlink.dividend.dto.response.DividendEventResponse;

import java.util.List;
import java.util.UUID;

public interface DividendService {
    DividendEventResponse declareDividend(DeclareDividendRequest request);
    DividendEventResponse getDividendEventById(UUID eventId);
    List<DividendEventResponse> getEventsByShareClass(UUID shareClassId);

    DividendEventResponse calculateAllocations(UUID eventId);
    DividendEventResponse approveDividend(UUID eventId);
    DividendEventResponse payDividend(UUID eventId);

    List<DividendAllocationResponse> getAllocationsByEvent(UUID eventId);
    List<DividendAllocationResponse> getAllocationsByPortfolio(UUID portfolioId);
}