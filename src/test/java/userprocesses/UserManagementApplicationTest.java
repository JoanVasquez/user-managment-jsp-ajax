package userprocesses;

import static org.hamcrest.Matchers.hasKey;
import static org.hamcrest.Matchers.endsWith;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.nullValue;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.forwardedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

import javax.servlet.http.HttpSession;

import com.jayway.jsonpath.JsonPath;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class UserManagementApplicationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @BeforeEach
    void cleanDatabase() {
        jdbcTemplate.update("DELETE FROM users");
    }

    @Test
    void anonymousUserCannotReadUsers() throws Exception {
        mockMvc.perform(get("/api/users"))
                .andExpect(status().isUnauthorized())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.code").value("AUTHENTICATION_REQUIRED"))
                .andExpect(jsonPath("$.msg").value("Authentication is required."))
                .andExpect(jsonPath("$.data").value(nullValue()));
    }

    @Test
    void unlistedRoutesRequireAuthenticationBeforeMvcHandlesThem() throws Exception {
        mockMvc.perform(get("/not-mapped"))
                .andExpect(status().isUnauthorized());

        register("ada@example.com", "correct horse battery staple");
        MockHttpSession session = login("ada@example.com", "correct horse battery staple");

        mockMvc.perform(get("/not-mapped").session(session))
                .andExpect(status().isNotFound());
    }

    @Test
    void mvcProtocolErrorsUseTheApiResponseWrapper() throws Exception {
        register("ada@example.com", "correct horse battery staple");
        MockHttpSession session = login("ada@example.com", "correct horse battery staple");

        mockMvc.perform(get("/api/sessions").session(session))
                .andExpect(status().isMethodNotAllowed())
                .andExpect(jsonPath("$.code").value("METHOD_NOT_ALLOWED"))
                .andExpect(jsonPath("$.msg").value("Request method is not supported."))
                .andExpect(jsonPath("$.data").value(nullValue()));

        mockMvc.perform(put("/api/users/not-a-number").session(session).with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(updateUserJson("Ada", "Lovelace", "a new secure password")))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("INVALID_REQUEST"))
                .andExpect(jsonPath("$.msg").value("Request parameters are invalid."))
                .andExpect(jsonPath("$.data").value(nullValue()));
    }

    @Test
    void csrfEndpointReturnsSessionBoundToken() throws Exception {
        MvcResult csrfResult = mockMvc.perform(get("/api/csrf"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("SUCCESS"))
                .andExpect(jsonPath("$.msg").isNotEmpty())
                .andExpect(jsonPath("$.data.headerName").value("X-CSRF-TOKEN"))
                .andExpect(jsonPath("$.data.token").isNotEmpty())
                .andReturn();

        String token = JsonPath.read(csrfResult.getResponse().getContentAsString(), "$.data.token");
        MockHttpSession session = (MockHttpSession) csrfResult.getRequest().getSession(false);

        mockMvc.perform(post("/api/users").session(session)
                .header("X-CSRF-TOKEN", token)
                .contentType(MediaType.APPLICATION_JSON)
                .content(createUserJson("Ada", "Lovelace", "ada@example.com",
                        "correct horse battery staple")))
                .andExpect(status().isCreated());

        mockMvc.perform(post("/api/users").session(session)
                .header("X-CSRF-TOKEN", "incorrect-token")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{}"))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").value("INVALID_CSRF_TOKEN"))
                .andExpect(jsonPath("$.msg").value("Invalid or missing CSRF token."))
                .andExpect(jsonPath("$.data").value(nullValue()));
    }

    @Test
    void postWithoutCsrfTokenIsRejected() throws Exception {
        mockMvc.perform(post("/api/users")
                .contentType(MediaType.APPLICATION_JSON)
                .content(createUserJson("Ada", "Lovelace", "ada@example.com",
                        "correct horse battery staple")))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").value("INVALID_CSRF_TOKEN"))
                .andExpect(jsonPath("$.msg").value("Invalid or missing CSRF token."))
                .andExpect(jsonPath("$.data").value(nullValue()));
    }

    @Test
    void jsonEndpointsRejectFormEncodedBodies() throws Exception {
        mockMvc.perform(post("/api/users").with(csrf())
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .param("firstName", "Ada"))
                .andExpect(status().isUnsupportedMediaType())
                .andExpect(jsonPath("$.code").value("UNSUPPORTED_MEDIA_TYPE"))
                .andExpect(jsonPath("$.msg").value("Content-Type must be application/json."))
                .andExpect(jsonPath("$.data").value(nullValue()));
    }

    @Test
    void malformedJsonReturnsBadRequest() throws Exception {
        mockMvc.perform(post("/api/users").with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content("{"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("MALFORMED_JSON"))
                .andExpect(jsonPath("$.msg").value("Malformed JSON request."))
                .andExpect(jsonPath("$.data").value(nullValue()));
    }

    @Test
    void registerLoginAndListUsersWithoutExposingPassword() throws Exception {
        register("ada@example.com", "correct horse battery staple");
        MockHttpSession session = login("ada@example.com", "correct horse battery staple");

        mockMvc.perform(get("/api/users").session(session))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("SUCCESS"))
                .andExpect(jsonPath("$.data[0].email").value("ada@example.com"))
                .andExpect(jsonPath("$.data[0]", not(hasKey("password"))))
                .andExpect(jsonPath("$.data[0]", not(hasKey("passwordHash"))))
                .andExpect(jsonPath("$.data[0]", not(hasKey("tempPassword"))));
    }

    @Test
    void duplicateEmailReturnsConflict() throws Exception {
        register("ada@example.com", "correct horse battery staple");

        mockMvc.perform(post("/api/users").with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(createUserJson("Ada", "Lovelace", "ada@example.com",
                        "another secure password")))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value("EMAIL_ALREADY_REGISTERED"))
                .andExpect(jsonPath("$.msg").value("Email is already registered."))
                .andExpect(jsonPath("$.data").value(nullValue()));
    }

    @Test
    void invalidLoginDoesNotRevealWhichCredentialFailed() throws Exception {
        mockMvc.perform(post("/api/sessions").with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(loginJson("missing@example.com", "wrong password")))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value("INVALID_CREDENTIALS"))
                .andExpect(jsonPath("$.msg").value("Invalid email or password."))
                .andExpect(jsonPath("$.data").value(nullValue()));
    }

    @Test
    void authenticatedUserCanUpdateAndDeleteOwnAccount() throws Exception {
        register("ada@example.com", "correct horse battery staple");
        MockHttpSession session = login("ada@example.com", "correct horse battery staple");
        Integer id = jdbcTemplate.queryForObject(
                "SELECT id_user FROM users WHERE email = ?", Integer.class, "ada@example.com");

        mockMvc.perform(put("/api/users/{id}", id).session(session).with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(updateUserJson("Augusta Ada", "King", "new correct horse battery staple")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("SUCCESS"))
                .andExpect(jsonPath("$.data.firstName").value("Augusta Ada"))
                .andExpect(jsonPath("$.data.lastName").value("King"))
                .andExpect(jsonPath("$.data", not(hasKey("passwordHash"))));

        mockMvc.perform(delete("/api/users/{id}", id).session(session).with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("SUCCESS"))
                .andExpect(jsonPath("$.data").value(nullValue()));
    }

    @Test
    void authenticatedUserCannotModifyAnotherAccount() throws Exception {
        register("ada@example.com", "correct horse battery staple");
        MockHttpSession session = login("ada@example.com", "correct horse battery staple");
        register("grace@example.com", "another correct horse battery staple");
        Integer otherId = jdbcTemplate.queryForObject(
                "SELECT id_user FROM users WHERE email = ?", Integer.class, "grace@example.com");

        mockMvc.perform(put("/api/users/{id}", otherId).session(session).with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(updateUserJson("Grace", "Hopper", "a new secure password")))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").value("USER_MODIFICATION_FORBIDDEN"))
                .andExpect(jsonPath("$.msg").value("You cannot modify this user."));

        mockMvc.perform(delete("/api/users/{id}", otherId).session(session).with(csrf()))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").value("USER_MODIFICATION_FORBIDDEN"))
                .andExpect(jsonPath("$.msg").value("You cannot modify this user."));
    }

    @Test
    void anonymousUserCannotUpdateOrDeleteUsers() throws Exception {
        mockMvc.perform(put("/api/users/1").with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(updateUserJson("Ada", "Lovelace", "a new secure password")))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value("AUTHENTICATION_REQUIRED"));

        mockMvc.perform(delete("/api/users/1").with(csrf()))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value("AUTHENTICATION_REQUIRED"));

        mockMvc.perform(delete("/api/sessions/current").with(csrf()))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value("AUTHENTICATION_REQUIRED"));
    }

    @Test
    void logoutInvalidatesAuthenticatedSession() throws Exception {
        register("ada@example.com", "correct horse battery staple");
        MockHttpSession session = login("ada@example.com", "correct horse battery staple");

        mockMvc.perform(delete("/api/sessions/current").session(session).with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("SUCCESS"))
                .andExpect(jsonPath("$.data").value(nullValue()));

        mockMvc.perform(get("/api/users").session(session))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void protectedPageRedirectsAnonymousUserToLogin() throws Exception {
        mockMvc.perform(get("/welcome"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/"));
    }

    @Test
    void pageControllersResolveJspViews() throws Exception {
        mockMvc.perform(get("/"))
                .andExpect(status().isOk())
                .andExpect(model().attribute("pageTitle", "User Processes"))
                .andExpect(view().name("index"))
                .andExpect(forwardedUrl("/WEB-INF/jsp/index.jsp"));

        register("ada@example.com", "correct horse battery staple");
        MockHttpSession session = login("ada@example.com", "correct horse battery staple");
        mockMvc.perform(get("/welcome").session(session))
                .andExpect(status().isOk())
                .andExpect(model().attribute("pageTitle", "User Management"))
                .andExpect(model().attribute("authenticated", true))
                .andExpect(view().name("welcome"))
                .andExpect(forwardedUrl("/WEB-INF/jsp/welcome.jsp"));
    }

    @Test
    void forgottenPasswordAlwaysReturnsGenericResponse() throws Exception {
        mockMvc.perform(post("/api/password-reset-requests").with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"email\":\"missing@example.com\"}"))
                .andExpect(status().isAccepted())
                .andExpect(content().contentTypeCompatibleWith("application/json"))
                .andExpect(jsonPath("$.code").value("SUCCESS"))
                .andExpect(jsonPath("$.msg").value(
                        "If the account exists, password reset instructions will be sent."))
                .andExpect(jsonPath("$.data").value(nullValue()));
    }

    private void register(String email, String password) throws Exception {
        mockMvc.perform(post("/api/users").with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(createUserJson("Ada", "Lovelace", email, password)))
                .andExpect(status().isCreated())
                .andExpect(header().exists("Location"))
                .andExpect(jsonPath("$.code").value("SUCCESS"))
                .andExpect(jsonPath("$.data.email").value(email));
    }

    private String createUserJson(String firstName, String lastName, String email, String password) {
        return String.format(
                "{\"firstName\":%s,\"lastName\":%s,\"email\":%s,\"password\":%s}",
                jsonString(firstName), jsonString(lastName), jsonString(email), jsonString(password));
    }

    private String updateUserJson(String firstName, String lastName, String password) {
        return String.format("{\"firstName\":%s,\"lastName\":%s,\"password\":%s}",
                jsonString(firstName), jsonString(lastName), jsonString(password));
    }

    private String jsonString(String value) {
        return value == null ? "null" : "\"" + value + "\"";
    }

    private MockHttpSession login(String email, String password) throws Exception {
        MvcResult result = mockMvc.perform(post("/api/sessions").with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(loginJson(email, password)))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", endsWith("/api/sessions/current")))
                .andExpect(jsonPath("$.code").value("SUCCESS"))
                .andExpect(jsonPath("$.data").value(nullValue()))
                .andReturn();
        HttpSession session = result.getRequest().getSession(false);
        return (MockHttpSession) session;
    }

    private String loginJson(String email, String password) {
        return String.format("{\"email\":%s,\"password\":%s}",
                jsonString(email), jsonString(password));
    }
}
