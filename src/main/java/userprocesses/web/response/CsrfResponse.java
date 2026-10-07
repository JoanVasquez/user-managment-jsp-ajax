package userprocesses.web.response;

import org.springframework.security.web.csrf.CsrfToken;

public final class CsrfResponse {
    private final String headerName;
    private final String token;

    public CsrfResponse(CsrfToken csrfToken) {
        this.headerName = csrfToken.getHeaderName();
        this.token = csrfToken.getToken();
    }

    public String getHeaderName() {
        return headerName;
    }

    public String getToken() {
        return token;
    }
}
