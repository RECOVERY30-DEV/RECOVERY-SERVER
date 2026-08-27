package recovery30.server.shared.web;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/** 클라이언트 연동용 API 문서(Swagger UI: /swagger-ui.html, OpenAPI: /v3/api-docs) 설정. */
@Configuration
public class OpenApiConfig {

  @Bean
  public OpenAPI openApi() {
    return new OpenAPI()
        .info(
            new Info().title("Recovery30 API").description("Recovery30 백엔드 API 문서").version("v1"));
  }
}
