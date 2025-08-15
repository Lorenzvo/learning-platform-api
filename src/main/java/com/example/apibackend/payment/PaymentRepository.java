package com.example.apibackend.payment;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import com.example.apibackend.payment.Payment.PaymentStatus;

import java.util.List;
import java.util.Optional;

@Repository
public interface PaymentRepository extends JpaRepository<Payment, Long> {

    Optional<Payment> findByGatewayTxnId(String gatewayTxnId);

    List<Payment> findByUserIdOrderByCreatedAtDesc(Long userId);

    Page<Payment> findByCourseId(Long courseId, Pageable pageable);

    long countByCourseIdAndStatus(Long courseId, PaymentStatus status);

    @Query("select coalesce(sum(p.amountCents),0) from Payment p where p.course.id = :courseId and p.status = :status")
    long sumAmountCentsByCourseAndStatus(Long courseId, PaymentStatus status);

    @Query("select coalesce(sum(p.amountCents),0) from Payment p where p.status = :status")
    Double sumAmountByStatus(@Param("status") PaymentStatus status);

    // Finds the most recent PENDING payment for a user and course (idempotency for rapid retries)
    Optional<Payment> findTopByUserIdAndCourseIdAndStatusOrderByCreatedAtDesc(Long userId, Long courseId, PaymentStatus status);
}