package com.swisscom.mycoolservice.servicesimpl;

import java.util.List;
import java.util.Optional;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

import com.swisscom.mycoolservice.beans.User;
import com.swisscom.mycoolservice.entity.UserEntity;
import com.swisscom.mycoolservice.exception.DuplicateBeanException;
import com.swisscom.mycoolservice.repository.UserRepository;
import com.swisscom.mycoolservice.util.UserConverter;
/**
 * this class is responsible to user management operation
 *  */
@Component
public class UserManagementComponent {

    private static final Logger logger = LogManager.getLogger(UserManagementComponent.class);
    // any feature that specific for user in the system should be autowired here
    // e.g. enhanced password validation, revoke user's token, send mail to user etc.
    private UserRepository userRepository;

    @Autowired
    UserManagementComponent(UserRepository userRepository) {
        this.userRepository = userRepository;
        logger.trace("{} initialized", this.getClass().getName());
    }

    public ResponseEntity<List<User>> getAllUsers() {
        List<UserEntity> userEntities = userRepository.findAll();
        List<User> users = UserConverter.convertToUserBeans(userEntities);
        return ResponseEntity.ok(users);
    }

    public ResponseEntity<Boolean> createUser(User user) {
            UserEntity userEntity = UserConverter.convertToUserEntity(user);
        try {
            Optional<UserEntity> byId = userRepository.findById(user.getUserName());
            if (byId.isPresent()) {
                String exceptionMessage = String.format("User %s already exists", byId.get());
                throw new DuplicateBeanException(exceptionMessage);
            }
            userRepository.save(userEntity);
            return ResponseEntity.status(HttpStatus.CREATED).body(true);
        } catch (DuplicateBeanException e){
            throw e;
        } catch (Exception e) {
            logger.error("Exception while saving user {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(false);
        }
    }
}
