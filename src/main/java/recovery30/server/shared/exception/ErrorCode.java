package recovery30.server.shared.exception;

import org.springframework.http.HttpStatus;

/** 서비스 전역에서 쓰는 에러 코드. 새 모듈이 에러를 추가할 땐 이 enum에 상수를 추가한다. */
public enum ErrorCode {
  INVALID_INPUT(HttpStatus.BAD_REQUEST, "COMMON_400", "잘못된 요청입니다"),
  INTERNAL_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "COMMON_500", "서버 내부 오류가 발생했습니다"),

  INVALID_EMAIL_FORMAT(HttpStatus.BAD_REQUEST, "MEMBER_400_1", "올바르지 않은 이메일 형식입니다"),
  INVALID_NICKNAME(HttpStatus.BAD_REQUEST, "MEMBER_400_2", "닉네임은 비어있을 수 없습니다"),
  MEMBER_NOT_FOUND(HttpStatus.NOT_FOUND, "MEMBER_404", "존재하지 않는 회원입니다");

  private final HttpStatus status;
  private final String code;
  private final String message;

  ErrorCode(HttpStatus status, String code, String message) {
    this.status = status;
    this.code = code;
    this.message = message;
  }

  public HttpStatus getStatus() {
    return status;
  }

  public String getCode() {
    return code;
  }

  public String getMessage() {
    return message;
  }
}
