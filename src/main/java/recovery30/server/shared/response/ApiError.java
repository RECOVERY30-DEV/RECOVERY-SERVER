package recovery30.server.shared.response;

/** 실패 응답에 담기는 에러 정보. */
public record ApiError(String code, String message) {}
