package com.trikaar.module.auth.repository;

import com.trikaar.module.auth.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface UserRepository extends JpaRepository<User, UUID> {

    Optional<User> findByUsernameAndBusinessIdAndDeletedFalse(String username, UUID businessId);

    Optional<User> findByEmailAndBusinessIdAndDeletedFalse(String email, UUID businessId);

    Optional<User> findByIdAndBusinessIdAndDeletedFalse(UUID id, UUID businessId);

    boolean existsByEmailAndBusinessIdAndDeletedFalse(String email, UUID businessId);

    boolean existsByUsernameAndBusinessIdAndDeletedFalse(String username, UUID businessId);
}
