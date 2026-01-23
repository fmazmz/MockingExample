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

    private String ROOM_ID;
    private LocalDateTime NOW;
    private LocalDateTime START;
    private LocalDateTime END;

    @BeforeEach
    void mockData() {
        NOW = LocalDateTime.of(2026, 1, 19, 10, 0);
        START = LocalDateTime.of(2026, 1, 19, 13, 0);
        END = LocalDateTime.of(2026, 1, 19, 14, 0);
        ROOM_ID = "default-room-id";
    }

    // Helper method to avoid duplicate setup of a mockRoom on each testcase
    private void setUpMockRoom(boolean isAvailable, String roomId) {
        when(timeProvider.getCurrentTime()).thenReturn(NOW);
        when(roomRepository.findById(roomId)).thenReturn(Optional.of(mockRoom));
        when(mockRoom.isAvailable(START, END)).thenReturn(isAvailable);
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
        setUpMockRoom(true, ROOM_ID);

        boolean result = bookingSystem.bookRoom(ROOM_ID, START, END);

        assertThat(result).isTrue();
        verify(mockRoom).addBooking(any(Booking.class));
        verify(roomRepository).save(mockRoom);
    }


    @Test
    @DisplayName("should return false when room is unavailable")
    void unavailableRoom() {
        setUpMockRoom(false, ROOM_ID);

        boolean result = bookingSystem.bookRoom(ROOM_ID, START, END);

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
        when(timeProvider.getCurrentTime()).thenReturn(NOW);
        when(roomRepository.findById(ROOM_ID)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> bookingSystem.bookRoom(ROOM_ID, START, END))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Rummet existerar inte");
    }

    @DisplayName("throws exception when startTime is in the past")
    @Test
    void startTimeInPast() {
        LocalDateTime pastStart = NOW.minusHours(3);

        when(timeProvider.getCurrentTime()).thenReturn(NOW);

        assertThatThrownBy(() -> bookingSystem.bookRoom(ROOM_ID, pastStart, END))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Kan inte boka tid i dåtid");
    }


    @DisplayName("throws exception when endTime is before startTime")
    @Test
    void endTimeBeforeStart() {
        LocalDateTime invalidEndTime = START.minusHours(1);

        when(timeProvider.getCurrentTime()).thenReturn(NOW);

        assertThatThrownBy(() -> bookingSystem.bookRoom(ROOM_ID, START, invalidEndTime))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Sluttid måste vara efter starttid");

    }


    // TODO: bookRoom Test scenarios:
    // - DONE - Unavailable room
    // - DONE - A room that does not exist
    // - DONE - Null parameters
    // - DONE - startTime before currentTime
    // - DONE - endTime before startTime
    // - failed sendBookingConfirmation should still book

}
