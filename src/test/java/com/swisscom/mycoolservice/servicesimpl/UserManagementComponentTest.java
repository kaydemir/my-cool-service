package com.swisscom.mycoolservice.servicesimpl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import com.swisscom.mycoolservice.beans.User;
import com.swisscom.mycoolservice.entity.UserEntity;
import com.swisscom.mycoolservice.exception.DuplicateBeanException;
import com.swisscom.mycoolservice.repository.UserRepository;
import com.swisscom.mycoolservice.util.UserConverter;

@SpringBootTest
public class UserManagementComponentTest {

    @Mock
    private UserRepository userRepository;


    private UserManagementComponent subject;

    @BeforeEach
    public void setUp(){
        MockitoAnnotations.initMocks(this);
        subject = new UserManagementComponent(userRepository);
    }

    @Test
    void testGetAllUsers() {
        // setup
        List<UserEntity> userEntities = Collections.singletonList(new UserEntity("user1", "user1@example.com"));
        List<User> expectedUsers = UserConverter.convertToUserBeans(userEntities);

        when(userRepository.findAll()).thenReturn(userEntities);

        // when
        ResponseEntity<List<User>> responseEntity = subject.getAllUsers();

        // then
        assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
        assertEquals(expectedUsers, responseEntity.getBody());
        verify(userRepository, times(1)).findAll();
    }

    @Test
    void testCreateUserSuccess() {
        // setup
        User user = new User("newUser", "newuser@example.com");
        UserEntity userEntity = UserConverter.convertToUserEntity(user);
        // when
        when(userRepository.findById(user.getUserName())).thenReturn(Optional.empty());
        when(userRepository.save(any(UserEntity.class))).thenReturn(userEntity);

        ResponseEntity<Boolean> responseEntity = subject.createUser(user);

        // then
        assertEquals(HttpStatus.CREATED, responseEntity.getStatusCode());
        assertTrue(responseEntity.getBody());
        verify(userRepository, times(1)).findById(user.getUserName());
        verify(userRepository, times(1)).save(userEntity);
    }

    @Test
    void testCreateUserDuplicate() {
        // setup
        User user = new User("existingUser", "existinguser@example.com");
        // when
        when(userRepository.findById(user.getUserName())).thenReturn(Optional.of(new UserEntity()));

        // then
        assertThrows(DuplicateBeanException.class, () -> subject.createUser(user));
        verify(userRepository, times(1)).findById(user.getUserName());
        verify(userRepository, never()).save(any(UserEntity.class));
    }

    @Test
    void testCreateUserFailure() {
        User user = new User("newUser", "newuser@example.com");
        UserEntity userEntity = UserConverter.convertToUserEntity(user);
        when(userRepository.findById(user.getUserName())).thenReturn(Optional.of(userEntity));
        when(userRepository.save(any(UserEntity.class))).thenThrow(new RuntimeException("Test Exception"));
        assertThrows(RuntimeException.class, () -> subject.createUser(user));
        verify(userRepository, times(1)).findById(user.getUserName());
    }

    @Test
    void testCreateUserDuplicateException() {
        User user = new User("existingUser", "existinguser@example.com");
        when(userRepository.findById(user.getUserName())).thenReturn(Optional.of(new UserEntity()));
        when(userRepository.save(any(UserEntity.class))).thenThrow(new RuntimeException("Test Exception"));
        assertThrows(RuntimeException.class, () -> subject.createUser(user));
        verify(userRepository, times(1)).findById(user.getUserName());
        verify(userRepository, never()).save(any(UserEntity.class));
    }
}
