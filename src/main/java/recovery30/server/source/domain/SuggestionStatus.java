package recovery30.server.source.domain;

import io.swagger.v3.oas.annotations.media.Schema;

/** 반복 패턴 추정 후보 상태 ({@code source_adjustment_suggestions.status}). */
@Schema(
    name = "SuggestionStatus",
    description =
        """
        추정 후보 상태
        - PROPOSED: 제안됨 (수락/거절 대기)
        - ACCEPTED: 수락됨 → 보정값(DRAFT)으로 생성됨
        - REJECTED: 거절됨
        """,
    enumAsRef = true)
public enum SuggestionStatus {
  PROPOSED,
  ACCEPTED,
  REJECTED
}
