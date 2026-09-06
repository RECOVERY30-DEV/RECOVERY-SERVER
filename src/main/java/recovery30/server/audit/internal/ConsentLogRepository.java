package recovery30.server.audit.internal;

import org.springframework.data.jpa.repository.JpaRepository;
import recovery30.server.audit.domain.ConsentLog;

/** 동의 변경 append-only 이력 저장소. */
public interface ConsentLogRepository extends JpaRepository<ConsentLog, Long> {}
