package recovery30.server.source.domain;

import io.swagger.v3.oas.annotations.media.Schema;

/** 보정값 유형 ({@code source_adjustments.adjustment_type}). 입력 4개 화면을 이 하나로 통합한다. */
@Schema(
    name = "AdjustmentType",
    description =
        """
        보정값 유형
        - CASH_SALES: 현금매출 (유입)
        - EXTERNAL_FUND: 타행·외부자금 (유입)
        - EXPECTED_INCOME: 예정수입 (유입)
        - EXPECTED_EXPENSE: 예정지출 (유출)
        """,
    enumAsRef = true)
public enum AdjustmentType {
  CASH_SALES,
  EXTERNAL_FUND,
  EXPECTED_INCOME,
  EXPECTED_EXPENSE;

  /** 유형에 따른 현금흐름 방향. 예정지출만 유출. */
  public String direction() {
    return this == EXPECTED_EXPENSE ? "O" : "I";
  }
}
