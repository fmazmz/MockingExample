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
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
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

    private static final LocalDateTime NOW = LocalDateTime.of(2026, 1, 19, 10, 0);
    private static final LocalDateTime START = LocalDateTime.of(2026, 1, 19, 13, 0);
    private static final LocalDateTime END = LocalDateTime.of(2026, 1, 19, 14, 0);
    private static final String ROOM_ID = "default-room-id";
    private static final String BOOKING_ID = "default-booking-id";


    // Helper method to avoid duplicate setup of a mockRoom on each testcase
    // Returns a mockedRoom that is either available or notAvailable depending on input
    private void setUpMockRoomAvailability(boolean isAvailable) {
        when(timeProvider.getCurrentTime()).thenReturn(NOW);
        when(roomRepository.findById(ROOM_ID)).thenReturn(Optional.of(mockRoom));
        when(mockRoom.isAvailable(START, END)).thenReturn(isAvailable);
    }

    private static Stream<Arguments> nullParameterProvider_bookRoom() {
        return Stream.of(
                Arguments.of(null, START, END), // roomId null
                Arguments.of(ROOM_ID, null, END), // startTime null
                Arguments.of(ROOM_ID, START, null), // endTime null
                Arguments.of(null, null, null)
        );
    }

    private static Stream<Arguments> nullParameterProvider_getAvailableRooms() {
        return Stream.of(
                Arguments.of(null, END), // startTime null
                Arguments.of(START, null), // endTime null
                Arguments.of(null, null)
        );
    }


    @Test
    @DisplayName("should return true when room is available")
    void success() {
        setUpMockRoomAvailability(true);

        boolean result = bookingSystem.bookRoom(ROOM_ID, START, END);

        assertThat(result).isTrue();
        verify(mockRoom).addBooking(any(Booking.class));
        verify(roomRepository).save(mockRoom);
    }


    @Test
    @DisplayName("should return false when room is unavailable")
    void unavailableRoom() {
        setUpMockRoomAvailability(false);

        boolean result = bookingSystem.bookRoom(ROOM_ID, START, END);

        assertThat(result).isFalse();
        verify(mockRoom, never()).addBooking(any(Booking.class));
        verify(roomRepository, never()).save(mockRoom);
    }


    @DisplayName("throws exception for null parameters")
    @ParameterizedTest
    @MethodSource("nullParameterProvider_bookRoom")
    void nullParameters_bookRoom(String roomId, LocalDateTime start, LocalDateTime end) {
        assertThatThrownBy(() -> bookingSystem.bookRoom(roomId, start, end))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Bokning kräver giltiga start- och sluttider samt rum-id");
    }

    @DisplayName("throws exception when room does not exist")
    @Test
    void nonExistentRoom() {
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
    void endTimeBeforeStart_bookRoom() {
        LocalDateTime earlyEnd = START.minusHours(1);

        when(timeProvider.getCurrentTime()).thenReturn(NOW);

        assertThatThrownBy(() -> bookingSystem.bookRoom(ROOM_ID, START, earlyEnd))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Sluttid måste vara efter starttid");

    }


    @DisplayName("room gets booked even if there is a notification failure")
    @Test
    void succeedWithNotificationFailure() throws NotificationException {
        setUpMockRoomAvailability(true);

        doThrow(new NotificationException("Email service down"))
                .when(notificationService).sendBookingConfirmation(any(Booking.class));

        boolean result = bookingSystem.bookRoom(ROOM_ID, START, END);

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

        boolean result = bookingSystem.cancelBooking(BOOKING_ID);

        assertThat(result).isFalse();
        verify(roomRepository, never()).save(any());
    }


    @DisplayName("throws exception when booking startTime is already past current time")
    @Test
    void lateCancellation() {
        LocalDateTime pastStart = NOW.minusHours(1);

        Booking mockBooking = mock(Booking.class);
        when(mockBooking.getStartTime()).thenReturn(pastStart);

        Room mockRoom = mock(Room.class);
        when(mockRoom.hasBooking(BOOKING_ID)).thenReturn(true);
        when(mockRoom.getBooking(BOOKING_ID)).thenReturn(mockBooking);

        when(timeProvider.getCurrentTime()).thenReturn(NOW);
        when(roomRepository.findAll()).thenReturn(List.of(mockRoom));

        assertThatThrownBy(() -> bookingSystem.cancelBooking(BOOKING_ID))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Kan inte avboka påbörjad eller avslutad bokning");
    }

    @DisplayName("booking gets cancelled even if there is a notification failure")
    @Test
    void succeedWithConfirmationFailure() throws NotificationException {
        Booking mockBooking = mock(Booking.class);
        when(mockBooking.getStartTime()).thenReturn(START);

        Room mockRoom = mock(Room.class);
        when(mockRoom.hasBooking(BOOKING_ID)).thenReturn(true);
        when(mockRoom.getBooking(BOOKING_ID)).thenReturn(mockBooking);

        when(timeProvider.getCurrentTime()).thenReturn(NOW);
        when(roomRepository.findAll()).thenReturn(List.of(mockRoom));

        doThrow(new NotificationException("NotificationService is down"))
                .when(notificationService).sendCancellationConfirmation(mockBooking);

        boolean result = bookingSystem.cancelBooking(BOOKING_ID);

        assertThat(result).isTrue();
        verify(mockRoom).removeBooking(BOOKING_ID);
        verify(roomRepository).save(mockRoom);
    }


    @DisplayName("returns available rooms for given time period")
    @Test
    void returnsAvailableRooms() {
        Room room1 = mock(Room.class);
        Room room2 = mock(Room.class);
        Room room3 = mock(Room.class);

        when(room1.isAvailable(START, END)).thenReturn(true);
        when(room2.isAvailable(START, END)).thenReturn(true);
        when(room3.isAvailable(START, END)).thenReturn(false);  // Mock one non available room

        when(roomRepository.findAll()).thenReturn(Arrays.asList(room1, room2, room3));

        List<Room> result = bookingSystem.getAvailableRooms(START, END);

        assertNotNull(result);
        assertThat(result.size()).isEqualTo(2);

        verify(roomRepository).findAll();
    }


    @DisplayName("throws exception if any parameter is null")
    @ParameterizedTest
    @MethodSource("nullParameterProvider_getAvailableRooms")
    void nullParameters_getAvailableRooms(LocalDateTime startTime, LocalDateTime endTime) {
        assertThatThrownBy(() -> bookingSystem.getAvailableRooms(startTime, endTime))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Måste ange både start- och sluttid");
    }

    @DisplayName("throws exception if endTime is before startTime")
    @Test
    void endTimeBeforeStart_getAvailableRooms() {
        LocalDateTime earlyEnd = START.minusHours(1);

        assertThatThrownBy(() -> bookingSystem.getAvailableRooms(START, earlyEnd))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Sluttid måste vara efter starttid");
    }
}
