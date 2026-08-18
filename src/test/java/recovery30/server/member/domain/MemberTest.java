package recovery30.server.member.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.Test;
import recovery30.server.shared.exception.BusinessException;
import recovery30.server.shared.exception.ErrorCode;

class MemberTest {

  @Test
  void 정상_값이면_생성된다() {
    Member member = new Member(new Email("test@example.com"), "닉네임");

    assertThat(member.getNickname()).isEqualTo("닉네임");
    assertThat(member.getRegisteredAt()).isNotNull();
  }

  @Test
  void 닉네임이_비어있으면_BusinessException을_던진다() {
    assertThatThrownBy(() -> new Member(new Email("test@example.com"), " "))
        .isInstanceOf(BusinessException.class)
        .extracting(e -> ((BusinessException) e).getErrorCode())
        .isEqualTo(ErrorCode.INVALID_NICKNAME);
  }
}
