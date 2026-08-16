package recovery30.server.member.getmember;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;
import recovery30.server.member.createmember.CreateMemberCommand;
import tools.jackson.databind.ObjectMapper;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class GetMemberHandlerTest {

  @Autowired private MockMvc mockMvc;

  @Autowired private ObjectMapper objectMapper;

  @Test
  void 존재하는_회원이면_200과_회원정보를_반환한다() throws Exception {
    String createBody =
        objectMapper.writeValueAsString(new CreateMemberCommand("view@example.com", "뷰테스터"));
    String createResponse =
        mockMvc
            .perform(
                post("/api/members").contentType(MediaType.APPLICATION_JSON).content(createBody))
            .andReturn()
            .getResponse()
            .getContentAsString();
    long id = objectMapper.readTree(createResponse).path("data").path("id").asLong();

    mockMvc
        .perform(get("/api/members/{id}", id))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.success").value(true))
        .andExpect(jsonPath("$.data.email").value("view@example.com"));
  }

  @Test
  void 존재하지_않는_회원이면_404와_에러코드를_반환한다() throws Exception {
    mockMvc
        .perform(get("/api/members/{id}", 999_999))
        .andExpect(status().isNotFound())
        .andExpect(jsonPath("$.success").value(false))
        .andExpect(jsonPath("$.error.code").value("MEMBER_404"));
  }
}
