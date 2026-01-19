package com.example;

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

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.stream.Stream;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("BookingSystem Tests")
class BookingSystemTest {
    @Mock private TimeProvider timeProvider;
    @Mock private NotificationService notificationService;
    @Mock private RoomRepository roomRepository;
    @Mock private Room mockRoom;

    @InjectMocks
    private BookingSystem bookingSystem;


    private LocalDateTime now;
    private LocalDateTime start;
    private LocalDateTime end;

    @BeforeEach
    void mockData() {
        now = LocalDateTime.of(2026, 1, 19, 10, 0);
        start = LocalDateTime.of(2026, 1, 19, 13, 0);
        end = LocalDateTime.of(2026, 1, 19, 14, 0);
    }

    // Helper method to avoid duplicate setup of a mockRoom on each testcase
    private void setUpMockRoom(boolean isAvailable) {
        when(timeProvider.getCurrentTime()).thenReturn(now);
        when(roomRepository.findById("room-id")).thenReturn(Optional.of(mockRoom));
        when(mockRoom.isAvailable(start, end)).thenReturn(isAvailable);
    }

    private static Stream<Arguments> nullParameterProvider() {
        LocalDateTime start = LocalDateTime.of(2026, 1, 19, 13, 0);
        LocalDateTime end = LocalDateTime.of(2026, 1, 19, 14, 0);

        return Stream.of(
                Arguments.of(null, start, end), // roomId null
                Arguments.of("room-id", null, end), // startTime null
                Arguments.of("room-id", start, null) // endTime null
        );
    }


    @Test
    @DisplayName("should return true if booking succeeded")
    void test_bookRoom_success() {
        setUpMockRoom(true);

        boolean result = bookingSystem.bookRoom("room-id", start, end);

        assertThat(result).isTrue();
        verify(mockRoom).addBooking(any(Booking.class));
        verify(roomRepository).save(mockRoom);
    }


    @Test
    @DisplayName("should return false when the room is unavailable")
    void test_bookRoom_unavailable_room() {
        setUpMockRoom(false);

        boolean result = bookingSystem.bookRoom("room-id", start, end);

        assertThat(result).isFalse();
        verify(mockRoom, never()).addBooking(any(Booking.class));
        verify(roomRepository, never()).save(mockRoom);
    }


    @DisplayName("should throw IllegalArgumentException when parameters are null")
    @ParameterizedTest
    @MethodSource("nullParameterProvider")
    void test_bookRoom_null_parameters(String roomId, LocalDateTime start, LocalDateTime end) {
        assertThatThrownBy(() -> bookingSystem.bookRoom(roomId, start, end))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Bokning kräver giltiga start- och sluttider samt rum-id");
    }


    // TODO: bookRoom Test scenarios:
    // - DONE - Unavailable room
    // - A room that does not exist
    // - DONE - Null parameters
    // - startTime before currentTime
    // - endTime before startTime
    // - failed sendBookingConfirmation should still book

}
