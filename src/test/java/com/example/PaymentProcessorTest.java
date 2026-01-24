package com.example;

import com.example.payment.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

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
}
