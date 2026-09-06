package recovery30.server.forecast.domain;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * 30일 현금흐름 예측 상태 ({@code forecast_runs.status}).
 *
 * <p>DB CHECK 제약과 값이 1:1로 대응한다. API 응답 계약의 단일 출처.
 */
@Schema(
    name = "ForecastStatus",
    description =
        """
        현금흐름 예측 상태
        - RISK: 30일 내 잔액 부족이 예상됨 (first_shortfall_date 존재)
        - STABLE: 30일간 안전자금 아래로 내려가지 않을 것으로 예상
        - HOLD: 데이터 커버리지가 낮아 판단 보류 (최저잔액 밴드 산출 불가)
        """,
    enumAsRef = true)
public enum ForecastStatus {
  RISK,
  STABLE,
  HOLD
}
