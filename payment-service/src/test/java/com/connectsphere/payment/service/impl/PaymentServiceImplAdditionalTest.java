package com.connectsphere.payment.service.impl;

import com.connectsphere.payment.dto.CreateOrderRequest;
import com.connectsphere.payment.dto.CreateOrderResponse;
import com.connectsphere.payment.dto.UpdatePaymentStatusRequest;
import com.connectsphere.payment.dto.VerifyPaymentRequest;
import com.connectsphere.payment.entity.Payment;
import com.connectsphere.payment.enums.PaymentStatus;
import com.connectsphere.payment.enums.PaymentType;
import com.connectsphere.payment.exception.ResourceNotFoundException;
import com.connectsphere.payment.repository.PaymentRepository;
import com.razorpay.Order;
import com.razorpay.OrderClient;
import com.razorpay.RazorpayClient;
import com.razorpay.RazorpayException;
import org.json.JSONObject;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PaymentServiceImplAdditionalTest {

    @Mock
    private PaymentRepository paymentRepository;

    @Mock
    private RazorpayClient razorpayClient;

    @Mock
    private OrderClient orderClient;

    @InjectMocks
    private PaymentServiceImpl paymentService;

    @BeforeEach
    void setUp() {
        razorpayClient.orders = orderClient;
        ReflectionTestUtils.setField(paymentService, "razorpayKeyId", "rzp_test_key");
        ReflectionTestUtils.setField(paymentService, "razorpayKeySecret", "secret");
        ReflectionTestUtils.setField(paymentService, "currency", "INR");
    }

    @Test
    void createOrderShouldCreateRazorpayOrderAndPersistPayment() throws Exception {
        CreateOrderRequest request = new CreateOrderRequest();
        request.setUserId(10L);
        request.setAmount(250.0);
        request.setPaymentType(PaymentType.PREMIUM_SUBSCRIPTION);

        Order order = new Order(new JSONObject().put("id", "order_123"));
        when(orderClient.create(any(JSONObject.class))).thenReturn(order);
        when(paymentRepository.save(any(Payment.class))).thenAnswer(invocation -> {
            Payment payment = invocation.getArgument(0);
            payment.setId(99L);
            return payment;
        });

        CreateOrderResponse response = paymentService.createOrder(request);

        assertEquals(99L, response.getPaymentId());
        assertEquals("order_123", response.getRazorpayOrderId());
        assertEquals(25000, response.getAmountInPaise());
        assertEquals("ConnectSphere Premium Subscription", response.getDescription());
    }

    @Test
    void createOrderShouldWrapRazorpayFailure() throws RazorpayException {
        CreateOrderRequest request = new CreateOrderRequest();
        request.setUserId(10L);
        request.setAmount(250.0);
        request.setPaymentType(PaymentType.DONATION);

        when(orderClient.create(any(JSONObject.class))).thenThrow(new RazorpayException("down"));

        assertThrows(RuntimeException.class, () -> paymentService.createOrder(request));
    }

    @Test
    void verifyPaymentShouldThrowWhenOrderIsMissing() {
        VerifyPaymentRequest request = new VerifyPaymentRequest();
        request.setRazorpayOrderId("missing");

        when(paymentRepository.findByRazorpayOrderId("missing")).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> paymentService.verifyPayment(request));
    }

    @Test
    void updatePaymentStatusShouldThrowWhenPaymentMissing() {
        UpdatePaymentStatusRequest request = new UpdatePaymentStatusRequest();
        request.setStatus(PaymentStatus.FAILED);

        when(paymentRepository.findById(404L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> paymentService.updatePaymentStatus(404L, request));
    }
}
