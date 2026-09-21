package com.github.dersolopes.eventmanagement.repository;

import com.github.dersolopes.eventmanagement.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface UserRepository extends JpaRepository<User, UUID> {

    Optional<User> findByEmail(String email);

    // Adicione esta linha para o Spring Data criar a query automaticamente
    boolean existsByEmail(String email);
}