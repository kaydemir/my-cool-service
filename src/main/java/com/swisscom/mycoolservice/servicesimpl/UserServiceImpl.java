package com.swisscom.mycoolservice.servicesimpl;

import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import com.swisscom.mycoolservice.beans.User;
import com.swisscom.mycoolservice.services.UserService;

@Service
public class UserServiceImpl implements UserService {

    private static final Logger logger = LogManager.getLogger(UserServiceImpl.class);

    private final UserManagementComponent userManagementComponent;

    @Autowired
    UserServiceImpl(UserManagementComponent userManagementComponent) {
        this.userManagementComponent = userManagementComponent;
        logger.trace("{} initialized", this.getClass().getName());
    }


    @Override
    public ResponseEntity<List<User>> getAllUsers() {
        return userManagementComponent.getAllUsers();
    }

    @Override
    public ResponseEntity<Boolean> createUser(User user) {
        return userManagementComponent.createUser(user);
    }
}
