package com.graphflix.userservice.repository;

import java.util.Optional;

import org.springframework.data.neo4j.repository.Neo4jRepository;
import org.springframework.data.neo4j.repository.query.Query;
import org.springframework.stereotype.Repository;

import com.graphflix.userservice.model.User;

@Repository
public interface UserRepository extends Neo4jRepository<User, String> {

    Optional<User> findByEmail(String email);

    @Query("MATCH (u:User {email: $email}) RETURN u.twoFactorEnabled")
    boolean isTwoFactorEnabled(String email);

    @Query("MATCH (u:User {email: $email}) RETURN u.totpSecret")
    String getTotpSecret(String email);

    @Query("MATCH (u:User {email: $email}) RETURN u.tokenVersion")
    int getTokenVersion(String email);

    @Query("MATCH (u:User {email: $email}) SET u.twoFactorEnabled = $enabled, u.tokenVersion = u.tokenVersion + 1")
    void setTwoFactorEnabled(String email, boolean enabled);

    @Query("MATCH (u:User {email: $email}) SET u.totpSecret = $secret")
    void setTotpSecret(String email, String secret);
}
