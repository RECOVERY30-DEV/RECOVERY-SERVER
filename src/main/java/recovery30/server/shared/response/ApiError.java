package recovery30.server.shared.response;

import io.swagger.v3.oas.annotations.media.Schema;

/** 실패 응답에 담기는 에러 정보. */
public record ApiError(
    @Schema(description = "에러 코드", example = "MEMBER_404") String code,
    @Schema(description = "에러 메시지", example = "존재하지 않는 회원입니다") String message) {}
