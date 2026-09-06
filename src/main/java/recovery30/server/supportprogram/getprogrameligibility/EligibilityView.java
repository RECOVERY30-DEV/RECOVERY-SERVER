package recovery30.server.supportprogram.getprogrameligibility;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.Instant;
import java.util.List;
import recovery30.server.supportprogram.domain.EligibilityEvaluationType;
import recovery30.server.supportprogram.domain.EligibilityResult;

/**
 * 지원사업 상세 화면 "자격요건 확인". 규칙 항목마다 체크박스 + 개별 문구를 렌더한다. 자동 판정이 아니라 참고용 추정이며({@code advisory=true}) 최종
 * 판단은 상담자가 한다. 아직 판정되지 않은 사업자·제도 조합이면 전체·항목 모두 {@code UNKNOWN}.
 */
public record EligibilityView(
    @Schema(description = "지원제도 코드", example = "SBIZ_STABLE_FUND") String programCode,
    @Schema(description = "종합 판정 (참고용)") EligibilityResult result,
    @Schema(
            description = "종합 판정 근거 (Match 근거). nullable",
            example = "최근 8주 매출 감소 패턴이 지원 대상 조건과 유사합니다.")
        String reasonText,
    @Schema(description = "항상 참고용(true). 자동 자격판정이 아님", example = "true") boolean advisory,
    @Schema(description = "적용 규칙셋 버전", example = "rule-2025-06") String rulesetVersion,
    @Schema(description = "판정 시각(UTC). 미판정이면 null", example = "2025-07-15T00:00:00Z")
        Instant checkedAt,
    @Schema(description = "규칙 항목별 결과") List<RuleResultView> items) {

  /** 자격요건 항목 한 줄 (체크박스 + 라벨 + 개별 문구). */
  public record RuleResultView(
      @Schema(description = "규칙 코드", example = "BIZ_AGE_1Y") String ruleCode,
      @Schema(description = "규칙 라벨", example = "사업자등록 1년 이상") String label,
      @Schema(description = "평가 방식") EligibilityEvaluationType evaluationType,
      @Schema(description = "항목 판정 (참고용). 미판정이면 UNKNOWN") EligibilityResult result,
      @Schema(description = "항목별 문구. nullable", example = "등록일 기준 충족 가능성 높음") String noteText) {}
}
