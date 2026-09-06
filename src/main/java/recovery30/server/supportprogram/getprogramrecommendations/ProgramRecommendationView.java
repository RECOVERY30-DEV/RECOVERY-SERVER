package recovery30.server.supportprogram.getprogramrecommendations;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDate;

/** 지원사업 목록/회복안 비교의 "추천" 항목. 목록 카드의 "Match 근거"를 programCode로 병합한다. */
public record ProgramRecommendationView(
    @Schema(description = "추천 순위 (1이 최상위)", example = "1") Integer rankNo,
    @Schema(description = "지원제도 코드", example = "SBIZ_STABLE_FUND") String programCode,
    @Schema(description = "지원제도명", example = "소상공인 경영안정자금") String name,
    @Schema(description = "지원 기관", example = "소상공인시장진흥공단") String agency,
    @Schema(description = "신청 기한. nullable", example = "2025-07-31") LocalDate applyDeadline,
    @Schema(description = "추천 근거 (Match 근거)", example = "최근 6개월 매출 감소·사업자 2년 이상·신용등급 조건 확인 필요")
        String matchReason) {}
