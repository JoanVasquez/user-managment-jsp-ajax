package userprocesses.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import userprocesses.model.User;

public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByEmail(String email);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("update User user set user.firstName = :firstName, user.lastName = :lastName, "
            + "user.passwordHash = :passwordHash where user.id = :id")
    int updateAccount(@Param("id") long id,
            @Param("firstName") String firstName,
            @Param("lastName") String lastName,
            @Param("passwordHash") String passwordHash);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("delete from User user where user.id = :id")
    int deleteAccount(@Param("id") long id);
}
