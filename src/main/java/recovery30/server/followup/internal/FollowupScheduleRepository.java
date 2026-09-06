package recovery30.server.followup.internal;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import recovery30.server.followup.domain.FollowupSchedule;

/** 30/60/90일 사후 점검 일정 저장소. */
public interface FollowupScheduleRepository extends JpaRepository<FollowupSchedule, Long> {

  List<FollowupSchedule> findByBusinessIdOrderByScheduledDateAsc(Long businessId);
}
