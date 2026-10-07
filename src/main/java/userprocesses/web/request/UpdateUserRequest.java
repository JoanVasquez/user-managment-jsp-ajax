package userprocesses.web.request;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public final class UpdateUserRequest {
    private final String firstName;
    private final String lastName;
    private final String password;

    @JsonCreator
    public UpdateUserRequest(
            @JsonProperty("firstName") String firstName,
            @JsonProperty("lastName") String lastName,
            @JsonProperty("password") String password) {
        this.firstName = firstName;
        this.lastName = lastName;
        this.password = password;
    }

    public String getFirstName() {
        return firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public String getPassword() {
        return password;
    }
}
