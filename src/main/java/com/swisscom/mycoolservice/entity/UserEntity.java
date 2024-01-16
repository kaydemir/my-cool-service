package com.swisscom.mycoolservice.entity;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Represents a persistent entity for storing user information in a database.(H2 in memory database)
 */
@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
public class UserEntity {
    @Id
    @NotBlank
    private String userName;
    @NotBlank
    @Email
    private String email;
}
