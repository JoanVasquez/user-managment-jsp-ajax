package userprocesses.web;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.security.web.authentication.logout.SecurityContextLogoutHandler;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import userprocesses.model.User;
import userprocesses.security.AuthenticatedUser;
import userprocesses.service.UserService;
import userprocesses.web.request.LoginRequest;
import userprocesses.web.response.ApiResponse;

@RestController
@RequestMapping("/api/sessions")
public class SessionController {
    private final UserService userService;

    public SessionController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<ApiResponse<Void>> create(
            @RequestBody LoginRequest body,
            HttpServletRequest request) {
        User user = userService.authenticate(body.getEmail(), body.getPassword());
        authenticate(request, user);
        return ResponseEntity.created(ServletUriComponentsBuilder.fromContextPath(request)
                .path("/api/sessions/current")
                .build()
                .toUri())
                .body(ApiResponse.success("Session created.", null));
    }

    @DeleteMapping("/current")
    public ApiResponse<Void> delete(HttpServletRequest request, HttpServletResponse response) {
        new SecurityContextLogoutHandler().logout(
                request, response, SecurityContextHolder.getContext().getAuthentication());
        return ApiResponse.success("Session deleted.", null);
    }

    private void authenticate(HttpServletRequest request, User user) {
        HttpSession existingSession = request.getSession(false);
        if (existingSession != null) {
            request.changeSessionId();
        }
        AuthenticatedUser principal = new AuthenticatedUser(user.getId(), user.getEmail());
        UsernamePasswordAuthenticationToken authentication =
                new UsernamePasswordAuthenticationToken(principal, null, principal.getAuthorities());
        SecurityContext context = SecurityContextHolder.createEmptyContext();
        context.setAuthentication(authentication);
        SecurityContextHolder.setContext(context);
        request.getSession(true).setAttribute(
                HttpSessionSecurityContextRepository.SPRING_SECURITY_CONTEXT_KEY, context);
    }
}
