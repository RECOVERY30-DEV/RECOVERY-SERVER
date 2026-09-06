package recovery30.server.source.domain;

import io.swagger.v3.oas.annotations.media.Schema;

/** 보정값 확실성 ({@code source_adjustments.certainty}). 시나리오 반영 범위를 결정한다. */
@Schema(
    name = "AdjustmentCertainty",
    description =
        """
        보정값 확실성
        - CONFIRMED: 계약·통지 등 근거 있음. 예상·낙관 시나리오 모두 반영
        - ESTIMATED: 가능성 높지만 미확정. 낙관 시나리오에만 반영 (보수적 제외)
        """,
    enumAsRef = true)
public enum AdjustmentCertainty {
  CONFIRMED,
  ESTIMATED
}
