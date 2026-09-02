package recovery30.server.forecast.getminbalance;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * 홈 화면 "예상 최저잔액" 박스. 금액은 원 단위 정수(부호 포함), "만 원" 변환·헤드라인/슬라이더 렌더는 클라이언트가 한다.
 *
 * <p>클라 렌더: 헤드라인 = {@code conservative} ~ {@code expected}, 슬라이더 트랙 = {@code conservative} → {@code
 * optimistic}, 마커 = {@code (expected - conservative) / (optimistic - conservative)}.
 */
public record MinBalanceView(
    @Schema(description = "예측 실행 ID", example = "4821") Long forecastRunId,
    @Schema(description = "밴드 산출 가능 여부. false면 HOLD(판단보류)라 세 값이 모두 null", example = "true")
        boolean available,
    @Schema(description = "보수적 시나리오 최저잔액(원)", example = "-1280000") Long conservative,
    @Schema(description = "예상 시나리오 최저잔액(원)", example = "540000") Long expected,
    @Schema(description = "낙관 시나리오 최저잔액(원)", example = "830000") Long optimistic) {}
