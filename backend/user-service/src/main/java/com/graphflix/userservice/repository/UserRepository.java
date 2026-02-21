package com.graphflix.userservice.repository;

import java.util.Optional;

import org.springframework.data.neo4j.repository.Neo4jRepository;
import org.springframework.data.neo4j.repository.query.Query;
import org.springframework.stereotype.Repository;

import com.graphflix.userservice.model.User;

@Repository
public interface UserRepository extends Neo4jRepository<User, String> {

    Optional<User> findByEmail(String email);

    @Query("MATCH (u:User {id: $userId}) RETURN u.twoFactorEnabled")
    boolean isTwoFactorEnabled(String userId);

    @Query("MATCH (u:User {id: $userId}) RETURN u.totpSecret")
    String getTotpSecret(String userId);

    @Query("MATCH (u:User {id: $userId}) RETURN u.tokenVersion")
    int getTokenVersion(String userId);

    @Query("MATCH (u:User {id: $userId}) SET u.twoFactorEnabled = $enabled, u.tokenVersion = u.tokenVersion + 1")
    void setTwoFactorEnabled(String userId, boolean enabled);

    @Query("MATCH (u:User {id: $userId}) SET u.totpSecret = $secret")
    void setTotpSecret(String userId, String secret);
}
