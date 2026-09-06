package recovery30.server.recoveryoption.domain;

import io.swagger.v3.oas.annotations.media.Schema;

/** 시나리오 유형 ({@code recovery_scenarios.scenario_type}). DB CHECK 제약과 값이 1:1로 대응한다. */
@Schema(
    name = "ScenarioType",
    description =
        """
        시나리오 유형
        - BASELINE: 아무 조치 없음. 현재 데이터 기반 기준 시나리오
        - SIMULATED: 회복안을 적용했을 때의 시뮬레이션 결과 (appliedOptionIds 참조)
        """,
    enumAsRef = true)
public enum ScenarioType {
  BASELINE,
  SIMULATED
}
