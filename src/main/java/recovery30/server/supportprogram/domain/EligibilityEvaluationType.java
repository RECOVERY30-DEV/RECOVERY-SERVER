package recovery30.server.supportprogram.domain;

import io.swagger.v3.oas.annotations.media.Schema;

/** 자격요건 항목의 평가 방식 ({@code recovery_program_eligibility_rules.evaluation_type}). */
@Schema(
    name = "EligibilityEvaluationType",
    description =
        """
        자격요건 항목 평가 방식
        - AUTO: 예측/프로필 데이터로 자동 추정 가능
        - COUNSELOR_ONLY: 상담자만 판단 가능 (예: 금융기관 연체 여부)
        """,
    enumAsRef = true)
public enum EligibilityEvaluationType {
  AUTO,
  COUNSELOR_ONLY
}
