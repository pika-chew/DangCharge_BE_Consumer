package com.picka_chew.dangcharge_Consumer.api.common.advice;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import io.swagger.v3.oas.annotations.Hidden;

import com.picka_chew.dangcharge_Consumer.api.common.dto.ApiResponseDto;
import com.picka_chew.dangcharge_Consumer.common.exception.ErrorStatus;
import com.picka_chew.dangcharge_Consumer.common.exception.GeneralException;

import lombok.extern.slf4j.Slf4j;

/**
 * 모든 오류를 공통 응답 포맷으로 바꾼다. 스프링 MVC 기본 예외(파라미터 누락, 타입 불일치, 검증 실패 등)는
 * ResponseEntityExceptionHandler가 상태 코드를 정하고, 본문만 공통 포맷으로 감싼다.
 */
@Slf4j
@Hidden
@RestControllerAdvice
public class ExceptionAdvice extends ResponseEntityExceptionHandler {

	@ExceptionHandler(GeneralException.class)
	public ResponseEntity<ApiResponseDto<Void>> handleGeneralException(GeneralException exception) {
		return toResponse(exception.getErrorStatus());
	}

	@ExceptionHandler(Exception.class)
	public ResponseEntity<ApiResponseDto<Void>> handleUnexpected(Exception exception) {
		log.error("처리하지 못한 예외", exception);
		return toResponse(ErrorStatus.INTERNAL_SERVER_ERROR);
	}

	@Override
	protected ResponseEntity<Object> handleExceptionInternal(Exception exception, Object body, HttpHeaders headers,
			HttpStatusCode statusCode, WebRequest request) {
		HttpStatus status = HttpStatus.resolve(statusCode.value());
		String code = status == null ? String.valueOf(statusCode.value()) : status.name();
		String message = status == null ? exception.getMessage() : status.getReasonPhrase();
		return ResponseEntity.status(statusCode).headers(headers).body(ApiResponseDto.onFailure(code, message));
	}

	private static ResponseEntity<ApiResponseDto<Void>> toResponse(ErrorStatus errorStatus) {
		return ResponseEntity.status(errorStatus.getHttpStatus()).body(ApiResponseDto.onFailure(errorStatus));
	}

}
