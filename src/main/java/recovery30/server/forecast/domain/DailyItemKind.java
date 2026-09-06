package recovery30.server.forecast.domain;

import io.swagger.v3.oas.annotations.media.Schema;

/** 일자별 근거 라인의 종류 ({@code forecast_daily_items.item_kind}). */
@Schema(
    name = "DailyItemKind",
    description =
        """
        일자별 근거 라인 종류
        - CONFIRMED: 확정 거래 (계좌·카드정산·자동이체 등 원천 데이터)
        - EXPECTED: 예상 거래 (반복 패턴·추정)
        - ADJUSTMENT: 사용자 보정값
        """,
    enumAsRef = true)
public enum DailyItemKind {
  CONFIRMED,
  EXPECTED,
  ADJUSTMENT
}
