package recovery30.server.forecast.getlatestforecast;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.Instant;
import java.time.LocalDate;

/** 홈 화면 헤더 + 상태 배지. 반환된 {@code forecastRunId}로 하위 리소스를 조회한다. */
public record LatestForecastView(
    @Schema(description = "예측 실행 ID (하위 리소스 조회에 사용)", example = "4821") Long forecastRunId,
    @Schema(description = "예측 기준일", example = "2025-07-15") LocalDate baseDate,
    @Schema(description = "예측 최종 갱신 시각(UTC)", example = "2025-07-14T23:32:00Z") Instant updatedAt,
    @Schema(
            description = "현금흐름 상태",
            example = "RISK",
            allowableValues = {"RISK", "STABLE", "HOLD"})
        String status) {}
