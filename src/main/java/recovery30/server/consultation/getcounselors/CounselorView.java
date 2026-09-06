package recovery30.server.consultation.getcounselors;

import io.swagger.v3.oas.annotations.media.Schema;

/** 상담 예약 화면에서 상담자를 고를 때 쓰는 항목. */
public record CounselorView(
    @Schema(description = "상담자 ID", example = "1") Long counselorId,
    @Schema(description = "이름", example = "김상담") String name,
    @Schema(description = "소속 기관. nullable", example = "소상공인시장진흥공단") String institution,
    @Schema(description = "지점/센터. nullable", example = "서울중부센터") String branch,
    @Schema(description = "직함/역할. nullable", example = "경영지도사") String role) {}
