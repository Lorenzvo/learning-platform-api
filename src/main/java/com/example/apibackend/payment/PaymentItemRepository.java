package com.example.apibackend.payment;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PaymentItemRepository extends JpaRepository<PaymentItem, Long> {
    List<PaymentItem> findAllByPaymentId(Long paymentId);
}
