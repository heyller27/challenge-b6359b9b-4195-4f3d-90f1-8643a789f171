package com.pragma.payment.application.service;

import com.pragma.payment.domain.model.Payment;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;

public class PaymentServiceTest {
    private PaymentService paymentService;

    @BeforeEach
    void setUp() {
        paymentService = new PaymentServiceImpl();
    }

    @Test
    public void testGetAllPayments() {
        List<Payment> payments = paymentService.getAllPayments();
        assertNotNull(payments);
    }

    @Test
    public void testGetPaymentById() {
        Payment payment = new Payment("1", "100", "USD", "pending");
        paymentService.createPayment(payment);
        Payment retrievedPayment = paymentService.getPaymentById("1");
        assertNotNull(retrievedPayment);
        assertEquals(payment.getId(), retrievedPayment.getId());
    }

    @Test
    public void testCreatePayment() {
        Payment payment = new Payment("1", "100", "USD", "pending");
        Payment createdPayment = paymentService.createPayment(payment);
        assertNotNull(createdPayment);
        assertEquals(payment.getId(), createdPayment.getId());
    }

    @Test
    public void testUpdatePayment() {
        Payment payment = new Payment("1", "100", "USD", "pending");
        paymentService.createPayment(payment);
        Payment updatedPayment = new Payment("1", "200", "USD", "completed");
        Payment result = paymentService.updatePayment("1", updatedPayment);
        assertNotNull(result);
        assertEquals(updatedPayment.getAmount(), result.getAmount());
    }

    @Test
    public void testDeletePayment() {
        Payment payment = new Payment("1", "100", "USD", "pending");
        paymentService.createPayment(payment);
        paymentService.deletePayment("1");
        Payment deletedPayment = paymentService.getPaymentById("1");
        assertNull(deletedPayment);
    }
}