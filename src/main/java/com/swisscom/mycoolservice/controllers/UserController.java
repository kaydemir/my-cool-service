package com.swisscom.mycoolservice.controllers;

import static com.swisscom.mycoolservice.controllers.UserController.ENDPOINT;

import java.util.List;

import javax.validation.Valid;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.swisscom.mycoolservice.beans.User;
import com.swisscom.mycoolservice.services.UserService;

/**
 * Controller class for handling user-related operations.
 * All endpoints are under the base path "/api/users" and produce JSON responses.
 */
@RestController
@RequestMapping(value = ENDPOINT, produces = { MediaType.APPLICATION_JSON_VALUE })
public class UserController {
    /**
     * Base path for all endpoints in this controller.
     */
    public static final String ENDPOINT = "/api/users";

    private static final Logger logger = LogManager.getLogger(UserController.class);
    private static final String RETURNING_RESPONSE = "Returning response {}";

    private UserService userService;

    @Autowired
    public UserController(UserService userService) {
        this.userService = userService;
        logger.trace("{} initialized", this.getClass().getName());
    }
    /**
     * Endpoint to retrieve a list of all users.
     *
     * @return ResponseEntity containing a list of users if successful, or an error response otherwise.
     */
    @GetMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<User>> getUsers() {
        logger.debug("GET /api/users: getUsers()");
        ResponseEntity<List<User>> response = userService.getAllUsers();
        logger.debug(RETURNING_RESPONSE, response);
        return response;
    }
    /**
     * Endpoint to create a new user.
     *
     * @param user The User object representing the user to be created.
     * @return ResponseEntity indicating success or failure of the user creation process.
     */
    @PostMapping()
    @PreAuthorize("isAuthenticated()")
//    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public ResponseEntity<Boolean> createUser(@RequestBody @Valid final User user){
        logger.debug("POST /api/users: createUser({})", user);
        ResponseEntity<Boolean> response = userService.createUser(user);
        logger.debug(RETURNING_RESPONSE, response);
        return response;
    }
}
