package userprocesses.web.response;

import userprocesses.model.User;

public final class UserResponse {
    private final long idUser;
    private final String firstName;
    private final String lastName;
    private final String email;

    public UserResponse(User user) {
        this.idUser = user.getId();
        this.firstName = user.getFirstName();
        this.lastName = user.getLastName();
        this.email = user.getEmail();
    }

    public long getIdUser() {
        return idUser;
    }

    public String getFirstName() {
        return firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public String getEmail() {
        return email;
    }
}
