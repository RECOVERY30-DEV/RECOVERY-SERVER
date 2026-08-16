package recovery30.server.shared.exception;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import recovery30.server.shared.response.ApiError;
import recovery30.server.shared.response.ApiResponse;

/** 모든 컨트롤러(Handler)의 예외를 ApiResponse 형태로 통일해서 내려준다. */
@RestControllerAdvice
public class GlobalExceptionHandler {

  @ExceptionHandler(BusinessException.class)
  public ResponseEntity<ApiResponse<Void>> handleBusinessException(BusinessException e) {
    ErrorCode errorCode = e.getErrorCode();
    return ResponseEntity.status(errorCode.getStatus())
        .body(ApiResponse.error(new ApiError(errorCode.getCode(), e.getMessage())));
  }

  @ExceptionHandler(Exception.class)
  public ResponseEntity<ApiResponse<Void>> handleException(Exception e) {
    ErrorCode errorCode = ErrorCode.INTERNAL_SERVER_ERROR;
    return ResponseEntity.status(errorCode.getStatus())
        .body(ApiResponse.error(new ApiError(errorCode.getCode(), errorCode.getMessage())));
  }
}
