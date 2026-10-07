package userprocesses.security;

import java.io.IOException;
import javax.servlet.http.HttpServletResponse;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.csrf.CsrfException;
import org.springframework.security.web.csrf.HttpSessionCsrfTokenRepository;

import userprocesses.web.response.ApiCode;
import userprocesses.web.response.ApiResponse;

@Configuration
public class SecurityConfig {

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http, ObjectMapper objectMapper) throws Exception {
        HttpSessionCsrfTokenRepository csrfRepository = new HttpSessionCsrfTokenRepository();
        csrfRepository.setHeaderName("X-CSRF-TOKEN");
        http
                .authorizeRequests(authorize -> authorize
                        .antMatchers("/", "/index", "/resources/**", "/api/csrf", "/error").permitAll()
                        .antMatchers(HttpMethod.POST, "/api/users").permitAll()
                        .antMatchers(HttpMethod.POST, "/api/sessions").permitAll()
                        .antMatchers(HttpMethod.POST, "/api/password-reset-requests").permitAll()
                        .anyRequest().authenticated())
                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED)
                        .sessionFixation().changeSessionId())
                .exceptionHandling(exceptions -> exceptions.authenticationEntryPoint((request, response, exception) -> {
                    if ((request.getContextPath() + "/welcome").equals(request.getRequestURI())) {
                        response.sendRedirect(request.getContextPath() + "/");
                    } else {
                        writeError(response, objectMapper, HttpServletResponse.SC_UNAUTHORIZED,
                                ApiCode.AUTHENTICATION_REQUIRED, "Authentication is required.");
                    }
                }).accessDeniedHandler((request, response, exception) -> {
                    if (exception instanceof CsrfException) {
                        writeError(response, objectMapper, HttpServletResponse.SC_FORBIDDEN,
                                ApiCode.INVALID_CSRF_TOKEN, "Invalid or missing CSRF token.");
                    } else {
                        writeError(response, objectMapper, HttpServletResponse.SC_FORBIDDEN,
                                ApiCode.ACCESS_DENIED, "Access is denied.");
                    }
                }))
                .headers(headers -> headers.contentTypeOptions().and().frameOptions().deny());
        http.csrf().csrfTokenRepository(csrfRepository);
        return http.build();
    }

    private void writeError(HttpServletResponse response, ObjectMapper objectMapper,
            int status, ApiCode code, String message) throws IOException {
        response.setStatus(status);
        response.setContentType("application/json");
        objectMapper.writeValue(response.getOutputStream(), ApiResponse.error(code, message));
    }
}
