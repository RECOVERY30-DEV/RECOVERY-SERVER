package recovery30.server.member.createmember;

import io.swagger.v3.oas.annotations.media.Schema;

public record CreateMemberResponse(
    @Schema(description = "회원 ID", example = "1") Long id,
    @Schema(description = "이메일", example = "user@example.com") String email,
    @Schema(description = "닉네임", example = "홍길동") String nickname) {}
