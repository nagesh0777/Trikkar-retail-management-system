package com.trikaar.module.employee.repository;

import com.trikaar.module.employee.entity.Employee;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface EmployeeRepository extends JpaRepository<Employee, UUID> {

    Page<Employee> findAllByBusinessIdAndDeletedFalse(UUID businessId, Pageable pageable);

    Optional<Employee> findByIdAndBusinessIdAndDeletedFalse(UUID id, UUID businessId);

    Optional<Employee> findByEmployeeCodeAndBusinessIdAndDeletedFalse(String employeeCode, UUID businessId);

    boolean existsByEmployeeCodeAndBusinessIdAndDeletedFalse(String employeeCode, UUID businessId);

    boolean existsByEmailAndBusinessIdAndDeletedFalse(String email, UUID businessId);

    @Query("SELECT e FROM Employee e WHERE e.businessId = :businessId AND e.deleted = false " +
            "AND (LOWER(e.firstName) LIKE LOWER(CONCAT('%', :search, '%')) " +
            "OR LOWER(e.lastName) LIKE LOWER(CONCAT('%', :search, '%')) " +
            "OR LOWER(e.employeeCode) LIKE LOWER(CONCAT('%', :search, '%')))")
    Page<Employee> searchEmployees(@Param("businessId") UUID businessId,
            @Param("search") String search,
            Pageable pageable);

    long countByBusinessIdAndStatusAndDeletedFalse(UUID businessId, Employee.EmployeeStatus status);
}
