package recovery30.server.business;

import java.time.Instant;
import recovery30.server.business.domain.Business;
import recovery30.server.business.domain.Consent;
import recovery30.server.business.domain.ConsentType;

/** business(동의) 슬라이스 통합테스트용 픽스처. */
public final class ConsentFixtures {

  private ConsentFixtures() {}

  public static Business business(String bizRegNo) {
    Business b = new Business();
    b.setUserId(1L);
    b.setBizRegNo(bizRegNo);
    b.setBizName("테스트 상점");
    b.setSafetyBufferAmount(1_000_000L);
    b.setCreatedAt(Instant.parse("2025-07-14T00:00:00Z"));
    return b;
  }

  public static ConsentType type(String code, String name, boolean required) {
    ConsentType t = new ConsentType();
    t.setCode(code);
    t.setName(name);
    t.setRequired(required);
    t.setPurpose(name + " 목적");
    t.setDataScope("거래 내역");
    t.setWithdrawEffect("철회 시 영향 안내");
    t.setVersion("v1.0");
    return t;
  }

  public static Consent consent(long businessId, String typeCode, String status) {
    Consent c = new Consent();
    c.setBusinessId(businessId);
    c.setConsentTypeCode(typeCode);
    c.setConsentVersion("v1.0");
    c.setStatus(status);
    if ("GRANTED".equals(status)) {
      c.setGrantedAt(Instant.parse("2025-07-14T00:00:00Z"));
    } else {
      c.setWithdrawnAt(Instant.parse("2025-07-15T00:00:00Z"));
    }
    return c;
  }
}
