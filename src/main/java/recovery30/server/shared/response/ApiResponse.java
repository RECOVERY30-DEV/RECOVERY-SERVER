package recovery30.server.shared.response;

/** 모든 API 응답을 감싸는 공통 포맷. success면 data, 실패면 error만 채워진다. */
public record ApiResponse<T>(boolean success, T data, ApiError error) {

  public static <T> ApiResponse<T> success(T data) {
    return new ApiResponse<>(true, data, null);
  }

  public static ApiResponse<Void> error(ApiError error) {
    return new ApiResponse<>(false, null, error);
  }
}
