package recovery30.server.followup.domain;

import io.swagger.v3.oas.annotations.media.Schema;

/** 잔액 회복 여부 ({@code recovery_followup_results.balance_recovered}). */
@Schema(
    name = "BalanceRecovered",
    description =
        """
        잔액 회복 여부
        - YES: 목표 수준까지 회복
        - PARTIAL: 일부 회복
        - NO: 회복되지 않음
        """,
    enumAsRef = true)
public enum BalanceRecovered {
  YES,
  PARTIAL,
  NO
}
