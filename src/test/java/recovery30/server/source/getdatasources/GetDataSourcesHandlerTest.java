package recovery30.server.source.getdatasources;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.math.BigDecimal;
import java.time.Instant;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;
import recovery30.server.source.domain.SourceDataSource;
import recovery30.server.source.internal.SourceDataSourceRepository;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class GetDataSourcesHandlerTest {

  private static final long BIZ = 77L;

  @Autowired private MockMvc mockMvc;
  @Autowired private SourceDataSourceRepository repository;

  private void save(String type, int coverage, String syncStatus) {
    SourceDataSource s = new SourceDataSource();
    s.setBusinessId(BIZ);
    s.setSourceType(type);
    s.setInstitutionName(type + " 기관");
    s.setCoverageRate(new BigDecimal(coverage + ".00"));
    s.setPeriodMonths(6);
    s.setLastSyncedAt(Instant.parse("2025-07-14T21:14:00Z"));
    s.setSyncStatus(syncStatus);
    repository.save(s);
  }

  @Test
  void 소스별_현황을_반환하고_70퍼_미만이면_belowThreshold다() throws Exception {
    save("BANK_ACCOUNT", 95, "SYNCED");
    save("AUTO_TRANSFER", 61, "PARTIAL");

    mockMvc
        .perform(get("/api/businesses/{businessId}/data-sources", BIZ))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.data.length()").value(2))
        .andExpect(jsonPath("$.data[0].sourceType").value("BANK_ACCOUNT"))
        .andExpect(jsonPath("$.data[0].belowThreshold").value(false))
        .andExpect(jsonPath("$.data[1].sourceType").value("AUTO_TRANSFER"))
        .andExpect(jsonPath("$.data[1].syncStatus").value("PARTIAL"))
        .andExpect(jsonPath("$.data[1].belowThreshold").value(true));
  }

  @Test
  void 연동이_없으면_빈_배열을_반환한다() throws Exception {
    mockMvc
        .perform(get("/api/businesses/{businessId}/data-sources", 999_999))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.data.length()").value(0));
  }
}
