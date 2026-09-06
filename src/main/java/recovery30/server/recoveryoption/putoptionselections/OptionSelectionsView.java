package recovery30.server.recoveryoption.putoptionselections;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;

/** 회복안 선택 저장 결과. 클라이언트는 이 목록으로 카드의 selected 상태를 갱신한다. */
public record OptionSelectionsView(
    @Schema(description = "현재 선택된 회복안 ID 목록", example = "[1, 3]") List<Long> selectedOptionIds) {}
