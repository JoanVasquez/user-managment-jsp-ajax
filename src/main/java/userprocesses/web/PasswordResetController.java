package userprocesses.web;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import userprocesses.service.UserService;
import userprocesses.web.request.PasswordResetRequest;
import userprocesses.web.response.ApiResponse;

@RestController
@RequestMapping("/api/password-reset-requests")
public class PasswordResetController {
    private static final String RESET_MESSAGE =
            "If the account exists, password reset instructions will be sent.";

    private final UserService userService;

    public PasswordResetController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<ApiResponse<Void>> create(@RequestBody PasswordResetRequest body) {
        userService.requestPasswordReset(body.getEmail());
        return ResponseEntity.status(HttpStatus.ACCEPTED)
                .body(ApiResponse.success(RESET_MESSAGE, null));
    }
}
