package com.example;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
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

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("BookingSystem Tests")
class BookingSystemTest {

    @Mock
    private TimeProvider timeProvider;

    @Mock
    private RoomRepository roomRepository;

    @Mock
    private NotificationService notificationService;

    @InjectMocks
    private BookingSystem bookingSystem;

    private Room room;
    private Booking booking;
    private final LocalDateTime now = LocalDateTime.of(2026, 2, 9, 10, 0);
    private final LocalDateTime startTime = now.plusHours(1);
    private final LocalDateTime endTime = now.plusHours(2);
    private final String roomId = "room1";
    private final String bookingId = "booking1";

    @BeforeEach
    void setUp() {
        room = new Room(roomId, "Test Room");
        booking = new Booking(bookingId, roomId, startTime, endTime);
        lenient().when(timeProvider.getCurrentTime()).thenReturn(now);
    }

    @Nested
    @DisplayName("bookRoom() Tests")
    class BookRoomTests {

        @Test
        @DisplayName("Should succeed for an available room")
        void testBookRoom_Success() throws NotificationException {
            when(roomRepository.findById(roomId)).thenReturn(Optional.of(room));

            boolean result = bookingSystem.bookRoom(roomId, startTime, endTime);

            assertThat(result).isTrue();
            verify(roomRepository).save(room);
            assertThat(room.isAvailable(startTime, endTime)).isFalse();
            verify(notificationService).sendBookingConfirmation(any(Booking.class));
        }

        @Test
        @DisplayName("Should throw exception when room does not exist")
        void testBookRoom_RoomNotFound() {
            when(roomRepository.findById(roomId)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> bookingSystem.bookRoom(roomId, startTime, endTime))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("Rummet existerar inte");
        }

        @Test
        @DisplayName("Should fail when room is not available")
        void testBookRoom_RoomNotAvailable() {
            room.addBooking(new Booking("otherBooking", roomId, startTime, endTime));
            when(roomRepository.findById(roomId)).thenReturn(Optional.of(room));

            boolean result = bookingSystem.bookRoom(roomId, startTime, endTime);

            assertThat(result).isFalse();
            verify(roomRepository, never()).save(any());
        }

        @Test
        @DisplayName("Should throw exception when booking in the past")
        void testBookRoom_InvalidTime_BookingInPast() {
            LocalDateTime pastTime = now.minusHours(1);
            assertThatThrownBy(() -> bookingSystem.bookRoom(roomId, pastTime, startTime))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("Kan inte boka tid i dåtid");
        }

        @Test
        @DisplayName("Should throw exception when end time is before start time")
        void testBookRoom_InvalidTime_EndTimeBeforeStartTime() {
            assertThatThrownBy(() -> bookingSystem.bookRoom(roomId, endTime, startTime))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("Sluttid måste vara efter starttid");
        }

        @ParameterizedTest
        @MethodSource("com.example.BookingSystemTest#invalidBookingParameters")
        @DisplayName("Should throw exception for null parameters")
        void testBookRoom_InvalidInput_NullParameters(String testRoomId, LocalDateTime testStartTime, LocalDateTime testEndTime) {
            assertThatThrownBy(() -> bookingSystem.bookRoom(testRoomId, testStartTime, testEndTime))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("Bokning kräver giltiga start- och sluttider samt rum-id");
        }

        @Test
        @DisplayName("Should succeed even if notification service fails")
        void testBookRoom_NotificationServiceFails() throws NotificationException {
            when(roomRepository.findById(roomId)).thenReturn(Optional.of(room));
            doThrow(new NotificationException("Failed to send")).when(notificationService).sendBookingConfirmation(any(Booking.class));

            boolean result = bookingSystem.bookRoom(roomId, startTime, endTime);

            assertThat(result).isTrue();
            verify(roomRepository).save(room);
        }
    }

    @Nested
    @DisplayName("cancelBooking() Tests")
    class CancelBookingTests {

        @BeforeEach
        void setup() {
            room.addBooking(booking);
            lenient().when(roomRepository.findAll()).thenReturn(Collections.singletonList(room));
        }

        @Test
        @DisplayName("Should succeed for an existing future booking")
        void testCancelBooking_Success() throws NotificationException {
            boolean result = bookingSystem.cancelBooking(bookingId);

            assertThat(result).isTrue();
            assertThat(room.hasBooking(bookingId)).isFalse();
            verify(roomRepository).save(room);
            verify(notificationService).sendCancellationConfirmation(booking);
        }

        @Test
        @DisplayName("Should fail when booking does not exist")
        void testCancelBooking_BookingNotFound() {
            when(roomRepository.findAll()).thenReturn(Collections.singletonList(new Room("otherRoom", "Other Room")));
            
            boolean result = bookingSystem.cancelBooking(bookingId);

            assertThat(result).isFalse();
            verify(roomRepository, never()).save(any());
        }

        @Test
        @DisplayName("Should throw exception when cancelling a past booking")
        void testCancelBooking_InvalidTime_BookingAlreadyStarted() {
            // Given a booking that has already started
            LocalDateTime pastStartTime = now.minusHours(2);
            LocalDateTime pastEndTime = now.minusHours(1);
            Booking pastBooking = new Booking("pastBooking", roomId, pastStartTime, pastEndTime);
            room.addBooking(pastBooking);
            
            when(roomRepository.findAll()).thenReturn(Collections.singletonList(room));

            assertThatThrownBy(() -> bookingSystem.cancelBooking("pastBooking"))
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessage("Kan inte avboka påbörjad eller avslutad bokning");
        }

        @Test
        @DisplayName("Should throw exception for null booking ID")
        void testCancelBooking_InvalidInput_NullBookingId() {
            assertThatThrownBy(() -> bookingSystem.cancelBooking(null))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("Boknings-id kan inte vara null");
        }

        @Test
        @DisplayName("Should succeed even if notification service fails")
        void testCancelBooking_NotificationServiceFails() throws NotificationException {
            doThrow(new NotificationException("Failed to send")).when(notificationService).sendCancellationConfirmation(booking);

            boolean result = bookingSystem.cancelBooking(bookingId);

            assertThat(result).isTrue();
            verify(roomRepository).save(room);
        }
    }

    @Nested
    @DisplayName("getAvailableRooms() Tests")
    class GetAvailableRoomsTests {

        @Test
        @DisplayName("Should return available rooms")
        void testGetAvailableRooms_Success() {
            Room unavailableRoom = new Room("room2", "Unavailable Room");
            unavailableRoom.addBooking(new Booking("booking2", "room2", startTime, endTime));
            when(roomRepository.findAll()).thenReturn(List.of(room, unavailableRoom));

            List<Room> availableRooms = bookingSystem.getAvailableRooms(startTime, endTime);

            assertThat(availableRooms).containsExactly(room);
        }

        @Test
        @DisplayName("Should return empty list when no rooms are available")
        void testGetAvailableRooms_NoRoomsAvailable() {
            room.addBooking(new Booking("booking2", roomId, startTime, endTime));
            when(roomRepository.findAll()).thenReturn(Collections.singletonList(room));

            List<Room> availableRooms = bookingSystem.getAvailableRooms(startTime, endTime);

            assertThat(availableRooms).isEmpty();
        }

        @Test
        @DisplayName("Should return all rooms when none have bookings")
        void testGetAvailableRooms_AllRoomsAvailable() {
             Room anotherRoom = new Room("room2", "Another Room");
            when(roomRepository.findAll()).thenReturn(List.of(room, anotherRoom));

            List<Room> availableRooms = bookingSystem.getAvailableRooms(startTime, endTime);

            assertThat(availableRooms).containsExactlyInAnyOrder(room, anotherRoom);
        }

        @Test
        @DisplayName("Should throw exception when end time is before start time")
        void testGetAvailableRooms_InvalidTime_EndTimeBeforeStartTime() {
            assertThatThrownBy(() -> bookingSystem.getAvailableRooms(endTime, startTime))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("Sluttid måste vara efter starttid");
        }
        
        @ParameterizedTest
        @MethodSource("com.example.BookingSystemTest#invalidGetAvailableRoomsParameters")
        @DisplayName("Should throw exception for null time parameters")
        void testGetAvailableRooms_InvalidInput_NullParameters(LocalDateTime testStartTime, LocalDateTime testEndTime) {
             assertThatThrownBy(() -> bookingSystem.getAvailableRooms(testStartTime, testEndTime))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("Måste ange både start- och sluttid");
        }
    }

    // Helper methods for parameterized tests

    static Stream<Arguments> invalidBookingParameters() {
        LocalDateTime validTime = LocalDateTime.now();
        return Stream.of(
                Arguments.of(null, validTime, validTime.plusHours(1)),
                Arguments.of("room1", null, validTime.plusHours(1)),
                Arguments.of("room1", validTime, null)
        );
    }
    
    static Stream<Arguments> invalidGetAvailableRoomsParameters() {
        LocalDateTime validTime = LocalDateTime.now();
        return Stream.of(
                Arguments.of(null, validTime),
                Arguments.of(validTime, null)
        );
    }
}