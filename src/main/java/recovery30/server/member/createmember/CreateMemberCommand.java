package recovery30.server.member.createmember;

import io.swagger.v3.oas.annotations.media.Schema;

public record CreateMemberCommand(
    @Schema(description = "이메일", example = "user@example.com") String email,
    @Schema(description = "닉네임", example = "홍길동") String nickname) {}
