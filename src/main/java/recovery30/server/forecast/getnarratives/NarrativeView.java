package recovery30.server.forecast.getnarratives;

import io.swagger.v3.oas.annotations.media.Schema;
import recovery30.server.forecast.domain.ForecastRunNarrative;
import recovery30.server.forecast.domain.NarrativeKind;

/** 안정 상태 안내 / 판단보류 안내 화면의 서술 문구 한 줄. */
public record NarrativeView(
    @Schema(description = "종류") NarrativeKind kind,
    @Schema(description = "같은 종류 안에서의 순서", example = "0") Integer seq,
    @Schema(description = "문구", example = "향후 30일간 안전자금 아래로 내려갈 가능성이 낮습니다.") String text) {

  static NarrativeView of(ForecastRunNarrative n) {
    return new NarrativeView(NarrativeKind.valueOf(n.getKind()), n.getSeq(), n.getText());
  }
}
