package recovery30.server.forecast.getforecast;

import io.swagger.v3.oas.annotations.media.Schema;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import recovery30.server.forecast.domain.ForecastStatus;

/**
 * 특정 예측 실행 1건의 요약. 원인 상세 화면 "부족 예상 시점 요약" 헤더와 Recovery Packet "위험 Snapshot"이 공유한다. 홈 화면의 latest /
 * min-balance / shortfall / safety-buffer 를 forecastRunId 기준으로 한 번에 받는 형태.
 */
public record ForecastDetailView(
    @Schema(description = "예측 실행 ID", example = "1") Long forecastRunId,
    @Schema(description = "사업자 ID", example = "1") Long businessId,
    @Schema(description = "예측 기준일", example = "2025-06-14") LocalDate baseDate,
    @Schema(description = "예측 최종 갱신 시각(UTC)", example = "2025-06-13T23:32:00Z") Instant updatedAt,
    @Schema(description = "현금흐름 상태", example = "RISK") ForecastStatus status,
    @Schema(description = "예측 기간(일)", example = "30") Integer horizonDays,
    @Schema(description = "전체 데이터 커버리지(%)", example = "84.00") BigDecimal coverageOverall,
    @Schema(description = "30일 내 부족 발생 여부. false면 dDay·expectedDate·부족액 null", example = "true")
        boolean hasShortfall,
    @Schema(description = "첫 부족일까지 남은 일수", example = "14") Integer daysToShortfall,
    @Schema(description = "첫 부족 예상일", example = "2025-06-28") LocalDate firstShortfallDate,
    @Schema(description = "예상 부족액 하한(원)", example = "800000") Long shortfallAmountMin,
    @Schema(description = "예상 부족액 상한(원)", example = "2300000") Long shortfallAmountMax,
    @Schema(description = "최저잔액 밴드 산출 가능 여부. false면 HOLD라 아래 3개 null", example = "true")
        boolean minBalanceAvailable,
    @Schema(description = "보수적 시나리오 최저잔액(원)", example = "-2300000") Long minBalanceConservative,
    @Schema(description = "예상 시나리오 최저잔액(원)", example = "-1400000") Long minBalanceExpected,
    @Schema(description = "낙관 시나리오 최저잔액(원)", example = "-800000") Long minBalanceOptimistic,
    @Schema(description = "Safety Buffer 충족 여부", example = "false") boolean bufferMet) {}
