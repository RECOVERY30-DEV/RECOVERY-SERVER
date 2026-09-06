package recovery30.server.consultation;

import java.time.Instant;
import recovery30.server.consultation.domain.Consultation;
import recovery30.server.consultation.domain.Counselor;
import recovery30.server.consultation.domain.CounselorSlot;

/** consultation 슬라이스 통합테스트용 픽스처. */
public final class ConsultationFixtures {

  private ConsultationFixtures() {}

  public static Counselor counselor(String name) {
    Counselor c = new Counselor();
    c.setName(name);
    c.setInstitution("소상공인시장진흥공단");
    c.setBranch("서울중부센터");
    c.setRole("경영지도사");
    return c;
  }

  public static CounselorSlot slot(
      long counselorId, Instant startAt, int capacity, int bookedCount) {
    CounselorSlot s = new CounselorSlot();
    s.setCounselorId(counselorId);
    s.setStartAt(startAt);
    s.setEndAt(startAt.plusSeconds(1800));
    s.setCapacity(capacity);
    s.setBookedCount(bookedCount);
    s.setStatus(bookedCount >= capacity ? "BOOKED" : "OPEN");
    return s;
  }

  public static Consultation consultation(long businessId, long counselorId, String channel) {
    Consultation c = new Consultation();
    c.setBusinessId(businessId);
    c.setCounselorId(counselorId);
    c.setChannel(channel);
    c.setScheduledAt(Instant.parse("2025-07-14T01:00:00Z"));
    c.setPurposeText("30일 현금흐름 위험 대응");
    c.setTransferConsentGranted(true);
    c.setStatus("REQUESTED");
    return c;
  }
}
