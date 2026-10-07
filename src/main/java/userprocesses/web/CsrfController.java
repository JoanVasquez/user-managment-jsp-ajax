package userprocesses.web;

import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import userprocesses.web.response.CsrfResponse;
import userprocesses.web.response.ApiResponse;

@RestController
public class CsrfController {

    @GetMapping("/api/csrf")
    public ApiResponse<CsrfResponse> csrf(CsrfToken csrfToken) {
        return ApiResponse.success("CSRF token issued.", new CsrfResponse(csrfToken));
    }
}
