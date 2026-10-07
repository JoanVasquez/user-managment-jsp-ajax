package userprocesses;

import static org.assertj.core.api.Assertions.assertThat;

import com.fasterxml.jackson.databind.JsonNode;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest(
        webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
        properties = {
                "spring.datasource.url="
                        + "jdbc:h2:mem:jsp-rendering;MODE=Oracle;DB_CLOSE_DELAY=-1;DATABASE_TO_LOWER=TRUE",
                "server.servlet.context-path=/test-app"
        })
@ActiveProfiles("test")
class JspRenderingIntegrationTest {

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @BeforeEach
    void cleanDatabase() {
        jdbcTemplate.update("DELETE FROM users");
    }

    @Test
    void jspPagesRenderThroughEmbeddedTomcat() {
        ResponseEntity<String> index = restTemplate.getForEntity("/", String.class);
        assertThat(index.getStatusCodeValue()).isEqualTo(200);
        assertThat(index.getBody())
                .contains("<title>User Processes</title>")
                .contains("<body id=\"main\">")
                .contains("id=\"app-config\"")
                .contains("data-csrf-url=\"/test-app/api/csrf\"")
                .contains("data-users-url=\"/test-app/api/users\"")
                .contains("id=\"page-config\"")
                .contains("data-sessions-url=\"/test-app/api/sessions\"")
                .contains("data-password-reset-requests-url=\"/test-app/api/password-reset-requests\"")
                .contains("data-welcome-url=\"/test-app/welcome\"")
                .containsSubsequence(
                        "src=\"https://ajax.googleapis.com/ajax/libs/jquery/3.7.1/jquery.min.js\"",
                        "src=\"/test-app/resources/js/api-client.js\"",
                        "src=\"/test-app/resources/js/session.js\"")
                .contains("id=\"login-form\"")
                .contains("id=\"register-form\"")
                .containsOnlyOnce("id=\"app-config\"")
                .containsOnlyOnce("id=\"page-config\"")
                .doesNotContain("data-current-session-url=", "data-index-url=");

        ResponseEntity<JsonNode> csrf = restTemplate.getForEntity("/api/csrf", JsonNode.class);
        String cookie = sessionCookie(csrf);
        String token = csrf.getBody().get("data").get("token").asText();

        HttpHeaders apiHeaders = apiHeaders(cookie, token);
        ResponseEntity<String> registration = restTemplate.exchange(
                "/api/users",
                HttpMethod.POST,
                new HttpEntity<String>(
                        "{\"firstName\":\"Ada\",\"lastName\":\"Lovelace\","
                                + "\"email\":\"ada@example.com\","
                                + "\"password\":\"correct horse battery staple\"}",
                        apiHeaders),
                String.class);
        assertThat(registration.getStatusCodeValue()).isEqualTo(201);
        assertThat(registration.getHeaders().getLocation().getPath())
                .startsWith("/test-app/api/users/");

        ResponseEntity<Void> login = restTemplate.exchange(
                "/api/sessions",
                HttpMethod.POST,
                new HttpEntity<String>(
                        "{\"email\":\"ada@example.com\","
                                + "\"password\":\"correct horse battery staple\"}",
                        apiHeaders),
                Void.class);
        assertThat(login.getStatusCodeValue()).isEqualTo(201);
        assertThat(login.getHeaders().getLocation().getPath())
                .isEqualTo("/test-app/api/sessions/current");

        String authenticatedCookieHeader = login.getHeaders().getFirst(HttpHeaders.SET_COOKIE);
        String authenticatedCookie = authenticatedCookieHeader == null
                ? cookie
                : authenticatedCookieHeader.split(";", 2)[0];
        HttpHeaders pageHeaders = new HttpHeaders();
        pageHeaders.set(HttpHeaders.COOKIE, authenticatedCookie);

        ResponseEntity<JsonNode> missingApi = restTemplate.exchange(
                "/api/not-mapped", HttpMethod.GET,
                new HttpEntity<Void>(pageHeaders), JsonNode.class);
        assertThat(missingApi.getStatusCodeValue()).isEqualTo(404);
        assertThat(missingApi.getBody().get("code").asText()).isEqualTo("RESOURCE_NOT_FOUND");
        assertThat(missingApi.getBody().get("msg").asText()).isEqualTo("Resource not found.");
        assertThat(missingApi.getBody().get("data").isNull()).isTrue();

        ResponseEntity<String> welcome = restTemplate.exchange(
                "/welcome", HttpMethod.GET, new HttpEntity<Void>(pageHeaders), String.class);

        assertThat(welcome.getStatusCodeValue()).isEqualTo(200);
        assertThat(welcome.getBody())
                .contains("<title>User Management</title>")
                .contains("id=\"app-config\"")
                .contains("data-csrf-url=\"/test-app/api/csrf\"")
                .contains("data-users-url=\"/test-app/api/users\"")
                .contains("id=\"page-config\"")
                .contains("data-current-session-url=\"/test-app/api/sessions/current\"")
                .contains("data-index-url=\"/test-app/\"")
                .containsSubsequence(
                        "src=\"https://ajax.googleapis.com/ajax/libs/jquery/3.7.1/jquery.min.js\"",
                        "src=\"/test-app/resources/js/api-client.js\"",
                        "src=\"https://cdn.datatables.net/1.13.8/js/jquery.dataTables.min.js\"",
                        "src=\"https://cdn.datatables.net/1.13.8/js/dataTables.bootstrap.min.js\"",
                        "src=\"/test-app/resources/js/main.js\"")
                .contains("id=\"tbl-user\"")
                .contains("id=\"btn-logout\"")
                .containsOnlyOnce("id=\"app-config\"")
                .containsOnlyOnce("id=\"page-config\"")
                .doesNotContain(
                        "data-sessions-url=",
                        "data-password-reset-requests-url=",
                        "data-welcome-url=");
    }

    private HttpHeaders apiHeaders(String cookie, String token) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set(HttpHeaders.COOKIE, cookie);
        headers.set("X-CSRF-TOKEN", token);
        return headers;
    }

    private String sessionCookie(ResponseEntity<?> response) {
        String setCookie = response.getHeaders().getFirst(HttpHeaders.SET_COOKIE);
        assertThat(setCookie).isNotNull();
        return setCookie.split(";", 2)[0];
    }
}
