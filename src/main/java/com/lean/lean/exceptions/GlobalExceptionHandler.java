package com.lean.lean.exceptions;

import com.fasterxml.jackson.databind.exc.InvalidFormatException;
import com.lean.lean.dto.BaseResponse;
import com.lean.lean.dto.Result;
import lombok.extern.slf4j.Slf4j;
import org.apache.tomcat.util.http.fileupload.impl.FileSizeLimitExceededException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.stereotype.Component;
import org.springframework.validation.BindException;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.multipart.MaxUploadSizeExceededException;

import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
@Slf4j
@Component("globalException")
public class GlobalExceptionHandler {
	
	@ExceptionHandler(ResourceNotFoundException.class)
    @SuppressWarnings("java:S2259")
    public ResponseEntity<BaseResponse> resourceNotFoundExceptionHandler(
            ResourceNotFoundException ex, WebRequest request) {
        log.error("resourceNotFoundExceptionHandler {} ", ex);
        BaseResponse baseResponse = new BaseResponse();
        Result result = new Result();
        baseResponse.setResult(result);
        return new ResponseEntity<>(baseResponse, HttpStatus.NOT_FOUND);
    }
	
	@ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<BaseResponse> handleMethodArgsNotValidException(
            MethodArgumentNotValidException ex, WebRequest request) {
        log.error("MethodArgumentNotValidException {} ", ex);
        BaseResponse baseResponse = new BaseResponse();
        Result result = new Result();
        BindingResult bindingResult = ex.getBindingResult();
        String responseDescription = bindingResult.getFieldErrors().get(0).getDefaultMessage();
        result.setResponseDescription(responseDescription);

        Map<String, String> fieldErrors = new HashMap<>(); //field-specific errors
        for (FieldError error : bindingResult.getFieldErrors()) {
            fieldErrors.put(error.getField(), error.getDefaultMessage());
        }
        baseResponse.setResult(result);
        log.info("fielderrors {} ", fieldErrors);
        baseResponse.setErrorFields(fieldErrors);
        baseResponse.setResult(result);
        return new ResponseEntity<>(baseResponse, HttpStatus.BAD_REQUEST);
    }

    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    @ExceptionHandler(FieldNullOrBlankException.class)
    public BaseResponse handleFieldNullOrBlankException(FieldNullOrBlankException ex,
                                                        WebRequest request) {
        log.error("handleBadRequestException {} ", ex);
        BaseResponse baseResponse = new BaseResponse();
        Result result = new Result();
        log.info("error {}", ex.getMessage());
        result.setResponseDescription(ex.getMessage());
        baseResponse.setResult(result);
        return baseResponse;
    }
    
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(BadRequestException.class)
    public Result handleBadRequestException(BadRequestException ex, WebRequest request) {
        log.error("handleBadRequestException {} ", ex);
        Result result = new Result();
        String responseDescription = ex.getMessage();
        result.setResponseDescription(responseDescription);
        return result;
    }

    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(InvalidFormatException.class)
    public BaseResponse invalidFormatException(InvalidFormatException ex, WebRequest request) {
        BaseResponse baseResponse = new BaseResponse();
        Result result = new Result();
        log.error("invalid format {} ", ex);
        String responseDescription = ex.getMessage();
        result.setResponseDescription(responseDescription);
        baseResponse.setResult(result);
        return baseResponse;
    }

    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public BaseResponse handleHttpMessageNotReadableException(HttpMessageNotReadableException ex, WebRequest request) {
        BaseResponse baseResponse = new BaseResponse();
        Result result = new Result();
        log.error("exception {}", ex);
        result.setResponseDescription(ex.getMessage());
        baseResponse.setResult(result);
        return baseResponse;
    }

    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(InvalidInputException.class)
    public BaseResponse handleInvalidInputException(InvalidInputException ex, WebRequest request) {
        log.error("handleInvalidInputException {} ", ex);
        BaseResponse baseResponse = new BaseResponse();
        Result result = new Result();
        result.setResponseDescription(ex.getMessage());
        baseResponse.setResult(result);
        return baseResponse;
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<BaseResponse> handleIllegalArgumentException(
            IllegalArgumentException ex, WebRequest request) {
        log.error("handleIllegalArgumentException {} ", ex);
        BaseResponse baseResponse = new BaseResponse();
        Result result = new Result();
        result.setResponseCode(100016);
        result.setResponseDescription(ex.getMessage());
        baseResponse.setResult(result);
        return new ResponseEntity<>(baseResponse, HttpStatus.BAD_REQUEST);
    }


    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ResponseEntity<BaseResponse> handleMissingServletRequestParameterException(
            MissingServletRequestParameterException ex, WebRequest request) {
        String message = ex.getMessage();
        log.error("handleMissingServletRequestParameterException {}", message);
        BaseResponse baseResponse = new BaseResponse();
        Result result = new Result();
        result.setResponseCode(100014);
        result.setResponseDescription(message);
        baseResponse.setResult(result);
        return new ResponseEntity<>(baseResponse, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(FileSizeLimitExceededException.class)
    public ResponseEntity<BaseResponse> handleFileSizeLimitExceededException(
            FileSizeLimitExceededException ex, WebRequest request) {
        String message = ex.getMessage();
        log.error("handleFileSizeLimitExceededException {}", message);
        BaseResponse baseResponse = new BaseResponse();
        Result result = new Result();
        result.setResponseCode(100070);
        result.setResponseDescription(message);
        baseResponse.setResult(result);
        return new ResponseEntity<>(baseResponse, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(MaxUploadSizeExceededException.class)
    public ResponseEntity<Result> handleMaxUploadSizeExceededException(
            MaxUploadSizeExceededException ex, WebRequest request) {
        String message = ex.getMessage();
        log.error("handleMaxUploadSizeExceededException {}", message);
        // Custom error message
        String customMessage = "Upload failed: File size exceeds the 2MB limit.";
        Result result = new Result();
        result.setResponseCode(100070);
        result.setResponseDescription(customMessage);
        return new ResponseEntity<>(result, HttpStatus.BAD_REQUEST);
    }


    @ExceptionHandler(BindException.class)
    public ResponseEntity<BaseResponse> handleBindException(BindException ex, WebRequest request){
        String message = ex.getMessage();
        BaseResponse baseResponse = new BaseResponse();
        Result result = new Result();
        result.setResponseCode(400);
        result.setResponseDescription(message);
        baseResponse.setResult(result);
        return new ResponseEntity<>(baseResponse, HttpStatus.BAD_REQUEST);
    }
}
