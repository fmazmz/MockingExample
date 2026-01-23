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
import java.util.Collections;
import java.util.List;
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

    private String bookingId;
    private String roomId;
    private LocalDateTime now;
    private LocalDateTime start;
    private LocalDateTime end;

    @BeforeEach
    void mockData() {
        now = LocalDateTime.of(2026, 1, 19, 10, 0);
        start = LocalDateTime.of(2026, 1, 19, 13, 0);
        end = LocalDateTime.of(2026, 1, 19, 14, 0);
        roomId = "default-room-id";
        bookingId = "default-booking-id";
    }

    // Helper method to avoid duplicate setup of a mockRoom on each testcase
    private void setUpMockRoom(boolean isAvailable, String roomId) {
        when(timeProvider.getCurrentTime()).thenReturn(now);
        when(roomRepository.findById(roomId)).thenReturn(Optional.of(mockRoom));
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
    @DisplayName("should return true when room is available")
    void success() {
        setUpMockRoom(true, roomId);

        boolean result = bookingSystem.bookRoom(roomId, start, end);

        assertThat(result).isTrue();
        verify(mockRoom).addBooking(any(Booking.class));
        verify(roomRepository).save(mockRoom);
    }


    @Test
    @DisplayName("should return false when room is unavailable")
    void unavailableRoom() {
        setUpMockRoom(false, roomId);

        boolean result = bookingSystem.bookRoom(roomId, start, end);

        assertThat(result).isFalse();
        verify(mockRoom, never()).addBooking(any(Booking.class));
        verify(roomRepository, never()).save(mockRoom);
    }


    @DisplayName("throws exception for null parameters")
    @ParameterizedTest
    @MethodSource("nullParameterProvider")
    void nullParameters(String roomId, LocalDateTime start, LocalDateTime end) {
        assertThatThrownBy(() -> bookingSystem.bookRoom(roomId, start, end))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Bokning kräver giltiga start- och sluttider samt rum-id");
    }

    @DisplayName("throws exception when room does not exist")
    @Test
    void nonExistantRoom() {
        when(timeProvider.getCurrentTime()).thenReturn(now);
        when(roomRepository.findById(roomId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> bookingSystem.bookRoom(roomId, start, end))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Rummet existerar inte");
    }

    @DisplayName("throws exception when startTime is in the past")
    @Test
    void startTimeInPast() {
        LocalDateTime paststart = now.minusHours(3);

        when(timeProvider.getCurrentTime()).thenReturn(now);

        assertThatThrownBy(() -> bookingSystem.bookRoom(roomId, paststart, end))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Kan inte boka tid i dåtid");
    }


    @DisplayName("throws exception when endTime is before startTime")
    @Test
    void endTimeBeforestart() {
        LocalDateTime earlyEnd = start.minusHours(1);

        when(timeProvider.getCurrentTime()).thenReturn(now);

        assertThatThrownBy(() -> bookingSystem.bookRoom(roomId, start, earlyEnd))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Sluttid måste vara efter starttid");

    }


    @DisplayName("room gets booked even if there is a notification failure")
    @Test
    void succeedWithNotificationFailure() throws NotificationException {
        setUpMockRoom(true, roomId);

        doThrow(new NotificationException("Email service down"))
                .when(notificationService).sendBookingConfirmation(any(Booking.class));

        boolean result = bookingSystem.bookRoom(roomId, start, end);

        assertThat(result).isTrue();

        verify(mockRoom).addBooking(any(Booking.class));
        verify(roomRepository).save(mockRoom);
    }

    @DisplayName("throws exception when bookingId is null")
    @Test
    void nullBookingId() {
        assertThatThrownBy(() -> bookingSystem.cancelBooking(null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Boknings-id kan inte vara null");

        verify(roomRepository, never()).save(mockRoom);
    }

    @DisplayName("returns false when no room is found with provided bookingId")
    @Test
    void bookingNotFound() {
        when(roomRepository.findAll()).thenReturn(Collections.emptyList());

        boolean result = bookingSystem.cancelBooking(bookingId);

        assertThat(result).isFalse();
        verify(roomRepository, never()).save(any());
    }


    @DisplayName("throws exception when booking startTime is already past current time")
    @Test
    void lateCancellation() {
        LocalDateTime pastStart = now.minusHours(1);

        Booking mockBooking = mock(Booking.class);
        when(mockBooking.getStartTime()).thenReturn(pastStart);

        Room mockRoom = mock(Room.class);
        when(mockRoom.hasBooking(bookingId)).thenReturn(true);
        when(mockRoom.getBooking(bookingId)).thenReturn(mockBooking);

        when(timeProvider.getCurrentTime()).thenReturn(now);
        when(roomRepository.findAll()).thenReturn(List.of(mockRoom));

        assertThatThrownBy(() -> bookingSystem.cancelBooking(bookingId))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Kan inte avboka påbörjad eller avslutad bokning");
    }

    @DisplayName("booking gets cancelled even if there is a notification failure")
    @Test
    void succeedWithConfirmationFailure() throws NotificationException {
        Booking mockBooking = mock(Booking.class);
        when(mockBooking.getStartTime()).thenReturn(start);

        Room mockRoom = mock(Room.class);
        when(mockRoom.hasBooking(bookingId)).thenReturn(true);
        when(mockRoom.getBooking(bookingId)).thenReturn(mockBooking);

        when(timeProvider.getCurrentTime()).thenReturn(now);
        when(roomRepository.findAll()).thenReturn(List.of(mockRoom));

        doThrow(new NotificationException("NotificationService is down"))
                .when(notificationService).sendCancellationConfirmation(mockBooking);

        boolean result = bookingSystem.cancelBooking(bookingId);

        assertThat(result).isTrue();
        verify(mockRoom).removeBooking(bookingId);
        verify(roomRepository).save(mockRoom);
    }

}
