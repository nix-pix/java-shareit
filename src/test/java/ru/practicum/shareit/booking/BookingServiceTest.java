package ru.practicum.shareit.booking;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import ru.practicum.shareit.booking.dto.BookingAllDto;
import ru.practicum.shareit.booking.dto.BookingControllerDto;
import ru.practicum.shareit.exception.IncorrectParameterException;
import ru.practicum.shareit.exception.ObjectNotFoundException;
import ru.practicum.shareit.item.Item;
import ru.practicum.shareit.item.ItemMapper;
import ru.practicum.shareit.user.User;
import ru.practicum.shareit.user.UserService;
import ru.practicum.shareit.user.dto.UserDto;

import java.util.List;

import static java.time.LocalDateTime.now;
import static java.util.List.of;
import static java.util.Optional.ofNullable;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.when;
import static org.springframework.data.domain.Page.empty;
import static ru.practicum.shareit.enums.State.*;
import static ru.practicum.shareit.enums.Status.*;
import static ru.practicum.shareit.user.UserMapper.toUser;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class BookingServiceTest {
    @Mock
    private BookingRepository bookingRepository;
    private BookingControllerDto bookingControllerDto;
    private BookingService bookingService;
    @Mock
    private UserService userService;
    private UserDto userDto;
    private Booking booking;

    @BeforeEach
    void initialize() {
        bookingService = new BookingServiceImpl(userService, bookingRepository);
        bookingControllerDto = bookingControllerDto.builder()
                .id(1L)
                .start(now())
                .end(now().plusHours(2))
                .itemId(1L)
                .booker(2L)
                .status(WAITING.name())
                .build();
        userDto = UserDto.builder()
                .id(1L)
                .name("Lora")
                .email("lora@mail.com")
                .build();
        booking = Booking.builder()
                .id(1L)
                .start(now())
                .end(now().plusHours(2))
                .item(new Item(1L, "pen", "blue pen", true, toUser(userDto), null))
                .booker(new User(2L, "Maggie", "maggie@mail.com"))
                .status(WAITING)
                .build();
    }

    private BookingAllDto saveBookingDto() {
        when(userService.get(any()))
                .thenReturn(userDto);
        when(bookingRepository.findBookingsByItem_IdIsAndStatusIsAndEndIsAfter(anyLong(), any(), any()))
                .thenReturn(of());
        when(bookingRepository.save(any()))
                .thenReturn(booking);
        return bookingService.save(
                bookingControllerDto,
                ItemMapper.toItemAllFieldsDto(
                        booking.getItem(),
                        null,
                        null,
                        of()
                ),
                2L);
    }

    @Test
    void saveBookingEmptyEndTimeTest() {
        lenient().when(userService.get(anyLong()))
                .thenReturn(userDto);
        bookingControllerDto.setEnd(null);
        Exception exception = assertThrows(IncorrectParameterException.class,
                () -> bookingService.save(
                        bookingControllerDto,
                        ItemMapper.toItemAllFieldsDto(
                                booking.getItem(),
                                null,
                                null,
                                of()),
                        2L)
        );
        assertEquals("Не задана дата окончания бронирования", exception.getMessage());
    }

    @Test
    void saveBookingTest() {
        BookingAllDto bookingAllFieldsDto = saveBookingDto();
        assertEquals(bookingAllFieldsDto.getId(), booking.getId());
        assertEquals(bookingAllFieldsDto.getItem().getId(), booking.getItem().getId());
    }

    @Test
    void saveBookingEmptyStartTimeTest() {
        lenient().when(userService.get(anyLong()))
                .thenReturn(userDto);
        bookingControllerDto.setStart(null);
        Exception exception = assertThrows(IncorrectParameterException.class,
                () -> bookingService.save(
                        bookingControllerDto,
                        ItemMapper.toItemAllFieldsDto(
                                booking.getItem(),
                                null,
                                null,
                                of()),
                        2L)
        );
        assertEquals("Не задана дата начала бронирования", exception.getMessage());
    }

    @Test
    void saveBookingStartInPastTest() {
        lenient().when(userService.get(anyLong()))
                .thenReturn(userDto);
        bookingControllerDto.setStart(now().minusDays(2));
        Exception exception = assertThrows(IncorrectParameterException.class,
                () -> bookingService.save(
                        bookingControllerDto,
                        ItemMapper.toItemAllFieldsDto(
                                booking.getItem(),
                                null,
                                null,
                                of()),
                        2L)
        );
        assertEquals("Некорректная дата начала бронирования", exception.getMessage());
    }

    @Test
    void saveBookingEmptyEndInPastTest() {
        lenient().when(userService.get(anyLong()))
                .thenReturn(userDto);
        bookingControllerDto.setEnd(now().minusDays(2));
        Exception exception = assertThrows(IncorrectParameterException.class,
                () -> bookingService.save(
                        bookingControllerDto,
                        ItemMapper.toItemAllFieldsDto(
                                booking.getItem(),
                                null,
                                null,
                                of()),
                        2L)
        );
        assertEquals("Некорректная дата бронирования", exception.getMessage());
    }

    @Test
    void saveBookingByItemOwnerTest() {
        bookingControllerDto.setBooker(booking.getItem().getOwner().getId());
        Exception exception = assertThrows(ObjectNotFoundException.class,
                () -> bookingService.save(
                        bookingControllerDto,
                        ItemMapper.toItemAllFieldsDto(
                                booking.getItem(),
                                null,
                                null,
                                of()),
                        bookingControllerDto.getBooker())
        );
        assertEquals("Вещь с id = " + booking.getId() + " не может быть арендована", exception.getMessage());
    }

    @Test
    void saveBookingTakenItemTest() {
        when(userService.get(any()))
                .thenReturn(userDto);
        when(bookingRepository.findBookingsByItem_IdIsAndStatusIsAndEndIsAfter(anyLong(), any(), any()))
                .thenReturn(of(booking));
        Exception exception = assertThrows(ObjectNotFoundException.class,
                () -> bookingService.save(
                        bookingControllerDto,
                        ItemMapper.toItemAllFieldsDto(
                                booking.getItem(),
                                null,
                                null,
                                of()),
                        1L)
        );
        assertEquals("Вещь с id = " + booking.getItem().getId() + " не может быть арендована", exception.getMessage());
    }

    @Test
    void saveBookingNotAvailableItemTest() {
        booking.getItem().setAvailable(false);
        Exception exception = assertThrows(IncorrectParameterException.class,
                () -> bookingService.save(
                        bookingControllerDto,
                        ItemMapper.toItemAllFieldsDto(
                                booking.getItem(),
                                null,
                                null,
                                of()),
                        2L)
        );
        assertEquals("Вещь с id = " + booking.getId() + " уже арендована", exception.getMessage());
    }

    @Test
    void approveBookingNotItemOwnerTest() {
        when(bookingRepository.findById(anyLong()))
                .thenReturn(ofNullable(booking));
        Exception exception = assertThrows(IncorrectParameterException.class,
                () -> bookingService.approve(
                        booking.getId(),
                        true,
                        3L)
        );
        assertEquals("Бронирование не может быть обновлено", exception.getMessage());
    }

    @Test
    void approveBookingTest() {
        var approved = new Booking(
                booking.getId(),
                booking.getStart(),
                booking.getEnd(),
                booking.getItem(),
                booking.getBooker(),
                APPROVED);
        when(bookingRepository.findById(anyLong()))
                .thenReturn(ofNullable(booking));
        when(bookingRepository.save(any()))
                .thenReturn(approved);
        var approvedFrom = bookingService.approve(
                booking.getId(),
                true,
                userDto.getId()
        );
        assertEquals(approvedFrom.getStatus().name(), approved.getStatus().name());
        assertEquals(approvedFrom.getId(), approved.getId());
    }

    @Test
    void approveBookingByBookerTest() {
        when(bookingRepository.findById(anyLong()))
                .thenReturn(ofNullable(booking));
        Exception exception = assertThrows(ObjectNotFoundException.class,
                () -> bookingService.approve(
                        booking.getId(),
                        true,
                        booking.getBooker().getId())
        );
        assertEquals("Пользователь с id = " + booking.getBooker().getId() + " не может одобрить заявку", exception.getMessage());
    }

    @Test
    void getNotFoundBookingTest() {
        when(bookingRepository.findById(anyLong()))
                .thenThrow(ObjectNotFoundException.class);
        assertThrows(ObjectNotFoundException.class,
                () -> bookingService.get(
                        7L,
                        7L)
        );
    }

    @Test
    void approveApprovedBookingTest() {
        booking.setStatus(APPROVED);
        when(bookingRepository.findById(anyLong()))
                .thenReturn(ofNullable(booking));
        Exception exception = assertThrows(IncorrectParameterException.class,
                () -> bookingService.approve(
                        booking.getId(),
                        true,
                        userDto.getId())
        );
        assertEquals("Бронирование не может быть обновлено", exception.getMessage());
    }

    @Test
    void getBookingTest() {
        BookingAllDto bookingAllFieldsDto = saveBookingDto();
        when(bookingRepository.findById(anyLong()))
                .thenReturn(ofNullable(booking));
        BookingAllDto bookingFrom = bookingService.get(
                bookingAllFieldsDto.getId(),
                userDto.getId()
        );
        assertEquals(bookingFrom.getItem().getId(), booking.getItem().getId());
        assertEquals(bookingFrom.getId(), booking.getId());
    }

    @Test
    void getBookingByAnotherUserTest() {
        when(bookingRepository.findById(anyLong()))
                .thenReturn(ofNullable(booking));
        assertThrows(ObjectNotFoundException.class,
                () -> bookingService.get(
                        booking.getId(),
                        7L)
        );
    }

    @Test
    void getAllBookingsIncorrectEndPaginationTest() {
        Exception exception = assertThrows(IncorrectParameterException.class,
                () -> bookingService.getAll(
                        userDto.getId(),
                        "Unknown",
                        0,
                        0)
        );
        assertEquals("size <= 0 || from < 0", exception.getMessage());
    }

    @Test
    void getAllBookingsTest() {
        saveBookingDto();
        when(bookingRepository.findBookingsByBookerIsOrderByStartDesc(any()))
                .thenReturn(of(booking));
        List<BookingAllDto> bookings = bookingService.getAll(
                userDto.getId(),
                null,
                null,
                null
        );
        assertEquals(bookings.get(0).getId(), booking.getId());
        assertEquals(bookings.size(), 1);
    }

    @Test
    void getAllBookingsIncorrectStartPaginationTest() {
        Exception exception = assertThrows(IncorrectParameterException.class,
                () -> bookingService.getAll(
                        userDto.getId(),
                        "Unknown",
                        -1,
                        14)
        );
        assertEquals("size <= 0 || from < 0", exception.getMessage());
    }

    @Test
    void getAllBookingsWithNotValidStateTest() {
        saveBookingDto();
        Exception exception = assertThrows(IncorrectParameterException.class,
                () -> bookingService.getAll(
                        userDto.getId(),
                        "Unknown",
                        null,
                        null)
        );
        assertEquals("Unknown state: Unknown", exception.getMessage());
    }

    @Test
    void getAllBookingsFutureStateTest() {
        saveBookingDto();
        when(bookingRepository.findBookingsByBookerIsAndStartIsAfterOrderByStartDesc(any(), any()))
                .thenReturn(of(booking));
        List<BookingAllDto> bookings = bookingService.getAll(
                userDto.getId(),
                FUTURE.name(),
                null,
                null
        );
        assertEquals(bookings.get(0).getId(), booking.getId());
        assertEquals(bookings.size(), 1);
    }

    @Test
    void getAllBookingsPastStateTest() {
        saveBookingDto();
        when(bookingRepository.findBookingsByBookerIsAndEndBeforeOrderByStartDesc(any(), any()))
                .thenReturn(of(booking));
        List<BookingAllDto> bookings = bookingService.getAll(
                userDto.getId(),
                PAST.name(),
                null,
                null
        );
        assertEquals(bookings.get(0).getId(), booking.getId());
        assertEquals(bookings.size(), 1);
    }

    @Test
    void getAllBookingsCurrentStateTest() {
        saveBookingDto();
        when(bookingRepository.findBookingsByBookerIsAndStartBeforeAndEndAfterOrderByStartDesc(any(), any(), any()))
                .thenReturn(of(booking));
        List<BookingAllDto> bookings = bookingService.getAll(
                userDto.getId(),
                CURRENT.name(),
                null,
                null
        );
        assertEquals(bookings.get(0).getId(), booking.getId());
        assertEquals(bookings.size(), 1);
    }

    @Test
    void getAllBookingsEmptyTest() {
        saveBookingDto();
        when(bookingRepository.findBookingsByBookerIsAndStartIsAfterOrderByStartDesc(any(), any()))
                .thenReturn(of());
        List<BookingAllDto> bookings = bookingService.getAll(
                userDto.getId(),
                FUTURE.name(),
                null,
                null
        );
        assertEquals(bookings.size(), 0);
    }

    @Test
    void getAllBookingsRejectStateTest() {
        saveBookingDto();
        when(bookingRepository.findBookingsByBookerIsAndStatusIsOrderByStartDesc(any(), any()))
                .thenReturn(of(booking));
        List<BookingAllDto> bookings = bookingService.getAll(
                userDto.getId(),
                REJECTED.name(),
                null,
                null
        );
        assertEquals(bookings.get(0).getId(), booking.getId());
        assertEquals(bookings.size(), 1);
    }

    @Test
    void getAllBookingsCancelStateEmptyTest() {
        saveBookingDto();
        when(bookingRepository.findBookingsByBookerIsAndStatusIsOrderByStartDesc(any(), any()))
                .thenReturn(of());
        List<BookingAllDto> bookings = bookingService.getAll(
                userDto.getId(),
                CANCELED.name(),
                null,
                null
        );
        assertEquals(bookings.size(), 0);
    }

    @Test
    void getBookingsByOwnerIdPastStateTest() {
        saveBookingDto();
        when(bookingRepository.findBookingsByItemOwnerAndEndBeforeOrderByStartDesc(any(), any()))
                .thenReturn(of(booking));
        List<BookingAllDto> bookings = bookingService.getBookingsByOwner(
                userDto.getId(),
                PAST.name(),
                null,
                null
        );
        assertEquals(bookings.get(0).getId(), booking.getId());
        assertEquals(bookings.size(), 1);
    }

    @Test
    void getBookingsByOwnerIdTest() {
        saveBookingDto();
        when(bookingRepository.findBookingsByItemOwnerIsOrderByStartDesc(any()))
                .thenReturn(of(booking));
        List<BookingAllDto> bookings = bookingService.getBookingsByOwner(
                userDto.getId(),
                null,
                null,
                null
        );
        assertEquals(bookings.get(0).getId(), booking.getId());
        assertEquals(bookings.size(), 1);
    }

    @Test
    void getBookingsByOwnerIdAllStateTest() {
        saveBookingDto();
        when(bookingRepository.findBookingsByItemOwnerIsOrderByStartDesc(any()))
                .thenReturn(of(booking));
        List<BookingAllDto> bookings = bookingService.getBookingsByOwner(
                userDto.getId(),
                ALL.name(),
                null,
                null
        );
        assertEquals(bookings.get(0).getId(), booking.getId());
        assertEquals(bookings.size(), 1);
    }

    @Test
    void getBookingsByOwnerIdFutureStateTest() {
        saveBookingDto();
        when(bookingRepository.findBookingsByItemOwnerAndStartAfterOrderByStartDesc(any(), any()))
                .thenReturn(of(booking));
        List<BookingAllDto> bookings = bookingService.getBookingsByOwner(
                userDto.getId(),
                FUTURE.name(),
                null,
                null
        );
        assertEquals(bookings.get(0).getId(), booking.getId());
        assertEquals(bookings.size(), 1);
    }

    @Test
    void getBookingsByOwnerIdRejectStateTest() {
        saveBookingDto();
        when(bookingRepository.findBookingsByItemOwnerIsAndStatusIsOrderByStartDesc(any(), any()))
                .thenReturn(of(booking));
        List<BookingAllDto> bookings = bookingService.getBookingsByOwner(
                userDto.getId(),
                REJECTED.name(),
                null,
                null
        );
        assertEquals(bookings.get(0).getId(), booking.getId());
        assertEquals(bookings.size(), 1);
    }

    @Test
    void getBookingsByOwnerIdCurrentStateTest() {
        saveBookingDto();
        when(bookingRepository.findBookingsByItemOwnerIsAndStartBeforeAndEndAfterOrderByStartDesc(any(), any(), any()))
                .thenReturn(of(booking));
        List<BookingAllDto> bookings = bookingService.getBookingsByOwner(
                userDto.getId(),
                CURRENT.name(),
                null,
                null
        );
        assertEquals(bookings.get(0).getId(), booking.getId());
        assertEquals(bookings.size(), 1);
    }

    @Test
    void getBookingsByItemEmptyTest() {
        when(bookingRepository.findBookingsByItem_IdAndItem_Owner_IdIsOrderByStart(anyLong(), anyLong()))
                .thenReturn(of());
        List<BookingAllDto> bookings = bookingService.getBookingsByItem(
                1L,
                2L
        );
        assertEquals(bookings.size(), 0);
    }

    @Test
    void getBookingsByItemTest() {
        when(bookingRepository.findBookingsByItem_IdAndItem_Owner_IdIsOrderByStart(anyLong(), anyLong()))
                .thenReturn(of(booking));
        List<BookingAllDto> bookings = bookingService.getBookingsByItem(
                1L,
                2L
        );
        assertEquals(bookings.get(0).getId(), booking.getId());
        assertEquals(bookings.size(), 1);
    }

    @Test
    void getAllBookingsPaginationFutureTest() {
        saveBookingDto();
        when(bookingRepository.findBookingsByBookerIsAndStartIsAfterOrderByStartDesc(any(), any(), any()))
                .thenReturn(empty());
        List<BookingAllDto> bookings = bookingService.getAll(
                userDto.getId(),
                FUTURE.name(),
                0,
                2
        );
        assertEquals(bookings.size(), 0);
    }

    @Test
    void getAllBookingsPaginationAllTest() {
        saveBookingDto();
        when(bookingRepository.findBookingsByBookerIsOrderByStartDesc(any(), any()))
                .thenReturn(empty());
        List<BookingAllDto> bookings = bookingService.getAll(
                userDto.getId(),
                ALL.name(),
                0,
                2
        );
        assertEquals(bookings.size(), 0);
    }

    @Test
    void getAllBookingsPaginationPastTest() {
        saveBookingDto();
        when(bookingRepository.findBookingsByBookerIsAndEndBeforeOrderByStartDesc(any(), any(), any()))
                .thenReturn(empty());
        List<BookingAllDto> bookings = bookingService.getAll(
                userDto.getId(),
                PAST.name(),
                0,
                2
        );
        assertEquals(bookings.size(), 0);
    }

    @Test
    void getAllBookingsPaginationCurrentTest() {
        saveBookingDto();
        when(bookingRepository.findBookingsByBookerIsAndStartBeforeAndEndAfterOrderByStartDesc(any(), any(), any(), any()))
                .thenReturn(empty());
        List<BookingAllDto> bookings = bookingService.getAll(
                userDto.getId(),
                CURRENT.name(),
                0,
                2
        );
        assertEquals(bookings.size(), 0);
    }

    @Test
    void getAllBookingsPaginationAnyTest() {
        saveBookingDto();
        when(bookingRepository.findBookingsByBookerIsAndStatusIsOrderByStartDesc(any(), any(), any()))
                .thenReturn(empty());
        List<BookingAllDto> bookings = bookingService.getAll(
                userDto.getId(),
                CANCELED.name(),
                0,
                2
        );
        assertEquals(bookings.size(), 0);
    }

    @Test
    void getBookingsByOwnerIdPastTest() {
        saveBookingDto();
        when(bookingRepository.findBookingsByItemOwnerAndEndBeforeOrderByStartDesc(any(), any()))
                .thenReturn(of());
        List<BookingAllDto> bookings = bookingService.getBookingsByOwner(
                userDto.getId(),
                PAST.name()
        );
        assertEquals(bookings.size(), 0);
    }

    @Test
    void getBookingsByOwnerIdCurrentTest() {
        saveBookingDto();
        when(bookingRepository.findBookingsByItemOwnerIsAndStartBeforeAndEndAfterOrderByStartDesc(any(), any(), any()))
                .thenReturn(of());
        List<BookingAllDto> bookings = bookingService.getBookingsByOwner(
                userDto.getId(),
                CURRENT.name()
        );
        assertEquals(bookings.size(), 0);
    }

    @Test
    void getBookingsByOwnerIdFutureTest() {
        saveBookingDto();
        when(bookingRepository.findBookingsByItemOwnerAndStartAfterOrderByStartDesc(any(), any()))
                .thenReturn(of());
        List<BookingAllDto> bookings = bookingService.getBookingsByOwner(
                userDto.getId(),
                FUTURE.name()
        );
        assertEquals(bookings.size(), 0);
    }

    @Test
    void getBookingsByOwnerIdAnyTest() {
        saveBookingDto();
        when(bookingRepository.findBookingsByItemOwnerIsAndStatusIsOrderByStartDesc(any(), any()))
                .thenReturn(of());
        List<BookingAllDto> bookings = bookingService.getBookingsByOwner(
                userDto.getId(),
                CANCELED.name()
        );
        assertEquals(bookings.size(), 0);
    }

    @Test
    void getBookingsByOwnerIdPaginationNotNullTest() {
        saveBookingDto();
        when(bookingRepository.findBookingsByItemOwnerIsOrderByStartDesc(any(), any()))
                .thenReturn(empty());
        List<BookingAllDto> bookings = bookingService.getBookingsByOwner(
                userDto.getId(),
                ALL.name(),
                0,
                2
        );
        assertEquals(bookings.size(), 0);
    }

    @Test
    void getBookingsByOwnerIdPaginationPastTest() {
        saveBookingDto();
        when(bookingRepository.findBookingsByItemOwnerAndEndBeforeOrderByStartDesc(any(), any(), any()))
                .thenReturn(empty());
        List<BookingAllDto> bookings = bookingService.getBookingsByOwner(
                userDto.getId(),
                PAST.name(),
                0,
                2
        );
        assertEquals(bookings.size(), 0);
    }

    @Test
    void getBookingsByOwnerIdPaginationCurrentTest() {
        saveBookingDto();
        when(bookingRepository.findBookingsByItemOwnerIsAndStartBeforeAndEndAfterOrderByStartDesc(any(), any(), any(), any()))
                .thenReturn(empty());
        List<BookingAllDto> bookings = bookingService.getBookingsByOwner(
                userDto.getId(),
                CURRENT.name(),
                0,
                2
        );
        assertEquals(bookings.size(), 0);
    }

    @Test
    void getBookingsByOwnerIdPaginationFutureTest() {
        saveBookingDto();
        when(bookingRepository.findBookingsByItemOwnerAndStartAfterOrderByStartDesc(any(), any(), any()))
                .thenReturn(empty());
        List<BookingAllDto> bookings = bookingService.getBookingsByOwner(
                userDto.getId(),
                FUTURE.name(),
                0,
                2
        );
        assertEquals(bookings.size(), 0);
    }

    @Test
    void getBookingsByOwnerIdPaginationAnyTest() {
        saveBookingDto();
        when(bookingRepository.findBookingsByItemOwnerIsAndStatusIsOrderByStartDesc(any(), any(), any()))
                .thenReturn(empty());
        List<BookingAllDto> bookings = bookingService.getBookingsByOwner(
                userDto.getId(),
                CANCELED.name(),
                0,
                2
        );
        assertEquals(bookings.size(), 0);
    }

    @Test
    void getAllBookingsAllTest() {
        saveBookingDto();
        when(bookingRepository.findBookingsByBookerIsOrderByStartDesc(any()))
                .thenReturn(of());
        List<BookingAllDto> bookings = bookingService.getAll(
                userDto.getId(),
                ALL.name()
        );
        assertEquals(bookings.size(), 0);
    }

    @Test
    void getAllBookingsCurrentTest() {
        saveBookingDto();
        when(bookingRepository.findBookingsByBookerIsAndStartBeforeAndEndAfterOrderByStartDesc(any(), any(), any()))
                .thenReturn(of());
        List<BookingAllDto> bookings = bookingService.getAll(
                userDto.getId(),
                CURRENT.name()
        );
        assertEquals(bookings.size(), 0);
    }

    @Test
    void getAllBookingsFutureTest() {
        saveBookingDto();
        when(bookingRepository.findBookingsByBookerIsAndStartIsAfterOrderByStartDesc(any(), any()))
                .thenReturn(of());
        List<BookingAllDto> bookings = bookingService.getAll(
                userDto.getId(),
                FUTURE.name()
        );
        assertEquals(bookings.size(), 0);
    }

    @Test
    void getAllBookingsAnyTest() {
        saveBookingDto();
        when(bookingRepository.findBookingsByBookerIsAndStatusIsOrderByStartDesc(any(), any()))
                .thenReturn(of());
        List<BookingAllDto> bookings = bookingService.getAll(
                userDto.getId(),
                CANCELED.name()
        );
        assertEquals(bookings.size(), 0);
    }
}
