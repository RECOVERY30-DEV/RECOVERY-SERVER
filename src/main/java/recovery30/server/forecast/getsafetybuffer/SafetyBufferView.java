package recovery30.server.forecast.getsafetybuffer;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * 홈 화면 "안전 잔액" 박스. "약 83만 원" 포맷과 shield 라벨("안전상태"/"주의")은 클라이언트가 {@code bufferMet}로 만든다.
 *
 * <p>{@code amount}는 예상 시나리오 최저잔액({@code min_balance_expected})을 재사용한다. 별도 정의가 필요하면 forecast_runs에
 * 스냅샷 컬럼을 추가한다 (docs 결정사항 참고).
 */
public record SafetyBufferView(
    @Schema(description = "예측 실행 ID", example = "4821") Long forecastRunId,
    @Schema(description = "안전 잔액(원)", example = "830000") Long amount,
    @Schema(description = "Safety Buffer 충족 여부", example = "true") boolean bufferMet) {}
