package recovery30.server.forecast.domain;

import io.swagger.v3.oas.annotations.media.Schema;

/** 예측 실행 서술 문구의 종류 ({@code forecast_run_narratives.kind}). */
@Schema(
    name = "NarrativeKind",
    description =
        """
        서술 문구 종류
        - STATUS_LABEL: 상태 배지 라벨 ("현금흐름 안정" / "판단 보류" 등)
        - STABLE_REASON: 안정 판단 근거 (안정 상태 화면 "판단 근거" bullet)
        - RISK_NOTE: 위험 상태 부연 ("부족일까지 11일 남았습니다.")
        - STATE_CHANGE_HINT: 상태가 바뀔 수 있는 경우 안내
        - DISCLAIMER: 공통 고지 문구
        """,
    enumAsRef = true)
public enum NarrativeKind {
  STATUS_LABEL,
  STABLE_REASON,
  RISK_NOTE,
  STATE_CHANGE_HINT,
  DISCLAIMER
}
