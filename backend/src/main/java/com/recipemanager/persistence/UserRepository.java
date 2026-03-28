package com.recipemanager.persistence;

import static com.recipemanager.generated.tables.Users.USERS;

import com.recipemanager.generated.tables.records.UsersRecord;
import java.util.Optional;
import org.jooq.DSLContext;
import org.springframework.stereotype.Repository;

@Repository
public class UserRepository {

    private final DSLContext dsl;

    public UserRepository(DSLContext dsl) {
        this.dsl = dsl;
    }

    public record UserRow(long id, String email, String passwordHash, String username) {
    }

    public long insert(String email, String passwordHash, String username) {
        dsl.insertInto(USERS)
                .set(USERS.EMAIL, email)
                .set(USERS.PASSWORD_HASH, passwordHash)
                .set(USERS.USERNAME, username)
                .execute();
        return dsl.select(USERS.ID).from(USERS).where(USERS.EMAIL.eq(email)).fetchSingle(USERS.ID);
    }

    public Optional<UserRow> findByEmail(String email) {
        return dsl.selectFrom(USERS)
                .where(USERS.EMAIL.eq(email))
                .fetchOptional()
                .map(UserRepository::mapRow);
    }

    public boolean existsByEmail(String email) {
        return dsl.fetchExists(dsl.selectOne().from(USERS).where(USERS.EMAIL.eq(email)));
    }

    private static UserRow mapRow(UsersRecord r) {
        return new UserRow(r.getId(), r.getEmail(), r.getPasswordHash(), r.getUsername());
    }
}
