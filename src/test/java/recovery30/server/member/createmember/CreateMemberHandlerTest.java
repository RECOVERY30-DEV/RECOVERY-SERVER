package recovery30.server.member.createmember;

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
import tools.jackson.databind.ObjectMapper;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class CreateMemberHandlerTest {

  @Autowired private MockMvc mockMvc;

  @Autowired private ObjectMapper objectMapper;

  @Test
  void 정상_요청이면_201과_생성된_회원을_반환한다() throws Exception {
    String body =
        objectMapper.writeValueAsString(new CreateMemberCommand("test@example.com", "테스터"));

    mockMvc
        .perform(post("/api/members").contentType(MediaType.APPLICATION_JSON).content(body))
        .andExpect(status().isCreated())
        .andExpect(jsonPath("$.success").value(true))
        .andExpect(jsonPath("$.data.email").value("test@example.com"))
        .andExpect(jsonPath("$.data.nickname").value("테스터"));
  }

  @Test
  void 이메일_형식이_잘못되면_400과_에러코드를_반환한다() throws Exception {
    String body = objectMapper.writeValueAsString(new CreateMemberCommand("not-an-email", "테스터"));

    mockMvc
        .perform(post("/api/members").contentType(MediaType.APPLICATION_JSON).content(body))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.success").value(false))
        .andExpect(jsonPath("$.error.code").value("MEMBER_400_1"));
  }

  @Test
  void 닉네임이_비어있으면_400과_에러코드를_반환한다() throws Exception {
    String body = objectMapper.writeValueAsString(new CreateMemberCommand("test@example.com", " "));

    mockMvc
        .perform(post("/api/members").contentType(MediaType.APPLICATION_JSON).content(body))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.error.code").value("MEMBER_400_2"));
  }
}
