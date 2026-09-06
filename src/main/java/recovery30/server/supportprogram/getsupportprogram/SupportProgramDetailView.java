package recovery30.server.supportprogram.getsupportprogram;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDate;
import recovery30.server.supportprogram.domain.SupportProgramStatus;

/** 지원사업 상세 화면 "지원 개요" + "공식 출처 및 신청". */
public record SupportProgramDetailView(
    @Schema(description = "지원제도 ID", example = "1") Long programId,
    @Schema(description = "지원제도 코드", example = "SBIZ_STABLE_FUND") String programCode,
    @Schema(description = "지원제도명", example = "소상공인 경영안정자금") String name,
    @Schema(description = "지원 기관", example = "소상공인시장진흥공단") String agency,
    @Schema(description = "지원 내용", example = "운전자금 최대 2,000만 원 융자") String supportContent,
    @Schema(description = "한도 금액(원). nullable", example = "20000000") Long limitAmount,
    @Schema(description = "금리 문구", example = "연 3.4% (고정, 상담자 확인 필요)") String interestRateText,
    @Schema(description = "지원 기간 문구", example = "3년 거치 5년 분할상환") String termText,
    @Schema(description = "신청 기한", example = "2025-07-31") LocalDate applyDeadline,
    @Schema(description = "신청 페이지 URL", example = "https://www.sbiz.or.kr") String applyUrl,
    @Schema(description = "공식 공고 URL", example = "https://www.sbiz.or.kr/notice/2025-1")
        String officialSourceUrl,
    @Schema(description = "규칙셋 버전", example = "rule-2025-06") String rulesetVersion,
    @Schema(description = "상태") SupportProgramStatus status) {}
