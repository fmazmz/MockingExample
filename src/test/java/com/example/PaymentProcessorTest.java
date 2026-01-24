package com.example;

import com.example.payment.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class PaymentProcessorTest {

    @Mock private PaymentConfig paymentConfig;
    @Mock private PaymentRepository paymentRepository;
    @Mock private PaymentApi paymentApi;
    @Mock private EmailService emailService;

    @InjectMocks
    private PaymentProcessor paymentProcessor;

    private static final String API_KEY = "test-api-key-123";
    private static final String EMAIL = "customer@email.com";
    private static final BigDecimal AMOUNT = BigDecimal.valueOf(200.0);

    @BeforeEach
    void setUp() {
        when(paymentConfig.getApiKey()).thenReturn(API_KEY);
    }


    private static Stream<Arguments> nullParameterProvider() {
        return Stream.of(
                Arguments.of(EMAIL, null), // Email is null
                Arguments.of(null, AMOUNT), // Amount is null
                Arguments.of(null, null)
        );
    }

    @DisplayName("throws exception if any paramater is null")
    @ParameterizedTest
    @MethodSource("nullParameterProvider")
    void nullParameters(String email, BigDecimal amount) {
        assertThatThrownBy(() -> paymentProcessor.processPayment(email, amount))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Email and amount cannot be null");
    }

    @DisplayName("successfully processes payment and saves the payment to the database")
    @Test
    void successfulPayment() throws PaymentException, NotificationException {
        PaymentApiResponse response = new PaymentApiResponse(true);

        when(paymentApi.charge(API_KEY, AMOUNT)).thenReturn(response);

        boolean result = paymentProcessor.processPayment(EMAIL, AMOUNT);

        assertThat(result).isTrue();
        verify(paymentRepository).save(AMOUNT, PaymentStatus.SUCCESS.name());
        verify(emailService).sendPaymentConfirmation(EMAIL, AMOUNT);
    }

    @DisplayName("throws exception when charge fails and saves failed payment to database")
    @Test
    void unsuccessfulPayment() throws NotificationException {
        PaymentApiResponse response = new PaymentApiResponse(false);

        when(paymentApi.charge(API_KEY, AMOUNT)).thenReturn(response);

        assertThatThrownBy(() -> paymentProcessor.processPayment(EMAIL, AMOUNT))
                .isInstanceOf(PaymentException.class)
                .hasMessageContaining("Payment failed with amount: " + AMOUNT);

        // Failed payment should still be saved to DB
        verify(paymentRepository).save(AMOUNT, PaymentStatus.FAILED.name());
        // No confirmation email should be sent on failed payment
        verify(emailService, never()).sendPaymentConfirmation(EMAIL, AMOUNT);
    }

    @DisplayName("failed email confirmation should still return successful payment and save to database")
    @Test
    void failedEmailConfirmation() throws PaymentException, NotificationException {
        PaymentApiResponse response = new PaymentApiResponse(true);

        when(paymentApi.charge(API_KEY, AMOUNT)).thenReturn(response);

        // Mock a failure to send email confirmation
        doThrow(new NotificationException("Email service failure"))
                .when(emailService).sendPaymentConfirmation(EMAIL, AMOUNT);

        boolean result = paymentProcessor.processPayment(EMAIL, AMOUNT);

        assertThat(result).isTrue();
        verify(paymentRepository).save(AMOUNT, PaymentStatus.SUCCESS.name());
        // Still verify that paymentProcessor tried to send the email
        verify(emailService).sendPaymentConfirmation(EMAIL, AMOUNT);
    }
}
