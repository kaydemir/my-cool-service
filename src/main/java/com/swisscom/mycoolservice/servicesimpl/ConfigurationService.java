package com.swisscom.mycoolservice.servicesimpl;

import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import com.swisscom.mycoolservice.properties.ApplicationProperties;
import com.swisscom.mycoolservice.properties.ApplicationProperties.User;
/**
 * Service class for retrieving configuration and user information based on the authenticated user.
 */
@Service
public class ConfigurationService {

    @Autowired
    private ApplicationProperties properties;

    /**
     * This method provide Authenticated user class.
     * */
    public User getCurrentUser() {
        String currentUsername = SecurityContextHolder.getContext().getAuthentication().getName();
        return properties.getUsers().get(currentUsername);
    }
    /**
     * get all users
     * */
    public Map<String, User> getUsers() {
        return properties.getUsers();
    }

    /**
     * This method provide Authenticated user name.
     * */
    public String getCurrentUserName() {
        return SecurityContextHolder.getContext().getAuthentication().getName();
    }

}
