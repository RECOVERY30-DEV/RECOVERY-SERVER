package recovery30.server.shared.exception;

import org.springframework.http.HttpStatus;

/** 서비스 전역에서 쓰는 에러 코드. 새 모듈이 에러를 추가할 땐 이 enum에 상수를 추가한다. */
public enum ErrorCode {
  INVALID_INPUT(HttpStatus.BAD_REQUEST, "COMMON_400", "잘못된 요청입니다"),
  MALFORMED_REQUEST_BODY(HttpStatus.BAD_REQUEST, "COMMON_400_1", "요청 본문을 읽을 수 없습니다"),
  RESOURCE_NOT_FOUND(HttpStatus.NOT_FOUND, "COMMON_404", "요청한 경로를 찾을 수 없습니다"),
  METHOD_NOT_ALLOWED(HttpStatus.METHOD_NOT_ALLOWED, "COMMON_405", "허용되지 않은 HTTP 메서드입니다"),
  INTERNAL_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "COMMON_500", "서버 내부 오류가 발생했습니다"),

  INVALID_EMAIL_FORMAT(HttpStatus.BAD_REQUEST, "MEMBER_400_1", "올바르지 않은 이메일 형식입니다"),
  INVALID_NICKNAME(HttpStatus.BAD_REQUEST, "MEMBER_400_2", "닉네임은 비어있을 수 없습니다"),
  MEMBER_NOT_FOUND(HttpStatus.NOT_FOUND, "MEMBER_404", "존재하지 않는 회원입니다"),

  FORECAST_NOT_FOUND(HttpStatus.NOT_FOUND, "FORECAST_404_1", "예측 이력이 없습니다"),

  RECOVERY_OPTION_NOT_FOUND(HttpStatus.NOT_FOUND, "RECOVERY_404_1", "존재하지 않는 회복안입니다"),
  RECOVERY_OPTION_SELECTION_LIMIT(
      HttpStatus.BAD_REQUEST, "RECOVERY_400_1", "회복안은 최대 2개까지 선택할 수 있습니다"),

  SUPPORT_PROGRAM_NOT_FOUND(HttpStatus.NOT_FOUND, "SUPPORT_404_1", "존재하지 않는 지원제도입니다"),

  PACKET_NOT_FOUND(HttpStatus.NOT_FOUND, "PACKET_404_1", "존재하지 않는 Recovery Packet입니다"),

  CONSULTATION_NOT_FOUND(HttpStatus.NOT_FOUND, "CONSULTATION_404_1", "존재하지 않는 상담 예약입니다"),
  COUNSELOR_NOT_FOUND(HttpStatus.NOT_FOUND, "CONSULTATION_404_2", "존재하지 않는 상담자입니다"),
  INVALID_CONSULTATION_CHANNEL(HttpStatus.BAD_REQUEST, "CONSULTATION_400_1", "지원하지 않는 상담 채널입니다"),
  SLOT_NOT_BOOKABLE(HttpStatus.BAD_REQUEST, "CONSULTATION_400_2", "예약 가능한 슬롯이 아닙니다");

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
