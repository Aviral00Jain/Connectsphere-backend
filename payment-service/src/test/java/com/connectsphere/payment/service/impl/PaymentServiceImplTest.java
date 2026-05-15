package com.connectsphere.payment.service.impl;

import com.connectsphere.payment.dto.CreatePaymentRequest;
import com.connectsphere.payment.dto.PaymentResponse;
import com.connectsphere.payment.dto.UpdatePaymentStatusRequest;
import com.connectsphere.payment.entity.Payment;
import com.connectsphere.payment.enums.PaymentStatus;
import com.connectsphere.payment.enums.PaymentType;
import com.connectsphere.payment.exception.ResourceNotFoundException;
import com.connectsphere.payment.repository.PaymentRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PaymentServiceImplTest {

    @Mock
    private PaymentRepository paymentRepository;

    @InjectMocks
    private PaymentServiceImpl paymentService;

    @Test
    void createPaymentShouldSetPendingStatusAndTransactionId() {
        CreatePaymentRequest request = new CreatePaymentRequest();
        request.setUserId(1L);
        request.setAmount(99.0);
        request.setPaymentType(PaymentType.PREMIUM_SUBSCRIPTION);

        when(paymentRepository.save(any(Payment.class))).thenAnswer(invocation -> invocation.getArgument(0));

        PaymentResponse response = paymentService.createPayment(request);

        assertEquals(PaymentStatus.PENDING, response.getStatus());
        assertNotNull(response.getTransactionId());
    }

    @Test
    void getPaymentByIdShouldThrowWhenPaymentMissing() {
        when(paymentRepository.findById(5L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> paymentService.getPaymentById(5L));
    }

    @Test
    void getPaymentByIdShouldReturnMappedPayment() {
        Payment payment = Payment.builder().id(5L).userId(1L).status(PaymentStatus.SUCCESS).build();
        when(paymentRepository.findById(5L)).thenReturn(Optional.of(payment));

        PaymentResponse response = paymentService.getPaymentById(5L);

        assertEquals(5L, response.getId());
    }

    @Test
    void getPaymentsByUserIdShouldMapAllResults() {
        when(paymentRepository.findByUserIdOrderByCreatedAtDesc(1L)).thenReturn(List.of(
                Payment.builder().id(1L).userId(1L).amount(10.0).paymentType(PaymentType.DONATION).status(PaymentStatus.SUCCESS).build()
        ));

        List<PaymentResponse> responses = paymentService.getPaymentsByUserId(1L);

        assertEquals(1, responses.size());
        assertEquals(PaymentStatus.SUCCESS, responses.get(0).getStatus());
    }

    @Test
    void getAllPaymentsShouldMapRepositoryResults() {
        when(paymentRepository.findAll()).thenReturn(List.of(
                Payment.builder().id(1L).userId(1L).status(PaymentStatus.PENDING).build(),
                Payment.builder().id(2L).userId(2L).status(PaymentStatus.SUCCESS).build()
        ));

        assertEquals(2, paymentService.getAllPayments().size());
    }

    @Test
    void updatePaymentStatusShouldPersistNewStatus() {
        Payment payment = Payment.builder()
                .id(9L)
                .status(PaymentStatus.PENDING)
                .build();
        UpdatePaymentStatusRequest request = new UpdatePaymentStatusRequest();
        request.setStatus(PaymentStatus.SUCCESS);

        when(paymentRepository.findById(9L)).thenReturn(Optional.of(payment));
        when(paymentRepository.save(payment)).thenReturn(payment);

        PaymentResponse response = paymentService.updatePaymentStatus(9L, request);

        assertEquals(PaymentStatus.SUCCESS, response.getStatus());
    }
}
