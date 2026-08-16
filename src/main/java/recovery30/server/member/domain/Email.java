package recovery30.server.member.domain;

import java.util.Objects;
import java.util.regex.Pattern;

/** 회원 이메일 값 객체. 형식이 올바르지 않으면 생성 시점에 예외를 던진다. */
public record Email(String value) {

  private static final Pattern EMAIL_PATTERN =
      Pattern.compile("^[\\w.+-]+@[\\w-]+\\.[a-zA-Z]{2,}$");

  public Email {
    Objects.requireNonNull(value, "email은 null일 수 없습니다");
    if (!EMAIL_PATTERN.matcher(value).matches()) {
      throw new IllegalArgumentException("올바르지 않은 이메일 형식입니다: " + value);
    }
  }
}
