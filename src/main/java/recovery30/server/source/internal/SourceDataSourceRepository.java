package recovery30.server.source.internal;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import recovery30.server.source.domain.SourceDataSource;

/** 원천 연동 커넥션 현재 상태 저장소 (source_data_sources). business_id + source_type 당 1건. */
public interface SourceDataSourceRepository extends JpaRepository<SourceDataSource, Long> {

  List<SourceDataSource> findByBusinessIdOrderByIdAsc(Long businessId);

  long countByBusinessId(Long businessId);
}
