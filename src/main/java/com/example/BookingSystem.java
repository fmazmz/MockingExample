package com.example;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

public class BookingSystem {
    private final TimeProvider timeProvider;
    private final RoomRepository roomRepository;
    private final NotificationService notificationService;

    public BookingSystem(TimeProvider timeProvider,
                         RoomRepository roomRepository,
                         NotificationService notificationService) {
        this.timeProvider = timeProvider;
        this.roomRepository = roomRepository;
        this.notificationService = notificationService;
    }

    public boolean bookRoom(String roomId, LocalDateTime startTime, LocalDateTime endTime) {
        if (startTime == null || endTime == null || roomId == null) {
            throw new IllegalArgumentException("Bokning kräver giltiga start- och sluttider samt rum-id");
        }

        if (startTime.isBefore(timeProvider.getCurrentTime())) {
            throw new IllegalArgumentException("Kan inte boka tid i dåtid");
        }

        if (endTime.isBefore(startTime)) {
            throw new IllegalArgumentException("Sluttid måste vara efter starttid");
        }

        Room room = roomRepository.findById(roomId)
                .orElseThrow(() -> new IllegalArgumentException("Rummet existerar inte"));

        if (!room.isAvailable(startTime, endTime)) {
            return false;
        }

        Booking booking = new Booking(UUID.randomUUID().toString(), roomId, startTime, endTime);
        room.addBooking(booking);
        roomRepository.save(room);

        try {
            notificationService.sendBookingConfirmation(booking);
        } catch (NotificationException e) {
            // Fortsätt även om notifieringen misslyckas
        }

        return true;
    }

        public List<Room> getAvailableRooms(LocalDateTime startTime, LocalDateTime endTime) {
            if (startTime == null || endTime == null) {
                throw new IllegalArgumentException("Måste ange både start- och sluttid");
            }

            if (endTime.isBefore(startTime)) {
                throw new IllegalArgumentException("Sluttid måste vara efter starttid");
            }

            return roomRepository.findAll().stream()
                    .filter(room -> room.isAvailable(startTime, endTime))
                    .collect(Collectors.toList());
        }

    public boolean cancelBooking(String bookingId) {
        if (bookingId == null) {
            throw new IllegalArgumentException("Boknings-id kan inte vara null");
        }

        Optional<Room> roomWithBooking = roomRepository.findAll().stream()
                .filter(room -> room.hasBooking(bookingId))
                .findFirst();

        if (roomWithBooking.isEmpty()) {
            return false;
        }

        Room room = roomWithBooking.get();
        Booking booking = room.getBooking(bookingId);

        if (booking.getStartTime().isBefore(timeProvider.getCurrentTime())) {
            throw new IllegalStateException("Kan inte avboka påbörjad eller avslutad bokning");
        }

        room.removeBooking(bookingId);
        roomRepository.save(room);

        try {
            notificationService.sendCancellationConfirmation(booking);
        } catch (NotificationException e) {
            // Fortsätt även om notifieringen misslyckas
        }

        return true;
    }
}

// Stödklasser och interface som behövs:
