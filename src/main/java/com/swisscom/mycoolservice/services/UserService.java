package com.swisscom.mycoolservice.services;

import java.util.List;

import org.springframework.http.ResponseEntity;

import com.swisscom.mycoolservice.beans.User;

/**
 * User Service specific operations
 */
public interface UserService {
    ResponseEntity<List<User>> getAllUsers();
    ResponseEntity<Boolean> createUser(User user);
}
