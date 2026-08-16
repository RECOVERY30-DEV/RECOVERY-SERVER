package recovery30.server.member.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import java.util.regex.Pattern;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/** 회원 이메일 값 객체. 형식이 올바르지 않으면 생성 시점에 예외를 던진다. */
@Embeddable
@Getter
@Setter
@NoArgsConstructor
public class Email {

  private static final Pattern EMAIL_PATTERN =
      Pattern.compile("^[\\w.+-]+@[\\w-]+\\.[a-zA-Z]{2,}$");

  @Column(name = "email", nullable = false)
  private String value;

  public Email(String value) {
    if (value == null || !EMAIL_PATTERN.matcher(value).matches()) {
      throw new IllegalArgumentException("올바르지 않은 이메일 형식입니다: " + value);
    }
    this.value = value;
  }
}
