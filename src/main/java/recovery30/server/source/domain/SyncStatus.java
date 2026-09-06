package recovery30.server.source.domain;

import io.swagger.v3.oas.annotations.media.Schema;

/** 연동 커넥션 동기화 상태 ({@code source_data_sources.sync_status}). */
@Schema(
    name = "SyncStatus",
    description =
        """
        연동 동기화 상태
        - SYNCED: 정상 갱신됨
        - PARTIAL: 일부만 반영됨 (커버리지 낮음)
        - FAILED: 갱신 실패
        """,
    enumAsRef = true)
public enum SyncStatus {
  SYNCED,
  PARTIAL,
  FAILED
}
