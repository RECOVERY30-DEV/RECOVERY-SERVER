package recovery30.server.business.domain;

import io.swagger.v3.oas.annotations.media.Schema;

/** 동의 항목 상태. NOT_SET 은 DB 값이 아니라 "아직 응답한 적 없음"을 표시용으로 나타낸 것. */
@Schema(
    name = "ConsentStatus",
    description =
        """
        동의 항목 상태
        - GRANTED: 동의함
        - WITHDRAWN: 철회함
        - NOT_SET: 아직 선택/응답한 적 없음 (core_consents 행 없음)
        """,
    enumAsRef = true)
public enum ConsentStatus {
  GRANTED,
  WITHDRAWN,
  NOT_SET
}
