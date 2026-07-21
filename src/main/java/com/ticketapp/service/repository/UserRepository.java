package com.ticketapp.service.repository;

import com.ticketapp.service.model.User;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.Optional;

/**
 * Repository for managing User documents.
 */
public interface UserRepository extends MongoRepository<User, String> {

    /**
     * Finds a user by their email address.
     */
    Optional<User> findByEmail(String email);

    /**
     * Checks if a user exists with the given email address.
     */
    boolean existsByEmail(String email);
}
