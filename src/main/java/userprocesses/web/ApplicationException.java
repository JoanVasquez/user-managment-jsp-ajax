package userprocesses.web;

import org.springframework.http.HttpStatus;

import userprocesses.web.response.ApiCode;

public final class ApplicationException extends RuntimeException {
    private static final long serialVersionUID = 1L;

    private final HttpStatus status;
    private final ApiCode code;

    private ApplicationException(HttpStatus status, ApiCode code, String message) {
        super(message);
        this.status = status;
        this.code = code;
    }

    public static ApplicationException badRequest(ApiCode code, String message) {
        return new ApplicationException(HttpStatus.BAD_REQUEST, code, message);
    }

    public static ApplicationException unauthorized(ApiCode code, String message) {
        return new ApplicationException(HttpStatus.UNAUTHORIZED, code, message);
    }

    public static ApplicationException forbidden(ApiCode code, String message) {
        return new ApplicationException(HttpStatus.FORBIDDEN, code, message);
    }

    public static ApplicationException notFound(ApiCode code, String message) {
        return new ApplicationException(HttpStatus.NOT_FOUND, code, message);
    }

    public static ApplicationException conflict(ApiCode code, String message) {
        return new ApplicationException(HttpStatus.CONFLICT, code, message);
    }

    public HttpStatus getStatus() {
        return status;
    }

    public ApiCode getCode() {
        return code;
    }
}
