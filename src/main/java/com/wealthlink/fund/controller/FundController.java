package com.wealthlink.fund.controller;

import com.wealthlink.fund.dto.FundCreateRequest;
import com.wealthlink.fund.dto.FundResponse;
import com.wealthlink.fund.dto.FundUpdateRequest;
import com.wealthlink.fund.service.FundService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/funds")
@RequiredArgsConstructor
@Tag(
        name = "Funds",
        description = "Fund master management APIs"
)
public class FundController {

    private final FundService fundService;

    @PostMapping
    @Operation(
            summary = "Create a fund",
            description = "Creates a new fund master record."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Fund created successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid request"),
            @ApiResponse(responseCode = "404", description = "Currency or country not found"),
            @ApiResponse(responseCode = "409", description = "Fund with the same ISIN already exists")
    })
    public ResponseEntity<FundResponse> createFund(
            @Valid @RequestBody FundCreateRequest request
    ) {

        FundResponse response = fundService.createFund(request);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(response);
    }

    @GetMapping("/{fundId}")
    @Operation(
            summary = "Get fund by ID",
            description = "Retrieves a fund using its UUID."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Fund found"),
            @ApiResponse(responseCode = "404", description = "Fund not found")
    })
    public ResponseEntity<FundResponse> getFund(
            @Parameter(
                    description = "Fund UUID",
                    required = true
            )
            @PathVariable("fundId") UUID fundId
    ) {

        FundResponse response = fundService.getFund(fundId);

        return ResponseEntity.ok(response);
    }

    @GetMapping
    @Operation(
            summary = "List and search funds",
            description = "Returns paginated funds with optional ISIN or name filtering."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Funds retrieved successfully"
            )
    })
    public ResponseEntity<Page<FundResponse>> getFunds(

            @Parameter(description = "Exact ISIN filter")
            @RequestParam(value = "isin", required = false)
            String isin,

            @Parameter(description = "Case-insensitive partial fund name filter")
            @RequestParam(value = "name", required = false)
            String name,

            @PageableDefault(
                    size = 20,
                    sort = "name",
                    direction = Sort.Direction.ASC
            )
            Pageable pageable
    ) {

        Page<FundResponse> response = fundService.getFunds(
                isin,
                name,
                pageable
        );

        return ResponseEntity.ok(response);
    }

    @PatchMapping("/{fundId}")
    @Operation(
            summary = "Update a fund",
            description = "Partially updates an existing fund. Only supplied fields are modified."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Fund updated successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid request"),
            @ApiResponse(responseCode = "404", description = "Fund, currency or country not found")
    })
    public ResponseEntity<FundResponse> updateFund(

            @Parameter(
                    description = "Fund UUID",
                    required = true
            )
            @PathVariable("fundId") UUID fundId,

            @Valid @RequestBody FundUpdateRequest request
    ) {

        FundResponse response = fundService.updateFund(
                fundId,
                request
        );

        return ResponseEntity.ok(response);
    }
}