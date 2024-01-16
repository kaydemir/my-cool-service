package com.swisscom.mycoolservice.services;

import java.util.List;

import org.springframework.http.ResponseEntity;

import com.swisscom.mycoolservice.beans.User;

/**
 * This interface defines operations specific to the User Service.
 */
public interface UserService {
    /**
     * Retrieves a list of all users.
     *
     * @return ResponseEntity containing a list of users if successful, or an error response otherwise.
     */
    ResponseEntity<List<User>> getAllUsers();
    /**
     * Creates a new user.
     *
     * @param user The User object representing the user to be created.
     * @return ResponseEntity indicating success or failure of the user creation process.
     */
    ResponseEntity<Boolean> createUser(User user);
}
