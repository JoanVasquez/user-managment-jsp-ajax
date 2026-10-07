package userprocesses.security;

import java.util.Collections;

import org.springframework.security.core.authority.SimpleGrantedAuthority;

public final class AuthenticatedUser extends org.springframework.security.core.userdetails.User {
    private static final long serialVersionUID = 1L;

    private final long id;

    public AuthenticatedUser(long id, String email) {
        super(email, "", Collections.singletonList(new SimpleGrantedAuthority("ROLE_USER")));
        this.id = id;
    }

    public long getId() {
        return id;
    }
}
