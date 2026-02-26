package com.trikaar.module.admin.repository;

import com.trikaar.module.admin.entity.AdminConfig;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface AdminConfigRepository extends JpaRepository<AdminConfig, UUID> {

    List<AdminConfig> findAllByBusinessIdAndDeletedFalse(UUID businessId);

    List<AdminConfig> findAllByBusinessIdAndCategoryAndDeletedFalse(UUID businessId, String category);

    Optional<AdminConfig> findByConfigKeyAndBusinessIdAndDeletedFalse(String key, UUID businessId);

    boolean existsByConfigKeyAndBusinessIdAndDeletedFalse(String key, UUID businessId);
}
