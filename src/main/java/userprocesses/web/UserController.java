package userprocesses.web;

import java.util.List;
import java.util.stream.Collectors;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.logout.SecurityContextLogoutHandler;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import userprocesses.model.User;
import userprocesses.security.AuthenticatedUser;
import userprocesses.service.UserService;
import userprocesses.web.request.CreateUserRequest;
import userprocesses.web.request.UpdateUserRequest;
import userprocesses.web.response.ApiCode;
import userprocesses.web.response.ApiResponse;
import userprocesses.web.response.UserResponse;

@RestController
@RequestMapping("/api/users")
public class UserController {
    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping
    public ApiResponse<List<UserResponse>> list(Authentication authentication) {
        List<UserResponse> users = userService.getVisibleUsers(principal(authentication).getId()).stream()
                .map(UserResponse::new)
                .collect(Collectors.toList());
        return ApiResponse.success("Users retrieved.", users);
    }

    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<ApiResponse<UserResponse>> create(
            @RequestBody CreateUserRequest body,
            HttpServletRequest request) {
        User created = userService.register(
                body.getFirstName(), body.getLastName(), body.getEmail(), body.getPassword());
        UserResponse response = new UserResponse(created);
        return ResponseEntity.created(ServletUriComponentsBuilder.fromContextPath(request)
                .path("/api/users/{id}")
                .buildAndExpand(response.getIdUser())
                .toUri())
                .body(ApiResponse.success("User created.", response));
    }

    @PutMapping(value = "/{id}", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ApiResponse<UserResponse> update(
            @PathVariable long id,
            @RequestBody UpdateUserRequest body,
            Authentication authentication) {
        AuthenticatedUser currentUser = principal(authentication);
        User updated = userService.update(
                currentUser.getId(), id, body.getFirstName(), body.getLastName(), body.getPassword());
        return ApiResponse.success("User updated.", new UserResponse(updated));
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Void> delete(
            @PathVariable long id,
            Authentication authentication,
            HttpServletRequest request,
            HttpServletResponse response) {
        AuthenticatedUser currentUser = principal(authentication);
        userService.delete(currentUser.getId(), id);
        new SecurityContextLogoutHandler().logout(request, response, authentication);
        return ApiResponse.success("User deleted.", null);
    }

    private AuthenticatedUser principal(Authentication authentication) {
        if (authentication == null || !(authentication.getPrincipal() instanceof AuthenticatedUser)) {
            throw ApplicationException.unauthorized(
                    ApiCode.AUTHENTICATION_REQUIRED, "Authentication is required.");
        }
        return (AuthenticatedUser) authentication.getPrincipal();
    }
}
