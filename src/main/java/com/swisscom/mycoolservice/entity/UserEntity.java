package com.swisscom.mycoolservice.entity;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;

import lombok.Data;

@Entity
@Data
public class UserEntity {
    @Id
    @NotBlank
    private String userName;
    @NotBlank
    @Email
    private String email;
}
