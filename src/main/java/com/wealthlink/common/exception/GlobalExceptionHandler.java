package com.wealthlink.common.exception;

import com.wealthlink.common.dto.ErrorResponse;
import com.wealthlink.fund.exception.CountryNotFoundException;
import com.wealthlink.fund.exception.CurrencyNotFoundException;
import com.wealthlink.fund.exception.DuplicateFundException;
import com.wealthlink.fund.exception.FundNotFoundException;
import com.wealthlink.fund.exception.FundProviderMappingNotFoundException;
import com.wealthlink.fund.exception.FundShareClassNotFoundException;
import com.wealthlink.fund.exception.ProviderNotFoundException;
import com.wealthlink.importdata.exception.ImportBatchNotFoundException;
import com.wealthlink.importdata.exception.ImportItemNotFoundException;
import com.wealthlink.importdata.exception.ImportJobNotFoundException;
import com.wealthlink.marketdata.exception.FundPriceAlreadyExistsException;
import com.wealthlink.marketdata.exception.FundPriceNotFoundException;
import com.wealthlink.marketdata.exception.FxRateAlreadyExistsException;
import com.wealthlink.marketdata.exception.FxRateNotFoundException;
import com.wealthlink.marketdata.exception.FxRateSourceAlreadyExistsException;
import com.wealthlink.marketdata.exception.FxRateSourceNotFoundException;
import jakarta.validation.ConstraintViolationException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.HashMap;
import java.util.Map;

/**
 * Central error handling for the REST API layer, so every controller
 * returns a consistent JSON error shape instead of raw stack traces.
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    // =========================================================
    // NOT FOUND EXCEPTIONS - 404
    // =========================================================

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleNotFound(ResourceNotFoundException ex) {
        return buildResponse(
                HttpStatus.NOT_FOUND,
                "Not Found",
                ex.getMessage()
        );
    }

    @ExceptionHandler(CountryNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleCountryNotFound(CountryNotFoundException ex) {
        return buildResponse(
                HttpStatus.NOT_FOUND,
                "Not Found",
                ex.getMessage()
        );
    }

    @ExceptionHandler(CurrencyNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleCurrencyNotFound(CurrencyNotFoundException ex) {
        return buildResponse(
                HttpStatus.NOT_FOUND,
                "Not Found",
                ex.getMessage()
        );
    }

    @ExceptionHandler(FundNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleFundNotFound(FundNotFoundException ex) {
        return buildResponse(
                HttpStatus.NOT_FOUND,
                "Not Found",
                ex.getMessage()
        );
    }

    @ExceptionHandler(FundProviderMappingNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleFundProviderMappingNotFound(
            FundProviderMappingNotFoundException ex) {

        return buildResponse(
                HttpStatus.NOT_FOUND,
                "Not Found",
                ex.getMessage()
        );
    }

    @ExceptionHandler(FundShareClassNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleFundShareClassNotFound(
            FundShareClassNotFoundException ex) {

        return buildResponse(
                HttpStatus.NOT_FOUND,
                "Not Found",
                ex.getMessage()
        );
    }

    @ExceptionHandler(ProviderNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleProviderNotFound(ProviderNotFoundException ex) {
        return buildResponse(
                HttpStatus.NOT_FOUND,
                "Not Found",
                ex.getMessage()
        );
    }

    @ExceptionHandler(FundPriceNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleFundPriceNotFound(FundPriceNotFoundException ex) {
        return buildResponse(
                HttpStatus.NOT_FOUND,
                "Not Found",
                ex.getMessage()
        );
    }

    @ExceptionHandler(FxRateNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleFxRateNotFound(FxRateNotFoundException ex) {
        return buildResponse(
                HttpStatus.NOT_FOUND,
                "Not Found",
                ex.getMessage()
        );
    }

    @ExceptionHandler(FxRateSourceNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleFxRateSourceNotFound(
            FxRateSourceNotFoundException ex) {

        return buildResponse(
                HttpStatus.NOT_FOUND,
                "Not Found",
                ex.getMessage()
        );
    }

    @ExceptionHandler(ImportBatchNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleImportBatchNotFound(
            ImportBatchNotFoundException ex) {

        return buildResponse(
                HttpStatus.NOT_FOUND,
                "Not Found",
                ex.getMessage()
        );
    }

    @ExceptionHandler(ImportItemNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleImportItemNotFound(
            ImportItemNotFoundException ex) {

        return buildResponse(
                HttpStatus.NOT_FOUND,
                "Not Found",
                ex.getMessage()
        );
    }

    @ExceptionHandler(ImportJobNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleImportJobNotFound(
            ImportJobNotFoundException ex) {

        return buildResponse(
                HttpStatus.NOT_FOUND,
                "Not Found",
                ex.getMessage()
        );
    }


    // =========================================================
    // DUPLICATE / ALREADY EXISTS EXCEPTIONS - 409
    // =========================================================

    @ExceptionHandler(DuplicateFundException.class)
    public ResponseEntity<ErrorResponse> handleDuplicateFund(DuplicateFundException ex) {
        return buildResponse(
                HttpStatus.CONFLICT,
                "Conflict",
                ex.getMessage()
        );
    }

    @ExceptionHandler(FundPriceAlreadyExistsException.class)
    public ResponseEntity<ErrorResponse> handleFundPriceAlreadyExists(
            FundPriceAlreadyExistsException ex) {

        return buildResponse(
                HttpStatus.CONFLICT,
                "Conflict",
                ex.getMessage()
        );
    }

    @ExceptionHandler(FxRateAlreadyExistsException.class)
    public ResponseEntity<ErrorResponse> handleFxRateAlreadyExists(
            FxRateAlreadyExistsException ex) {

        return buildResponse(
                HttpStatus.CONFLICT,
                "Conflict",
                ex.getMessage()
        );
    }

    @ExceptionHandler(FxRateSourceAlreadyExistsException.class)
    public ResponseEntity<ErrorResponse> handleFxRateSourceAlreadyExists(
            FxRateSourceAlreadyExistsException ex) {

        return buildResponse(
                HttpStatus.CONFLICT,
                "Conflict",
                ex.getMessage()
        );
    }


    // =========================================================
    // VALIDATION EXCEPTIONS - 400
    // =========================================================

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidation(
            MethodArgumentNotValidException ex) {

        Map<String, String> fieldErrors = new HashMap<>();

        ex.getBindingResult()
                .getFieldErrors()
                .forEach(fe ->
                        fieldErrors.put(
                                fe.getField(),
                                fe.getDefaultMessage()
                        )
                );

        ErrorResponse body = new ErrorResponse(
                HttpStatus.BAD_REQUEST.value(),
                "Validation Failed",
                "One or more fields are invalid",
                fieldErrors
        );

        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(body);
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ErrorResponse> handleConstraintViolation(
            ConstraintViolationException ex) {

        return buildResponse(
                HttpStatus.BAD_REQUEST,
                "Validation Failed",
                ex.getMessage()
        );
    }

    @ExceptionHandler(InvalidRoleSelectionException.class)
    public ResponseEntity<ErrorResponse> handleInvalidRoleSelection(
            InvalidRoleSelectionException ex) {

        return buildResponse(
                HttpStatus.BAD_REQUEST,
                "Invalid Role",
                ex.getMessage()
        );
    }


    // =========================================================
    // DATABASE EXCEPTIONS - 409
    // =========================================================

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ErrorResponse> handleDataIntegrity(
            DataIntegrityViolationException ex) {

        ErrorResponse body = new ErrorResponse(
                HttpStatus.CONFLICT.value(),
                "Conflict",
                "The request conflicts with existing data (duplicate value or invalid reference)"
        );

        return ResponseEntity
                .status(HttpStatus.CONFLICT)
                .body(body);
    }


    // =========================================================
    // SECURITY EXCEPTIONS
    // =========================================================

    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<ErrorResponse> handleAuthentication(
            AuthenticationException ex) {

        return buildResponse(
                HttpStatus.UNAUTHORIZED,
                "Unauthorized",
                "Invalid username or password"
        );
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ErrorResponse> handleAccessDenied(
            AccessDeniedException ex) {

        return buildResponse(
                HttpStatus.FORBIDDEN,
                "Forbidden",
                "You do not have permission to perform this action"
        );
    }


    // =========================================================
    // COMMON RESPONSE BUILDER
    // =========================================================

    private ResponseEntity<ErrorResponse> buildResponse(
            HttpStatus status,
            String error,
            String message) {

        ErrorResponse body = new ErrorResponse(
                status.value(),
                error,
                message
        );

        return ResponseEntity
                .status(status)
                .body(body);
    }
}