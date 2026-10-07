package userprocesses.web.request;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public final class PasswordResetRequest {
    private final String email;

    @JsonCreator
    public PasswordResetRequest(@JsonProperty("email") String email) {
        this.email = email;
    }

    public String getEmail() {
        return email;
    }
}
