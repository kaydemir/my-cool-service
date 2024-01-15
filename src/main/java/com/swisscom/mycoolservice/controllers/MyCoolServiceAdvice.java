package com.swisscom.mycoolservice.controllers;


import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.TypeMismatchException;
import org.springframework.beans.factory.BeanCreationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.NoHandlerFoundException;

import com.swisscom.mycoolservice.exception.DuplicateBeanException;

@RestControllerAdvice("com.swisscom.mycoolservice")
public class MyCoolServiceAdvice {

    private static final Logger logger = LogManager.getLogger(MyCoolServiceAdvice.class);

    @ExceptionHandler({ TypeMismatchException.class })
    public ResponseEntity<Object> handleTypeMismatchException(final TypeMismatchException ex) {
        logger.warn("{} - {}", HttpStatus.OK, ex.getMessage());
        return ResponseEntity.badRequest().body(ex.getMessage());
    }

    @ExceptionHandler({AccessDeniedException.class})
    public ResponseEntity<Object> handleRoleException(final AccessDeniedException ex) {
        logger.warn("{} - {}", HttpStatus.UNAUTHORIZED, ex.getMessage());
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("User does not have the permission to invoke this API");
    }

    @ExceptionHandler(NoHandlerFoundException.class)
    public ResponseEntity<String> handleNotFoundException(NoHandlerFoundException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Method not found");
    }

    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<String> handleBadCredentialsException(BadCredentialsException ex) {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Bad credentials");
    }

    @ExceptionHandler({ BeanCreationException.class })
    public ResponseEntity<Object> handleBeanCreationException(final BeanCreationException ex) {
        logger.error("Bean cannot be created {} ", ex.getBeanName());
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Exception details: " + ex.getMessage());
    }

    @ExceptionHandler({ DuplicateBeanException.class })
    public ResponseEntity<Object> handleDuplicateBeanException(final DuplicateBeanException ex) {
        logger.error("Bean already exists {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.CONFLICT).body("Exception details: " + ex.getMessage());
    }

    @ExceptionHandler({UsernameNotFoundException.class})
    public ResponseEntity<Object> handleUsernameNotFoundException(final UsernameNotFoundException ex) {
        logger.warn("{} - {}", HttpStatus.OK, ex.getMessage());
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body("user not found : " + ex.getMessage());
    }











}
