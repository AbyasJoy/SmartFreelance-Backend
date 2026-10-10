package com.smartfreelance.repository;

import com.smartfreelance.entity.Payment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PaymentRepository extends JpaRepository<Payment, Long> {

    List<Payment> findByProjectId(Long projectId);

    Optional<Payment> findByMilestoneId(Long milestoneId);

    List<Payment> findByFreelancerId(Long freelancerId);

    @Query("SELECT p FROM Payment p JOIN Project proj ON p.projectId = proj.id WHERE proj.clientId = :clientId")
    List<Payment> findByClientId(@Param("clientId") Long clientId);

    long countByStatus(String status);

    @Query(value = "SELECT COALESCE(SUM(CAST(amount AS DECIMAL(12,2))), 0.0) FROM payments WHERE UPPER(status) = 'COMPLETED'", nativeQuery = true)
    Double sumCompletedPaymentAmount();
}
