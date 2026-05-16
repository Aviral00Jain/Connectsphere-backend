package com.connectsphere.payment.service.impl;

import com.connectsphere.payment.dto.*;
import com.connectsphere.payment.entity.Payment;
import com.connectsphere.payment.enums.PaymentStatus;
import com.connectsphere.payment.exception.ResourceNotFoundException;
import com.connectsphere.payment.repository.PaymentRepository;
import com.connectsphere.payment.service.PaymentService;
import com.razorpay.Order;
import com.razorpay.RazorpayClient;
import com.razorpay.RazorpayException;
import com.razorpay.Utils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class PaymentServiceImpl implements PaymentService {

    private final PaymentRepository paymentRepository;
    private final RazorpayClient razorpayClient;

    @Value("${razorpay.key.id}")
    private String razorpayKeyId;

    @Value("${razorpay.key.secret}")
    private String razorpayKeySecret;

    @Value("${razorpay.currency:INR}")
    private String currency;

    @Override
    @Transactional
    public CreateOrderResponse createOrder(CreateOrderRequest request) {
        try {
            // Convert amount to paise (Razorpay uses smallest currency unit)
            int amountInPaise = (int) (request.getAmount() * 100);

            // Create Razorpay order
            JSONObject orderRequest = new JSONObject();
            orderRequest.put("amount", amountInPaise);
            orderRequest.put("currency", currency);
            orderRequest.put("receipt", "rcpt_" + UUID.randomUUID().toString().substring(0, 8));

            Order razorpayOrder = razorpayClient.orders.create(orderRequest);
            String razorpayOrderId = razorpayOrder.get("id");

            log.info("Created Razorpay order: {} for user: {} amount: {}",
                    razorpayOrderId, request.getUserId(), request.getAmount());

            // Save payment record with PENDING status
            Payment payment = Payment.builder()
                    .userId(request.getUserId())
                    .amount(request.getAmount())
                    .paymentType(request.getPaymentType())
                    .status(PaymentStatus.PENDING)
                    .transactionId(UUID.randomUUID().toString())
                    .razorpayOrderId(razorpayOrderId)
                    .createdAt(LocalDateTime.now())
                    .updatedAt(LocalDateTime.now())
                    .build();

            Payment saved = paymentRepository.save(payment);

            // Build description for Razorpay checkout display
            String description = switch (request.getPaymentType()) {
                case PREMIUM_SUBSCRIPTION -> "ConnectSphere Premium Subscription";
                case PROMOTED_POST -> "Promoted Post Boost";
                case DONATION -> "Donation to Creator";
            };

            return CreateOrderResponse.builder()
                    .paymentId(saved.getId())
                    .razorpayOrderId(razorpayOrderId)
                    .razorpayKeyId(razorpayKeyId)
                    .amountInPaise(amountInPaise)
                    .currency(currency)
                    .paymentType(request.getPaymentType().name())
                    .description(description)
                    .build();

        } catch (RazorpayException e) {
            log.error("Failed to create Razorpay order: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to create payment order: " + e.getMessage());
        }
    }

    @Override
    @Transactional
    public PaymentResponse verifyPayment(VerifyPaymentRequest request) {
        // Find the payment by Razorpay order ID
        Payment payment = paymentRepository.findByRazorpayOrderId(request.getRazorpayOrderId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Payment not found for order: " + request.getRazorpayOrderId()));

        try {
            // Verify the Razorpay signature using HMAC-SHA256
            JSONObject attributes = new JSONObject();
            attributes.put("razorpay_order_id", request.getRazorpayOrderId());
            attributes.put("razorpay_payment_id", request.getRazorpayPaymentId());
            attributes.put("razorpay_signature", request.getRazorpaySignature());

            boolean isValid = Utils.verifyPaymentSignature(attributes, razorpayKeySecret);

            if (isValid) {
                payment.setStatus(PaymentStatus.SUCCESS);
                payment.setRazorpayPaymentId(request.getRazorpayPaymentId());
                payment.setRazorpaySignature(request.getRazorpaySignature());
                log.info("Payment verified successfully: orderId={}, paymentId={}",
                        request.getRazorpayOrderId(), request.getRazorpayPaymentId());
            } else {
                payment.setStatus(PaymentStatus.FAILED);
                log.warn("Payment signature verification failed: orderId={}",
                        request.getRazorpayOrderId());
            }
        } catch (RazorpayException e) {
            payment.setStatus(PaymentStatus.FAILED);
            log.error("Payment verification error: {}", e.getMessage(), e);
        }

        payment.setUpdatedAt(LocalDateTime.now());
        Payment updated = paymentRepository.save(payment);
        return mapToResponse(updated);
    }

    @Override
    @Transactional
    public PaymentResponse createPayment(CreatePaymentRequest request) {
        Payment payment = Payment.builder()
                .userId(request.getUserId())
                .amount(request.getAmount())
                .paymentType(request.getPaymentType())
                .status(PaymentStatus.PENDING)
                .transactionId(UUID.randomUUID().toString())
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        Payment saved = paymentRepository.save(payment);
        return mapToResponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public PaymentResponse getPaymentById(Long id) {
        Payment payment = paymentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Payment not found with id: " + id));

        return mapToResponse(payment);
    }

    @Override
    @Transactional(readOnly = true)
    public List<PaymentResponse> getPaymentsByUserId(Long userId) {
        return paymentRepository.findByUserIdOrderByCreatedAtDesc(userId)
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<PaymentResponse> getAllPayments() {
        return paymentRepository.findAll()
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    @Override
    @Transactional
    public PaymentResponse updatePaymentStatus(Long id, UpdatePaymentStatusRequest request) {
        Payment payment = paymentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Payment not found with id: " + id));

        payment.setStatus(request.getStatus());
        payment.setUpdatedAt(LocalDateTime.now());

        Payment updated = paymentRepository.save(payment);
        return mapToResponse(updated);
    }

    private PaymentResponse mapToResponse(Payment payment) {
        return PaymentResponse.builder()
                .id(payment.getId())
                .userId(payment.getUserId())
                .amount(payment.getAmount())
                .paymentType(payment.getPaymentType())
                .status(payment.getStatus())
                .transactionId(payment.getTransactionId())
                .razorpayOrderId(payment.getRazorpayOrderId())
                .razorpayPaymentId(payment.getRazorpayPaymentId())
                .createdAt(payment.getCreatedAt())
                .updatedAt(payment.getUpdatedAt())
                .build();
    }
}