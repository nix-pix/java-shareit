package ru.practicum.shareit.booking;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import ru.practicum.shareit.booking.dto.BookingAllDto;
import ru.practicum.shareit.booking.dto.BookingControllerDto;
import ru.practicum.shareit.item.ItemService;
import ru.practicum.shareit.item.dto.ItemAllDto;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.user.UserService;
import ru.practicum.shareit.user.dto.UserDto;

import javax.persistence.EntityManager;
import javax.transaction.Transactional;
import java.util.List;

import static java.time.LocalDateTime.now;
import static java.util.List.of;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.notNullValue;
import static ru.practicum.shareit.enums.Status.APPROVED;
import static ru.practicum.shareit.enums.Status.WAITING;

@Transactional
@SpringBootTest
@RequiredArgsConstructor(onConstructor_ = @Autowired)
class BookingServiceImplTest {
    private BookingAllDto bookingAllFieldsDto;
    private final BookingService bookingService;
    private final EntityManager entityManager;
    private final UserService userService;
    private final ItemService itemService;
    private ItemDto itemDto;
    private UserDto owner;

    @BeforeEach
    void initialize() {
        owner = userService.save(
                new UserDto(
                        null,
                        "Lora",
                        "lora@mail.com")
        );
        UserDto booker = userService.save(
                new UserDto(
                        null,
                        "Mike",
                        "mike@mail.com")
        );
        itemDto = itemService.save(
                new ItemDto(
                        null,
                        "pen",
                        "blue",
                        true,
                        null),
                null,
                owner.getId()
        );
        ItemAllDto itemAllFieldsDto = new ItemAllDto(
                itemDto.getId(),
                itemDto.getName(),
                itemDto.getDescription(),
                true,
                owner.getId(),
                null,
                null,
                null,
                of()
        );
        BookingControllerDto bookingSavingDto = BookingControllerDto.builder()
                .id(1L)
                .start(now())
                .end(now().plusHours(2))
                .itemId(1L)
                .booker(1L)
                .status(WAITING.name())
                .build();
        bookingAllFieldsDto = bookingService.save(
                bookingSavingDto,
                itemAllFieldsDto,
                booker.getId()
        );
    }

    @Test
    void saveTest() {
        Booking booking = entityManager
                .createQuery(
                        "SELECT booking " +
                                "FROM Booking booking",
                        Booking.class)
                .getSingleResult();
        assertThat(booking.getId(), notNullValue());
        assertThat(booking.getBooker().getId(),
                equalTo(bookingAllFieldsDto.getBooker().getId()));
        assertThat(booking.getItem().getId(),
                equalTo(bookingAllFieldsDto.getItem().getId()));
    }

    @Test
    void getAllBookingsTest() {
        List<BookingAllDto> approved = bookingService.getAll(
                bookingAllFieldsDto.getBooker().getId(),
                null,
                null,
                null);
        List<Booking> booking = entityManager.createQuery(
                        "SELECT booking " +
                                "FROM Booking booking " +
                                "WHERE booking.booker.id = :id",
                        Booking.class)
                .setParameter("id", bookingAllFieldsDto.getBooker().getId())
                .getResultList();
        assertThat(approved.get(0).getId(),
                equalTo(booking.get(0).getId()));
        assertThat(approved.size(),
                equalTo(booking.size()));
    }

    @Test
    void getBookingsByOwnerIdStatusTest() {
        List<BookingAllDto> bookings = bookingService.getBookingsByOwner(
                owner.getId(),
                APPROVED.name(),
                null,
                null);
        List<Booking> approvedBookings = entityManager.createQuery(
                        "SELECT booking " +
                                "FROM Booking booking " +
                                "JOIN booking.item item " +
                                "WHERE item.owner.id = :id AND booking.status = :status",
                        Booking.class)
                .setParameter("id", owner.getId())
                .setParameter("status", APPROVED)
                .getResultList();
        assertThat(bookings.size(),
                equalTo(approvedBookings.size()));
        assertThat(bookings.size(),
                equalTo(0));
    }

    @Test
    void getBookingsByItemTest() {
        List<BookingAllDto> bookingsFrom = bookingService.getBookingsByItem(
                itemDto.getId(),
                owner.getId()
        );
        List<Booking> bookings = entityManager.createQuery(
                        "SELECT booking " +
                                "FROM Booking booking " +
                                "JOIN booking.item item " +
                                "WHERE item.owner.id = :ownerId AND item.id = :itemId",
                        Booking.class)
                .setParameter("itemId", itemDto.getId())
                .setParameter("ownerId", owner.getId())
                .getResultList();
        assertThat(bookingsFrom.get(0).getId(),
                equalTo(bookings.get(0).getId()));
        assertThat(bookingsFrom.size(),
                equalTo(bookings.size()));
    }

    @Test
    void getAllBookingsEmptyListTest() {
        List<BookingAllDto> allBookings = bookingService.getAll(
                bookingAllFieldsDto.getBooker().getId(),
                APPROVED.name(),
                null,
                null);
        List<Booking> approved = entityManager.createQuery(
                        "SELECT booking " +
                                "FROM Booking booking " +
                                "WHERE booking.booker.id = :id AND booking.status = :status",
                        Booking.class)
                .setParameter("id", bookingAllFieldsDto.getBooker().getId())
                .setParameter("status", APPROVED)
                .getResultList();
        assertThat(allBookings.size(),
                equalTo(approved.size()));
        assertThat(allBookings.size(),
                equalTo(0));
    }

    @Test
    void getBookingsByOwnerIdTest() {
        List<BookingAllDto> bookings = bookingService.getBookingsByOwner(
                owner.getId(),
                null,
                null,
                null);
        List<Booking> booking = entityManager.createQuery(
                        "SELECT booking " +
                                "FROM Booking booking " +
                                "JOIN booking.item item " +
                                "WHERE item.owner.id = :id",
                        Booking.class)
                .setParameter("id", owner.getId())
                .getResultList();
        assertThat(bookings.get(0).getId(),
                equalTo(booking.get(0).getId()));
        assertThat(bookings.size(),
                equalTo(booking.size()));
    }

    @Test
    void getBookingByIdTest() {
        BookingAllDto approved = bookingService.get(
                bookingAllFieldsDto.getId(),
                bookingAllFieldsDto.getBooker().getId()
        );
        Booking booking = entityManager
                .createQuery(
                        "SELECT booking " +
                                "FROM Booking booking " +
                                "WHERE booking.id = :id AND booking.booker.id = :bookerId",
                        Booking.class)
                .setParameter("bookerId", bookingAllFieldsDto.getBooker().getId())
                .setParameter("id", bookingAllFieldsDto.getId())
                .getSingleResult();
        assertThat(approved.getItem().getId(),
                equalTo(booking.getItem().getId()));
        assertThat(approved.getStart(),
                equalTo(booking.getStart()));
        assertThat(approved.getId(),
                equalTo(booking.getId()));
    }
}
