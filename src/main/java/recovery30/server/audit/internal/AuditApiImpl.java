package recovery30.server.audit.internal;

import java.time.Instant;
import org.springframework.stereotype.Component;
import recovery30.server.audit.api.AuditApi;
import recovery30.server.audit.domain.ConsentLog;

/** AuditApi 구현체. 다른 모듈은 이 클래스가 아니라 AuditApi 인터페이스만 주입받는다. */
@Component
public class AuditApiImpl implements AuditApi {

  private final ConsentLogRepository consentLogRepository;

  public AuditApiImpl(ConsentLogRepository consentLogRepository) {
    this.consentLogRepository = consentLogRepository;
  }

  @Override
  public void appendConsentLog(
      Long businessId,
      String consentTypeCode,
      String fromStatus,
      String toStatus,
      String consentVersion,
      String ipAddress,
      String userAgent) {
    ConsentLog log = new ConsentLog();
    log.setBusinessId(businessId);
    log.setConsentTypeCode(consentTypeCode);
    log.setFromStatus(fromStatus);
    log.setToStatus(toStatus);
    log.setConsentVersion(consentVersion);
    log.setIpAddress(ipAddress);
    log.setUserAgent(userAgent);
    log.setChangedAt(Instant.now());
    consentLogRepository.save(log);
  }
}
