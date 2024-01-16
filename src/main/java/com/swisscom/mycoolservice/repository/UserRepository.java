package com.swisscom.mycoolservice.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.swisscom.mycoolservice.entity.UserEntity;

/** Repository interface for managing UserEntity entities in the database. */
@Repository("userRepository")
public interface UserRepository extends JpaRepository<UserEntity, String> {
}
