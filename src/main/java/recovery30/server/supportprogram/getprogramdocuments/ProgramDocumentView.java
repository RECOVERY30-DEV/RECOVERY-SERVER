package recovery30.server.supportprogram.getprogramdocuments;

import io.swagger.v3.oas.annotations.media.Schema;

/** 지원사업 상세 화면 "필요서류" 한 줄. */
public record ProgramDocumentView(
    @Schema(description = "서류 ID", example = "5") Long documentId,
    @Schema(description = "서류명", example = "사업자등록증") String name,
    @Schema(description = "설명. nullable", example = "최근 발급본") String description,
    @Schema(description = "필수 여부", example = "true") boolean required) {}
