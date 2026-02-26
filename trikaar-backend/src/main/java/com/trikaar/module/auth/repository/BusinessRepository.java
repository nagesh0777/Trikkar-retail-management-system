package com.trikaar.module.auth.repository;

import com.trikaar.module.auth.entity.Business;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface BusinessRepository extends JpaRepository<Business, UUID> {

    Optional<Business> findBySlugAndDeletedFalse(String slug);

    Optional<Business> findByIdAndDeletedFalse(UUID id);

    boolean existsBySlugAndDeletedFalse(String slug);

    boolean existsByRegistrationNumberAndDeletedFalse(String registrationNumber);
}
