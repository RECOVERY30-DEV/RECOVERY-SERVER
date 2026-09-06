package recovery30.server.supportprogram.getsupportprograms;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDate;
import recovery30.server.supportprogram.domain.SupportProgramStatus;

/** 지원사업 목록 화면 카드 한 장. "Match 근거"는 program-recommendations 응답을 클라이언트가 programCode로 병합한다. */
public record SupportProgramSummaryView(
    @Schema(description = "지원제도 ID", example = "1") Long programId,
    @Schema(description = "지원제도 코드 (상세·자격 조회에 사용)", example = "SBIZ_STABLE_FUND")
        String programCode,
    @Schema(description = "지원제도명", example = "소상공인 경영안정자금") String name,
    @Schema(description = "지원 기관", example = "소상공인시장진흥공단") String agency,
    @Schema(description = "지원 내용 요약", example = "운전자금 최대 2,000만 원 융자") String supportContent,
    @Schema(description = "한도 금액(원). nullable", example = "20000000") Long limitAmount,
    @Schema(description = "금리 문구. nullable", example = "연 3.4% (고정, 상담자 확인 필요)")
        String interestRateText,
    @Schema(description = "신청 기한. nullable", example = "2025-07-31") LocalDate applyDeadline,
    @Schema(description = "상태") SupportProgramStatus status) {}
