package recovery30.server.business.updateconsent;

import io.swagger.v3.oas.annotations.media.Schema;

/** 동의 항목 grant/withdraw 요청. */
public record UpdateConsentCommand(
    @Schema(
            description = "true = 동의(GRANTED), false = 철회(WITHDRAWN)",
            example = "true",
            requiredMode = Schema.RequiredMode.REQUIRED)
        Boolean granted) {}
