package recovery30.server.supportprogram.domain;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * 자격 판정 결과 ({@code recovery_program_eligibility_checks.result} 및 규칙별 결과). 자동 판정이 아니라 참고용(advisory)
 * 추정이다.
 */
@Schema(
    name = "EligibilityResult",
    description =
        """
        자격 판정 결과 (참고용 추정, 확정 아님)
        - LIKELY_PASS: 충족 가능성 높음
        - NEEDS_REVIEW: 상담자 최종 확인 필요
        - LIKELY_FAIL: 미충족 가능성 높음
        - UNKNOWN: 판단 불가 / 아직 판정되지 않음
        """,
    enumAsRef = true)
public enum EligibilityResult {
  LIKELY_PASS,
  NEEDS_REVIEW,
  LIKELY_FAIL,
  UNKNOWN
}
