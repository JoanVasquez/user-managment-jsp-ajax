package userprocesses.web.response;

import java.util.Objects;

public final class ApiResponse<T> {
    private final ApiCode code;
    private final String msg;
    private final T data;

    private ApiResponse(ApiCode code, String msg, T data) {
        this.code = code;
        this.msg = msg;
        this.data = data;
    }

    public static <T> ApiResponse<T> success(String msg, T data) {
        return new ApiResponse<>(ApiCode.SUCCESS, Objects.requireNonNull(msg, "msg"), data);
    }

    public static ApiResponse<Void> error(ApiCode code, String msg) {
        ApiCode errorCode = Objects.requireNonNull(code, "code");
        if (errorCode == ApiCode.SUCCESS) {
            throw new IllegalArgumentException("SUCCESS cannot be used for an error response.");
        }
        return new ApiResponse<>(errorCode, Objects.requireNonNull(msg, "msg"), null);
    }

    public ApiCode getCode() {
        return code;
    }

    public String getMsg() {
        return msg;
    }

    public T getData() {
        return data;
    }
}
