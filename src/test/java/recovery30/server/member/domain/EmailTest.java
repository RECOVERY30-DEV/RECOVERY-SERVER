package recovery30.server.member.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.Test;
import recovery30.server.shared.exception.BusinessException;
import recovery30.server.shared.exception.ErrorCode;

class EmailTest {

  @Test
  void 올바른_이메일이면_생성된다() {
    Email email = new Email("test@example.com");

    assertThat(email.getValue()).isEqualTo("test@example.com");
  }

  @Test
  void 형식이_잘못되면_BusinessException을_던진다() {
    assertThatThrownBy(() -> new Email("not-an-email"))
        .isInstanceOf(BusinessException.class)
        .extracting(e -> ((BusinessException) e).getErrorCode())
        .isEqualTo(ErrorCode.INVALID_EMAIL_FORMAT);
  }
}
