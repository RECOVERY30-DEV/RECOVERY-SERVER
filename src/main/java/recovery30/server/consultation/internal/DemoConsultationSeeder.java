package recovery30.server.consultation.internal;

import java.time.Instant;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Profile;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import recovery30.server.consultation.domain.Counselor;
import recovery30.server.consultation.domain.CounselorSlot;

/**
 * demo 프로파일에서 상담 예약 화면의 예약 가능 슬롯을 주입한다. 상담자는 Flyway V14 가 2명 적재하며, V14 가 없는 테스트 환경이면 여기서 2명을
 * 채운다(멱등). 슬롯은 잔여석이 서로 다르게(3/2/1) 세팅해 "잔여 N석" 표시를 확인할 수 있게 한다.
 */
@Component
@Profile("demo")
@ConditionalOnProperty(
    prefix = "demo.seed",
    name = "enabled",
    havingValue = "true",
    matchIfMissing = true)
@Order(6)
public class DemoConsultationSeeder implements ApplicationRunner {

  private static final Logger log = LoggerFactory.getLogger(DemoConsultationSeeder.class);

  private final CounselorRepository counselorRepository;
  private final CounselorSlotRepository counselorSlotRepository;

  public DemoConsultationSeeder(
      CounselorRepository counselorRepository, CounselorSlotRepository counselorSlotRepository) {
    this.counselorRepository = counselorRepository;
    this.counselorSlotRepository = counselorSlotRepository;
  }

  @Override
  @Transactional
  public void run(ApplicationArguments args) {
    if (counselorSlotRepository.count() > 0) {
      return;
    }
    List<Counselor> counselors = counselorRepository.findAllByOrderByIdAsc();
    if (counselors.isEmpty()) {
      counselors =
          List.of(
              counselorRepository.save(counselor("김상담", "소상공인시장진흥공단", "서울중부센터", "경영지도사")),
              counselorRepository.save(counselor("이회복", "IBK기업은행", "을지로점", "여신상담역")));
      log.info("[demo] 상담자 2명 시딩 (Flyway V14 미적용 환경)");
    }

    int[] booked = {1, 0, 2}; // 잔여 2 / 3 / 1
    Instant[] starts = {
      Instant.parse("2025-07-14T01:00:00Z"),
      Instant.parse("2025-07-14T05:00:00Z"),
      Instant.parse("2025-07-15T02:00:00Z")
    };
    int total = 0;
    for (Counselor c : counselors) {
      for (int i = 0; i < starts.length; i++) {
        CounselorSlot slot = new CounselorSlot();
        slot.setCounselorId(c.getId());
        slot.setStartAt(starts[i]);
        slot.setEndAt(starts[i].plusSeconds(1800));
        slot.setCapacity(3);
        slot.setBookedCount(booked[i]);
        slot.setStatus(booked[i] >= 3 ? "BOOKED" : "OPEN");
        counselorSlotRepository.save(slot);
        total++;
      }
    }
    log.info("[demo] 상담 슬롯 {}건 시딩", total);
  }

  private static Counselor counselor(String name, String institution, String branch, String role) {
    Counselor c = new Counselor();
    c.setName(name);
    c.setInstitution(institution);
    c.setBranch(branch);
    c.setRole(role);
    return c;
  }
}
