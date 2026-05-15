package com.connectsphere.payment.repository;

import com.connectsphere.payment.entity.Payment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface PaymentRepository extends JpaRepository<Payment, Long> {
    List<Payment> findByUserIdOrderByCreatedAtDesc(Long userId);
    Optional<Payment> findByRazorpayOrderId(String razorpayOrderId);
}