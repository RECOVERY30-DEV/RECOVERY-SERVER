package recovery30.server.recoveryoption.putoptionselections;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;

/** 회복안 비교 화면 "회복 옵션 선택 (최대 2개)" 저장 요청. 선택 집합 전체를 교체한다. */
public record PutOptionSelectionsCommand(
    @Schema(description = "선택할 회복안 ID 목록. 최대 2개. 빈 배열이면 전체 해제", example = "[1, 3]")
        List<Long> optionIds) {}
