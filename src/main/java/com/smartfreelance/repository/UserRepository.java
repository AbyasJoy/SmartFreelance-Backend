package com.smartfreelance.repository;

import com.smartfreelance.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByEmail(String email);

    long countByActive(boolean active);

    long countByRole(com.smartfreelance.entity.Role role);
}
