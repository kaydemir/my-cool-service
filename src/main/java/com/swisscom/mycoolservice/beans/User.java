package com.swisscom.mycoolservice.beans;


import java.io.Serializable;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
/**
 * Represents a user entity with basic information.
 */
@Data
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PACKAGE, force = true)
public class User implements Serializable {

    private static final long serialVersionUID = 4352701795767518903L;
    /**
     * The username of the user.
     */
    @NotBlank
    private String userName;
    /**
     * The email address of the user.
     */
    @NotBlank
    @Email
    private String email;

}
