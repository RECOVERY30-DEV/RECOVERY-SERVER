package recovery30.server.recoveryoption.domain;

import io.swagger.v3.oas.annotations.media.Schema;

/** 회복안 분류 ({@code recovery_options.category}). DB CHECK 제약과 값이 1:1로 대응한다. */
@Schema(
    name = "RecoveryOptionCategory",
    description =
        """
        회복안 분류
        - FINANCIAL_CONSULT: 금융 상담·협의가 필요한 옵션 (상환조건 조정, 금리인하요구 등)
        - SELF_ACTION: 당사자가 직접 실행하는 옵션 (고정비 납부일 재배치 등)
        - SUPPORT_PROGRAM: 정책자금·지원제도 연계 옵션
        """,
    enumAsRef = true)
public enum RecoveryOptionCategory {
  FINANCIAL_CONSULT,
  SELF_ACTION,
  SUPPORT_PROGRAM
}
