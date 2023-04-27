package ru.practicum.shareit.booking;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.enums.States;
import ru.practicum.shareit.exceptions.IncorrectItemException;
import ru.practicum.shareit.exceptions.IncorrectParameterException;
import ru.practicum.shareit.exceptions.ItemNotFoundException;
import ru.practicum.shareit.exceptions.ObjectNotFoundException;
import ru.practicum.shareit.item.Item;
import ru.practicum.shareit.item.ItemAllDto;
import ru.practicum.shareit.item.ItemMapper;
import ru.practicum.shareit.user.User;
import ru.practicum.shareit.user.UserMapper;
import ru.practicum.shareit.user.UserService;
import ru.practicum.shareit.user.dto.UserDto;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static java.util.Arrays.stream;
import static java.util.stream.Collectors.toList;
import static ru.practicum.shareit.enums.Status.*;

@Slf4j
@Service
@AllArgsConstructor
@Transactional(readOnly = true)
public class BookingServiceImpl implements BookingService {
    private final UserService userService;
    private final BookingStorage bookingStorage;

    @Override
    @Transactional
    public BookingAllDto save(BookingControllerDto bookingControllerDto, ItemAllDto itemDto, Long id) {
        if (itemDto.getOwnerId().equals(id))
            throw new ItemNotFoundException("Вещь с id = " + itemDto.getId() + " не можеь быть арендована");
        if (!itemDto.getAvailable())
            throw new IncorrectItemException("Вещь с id = " + itemDto.getId() + " уже арендована");
        valid(bookingControllerDto);
        User booker = UserMapper.toUser(userService.get(id));
        Item item = ItemMapper.toItem(itemDto);
        Booking booking = BookingMapper.toBooking(bookingControllerDto);
        booking.setStatus(WAITING);
        booking.setBooker(booker);
        booking.setItem(item);
        Booking savedBooking = bookingStorage.save(booking);
        return BookingMapper.mapToBookingAllFieldsDto(savedBooking);
    }

    @Override
    @Transactional
    public BookingAllDto approve(Long bookingId, boolean approved, Long userId) {
        Booking booking = bookingStorage.findById(bookingId).orElseThrow(
                () -> new ObjectNotFoundException("Такого бронирования не существует"));
        if (booking.getBooker().getId().equals(userId))
            throw new ObjectNotFoundException("Пользователь с id = " + userId + " не может одобрить заявку");
        if (!booking.getItem().getOwner().getId().equals(userId)
                || !booking.getStatus().equals(WAITING))
            throw new IncorrectParameterException("Бронирование не может быть обновлено");
        booking.setStatus(approved ? APPROVED : REJECTED);
        Booking savedBooking = bookingStorage.save(booking);
        return BookingMapper.mapToBookingAllFieldsDto(savedBooking);
    }

    @Override
    public List<BookingAllDto> getBookingsByOwner(Long userId, String state) {
        UserDto userDto = userService.get(userId);
        User user = UserMapper.toUser(userDto);
        if (state == null || States.ALL.name().equals(state)) {
            return bookingStorage.findBookingsByItemOwnerIsOrderByStartDesc(user)
                    .stream()
                    .map(BookingMapper::mapToBookingAllFieldsDto)
                    .collect(toList());
        }
        if (States.PAST.name().equals(state)) {
            return bookingStorage.findBookingsByItemOwnerAndEndBeforeOrderByStartDesc(user, LocalDateTime.now())
                    .stream()
                    .map(BookingMapper::mapToBookingAllFieldsDto)
                    .collect(toList());
        }
        if (States.CURRENT.name().equals(state)) {
            return bookingStorage.findBookingsByItemOwnerIsAndStartBeforeAndEndAfterOrderByStartDesc(
                            user, LocalDateTime.now(), LocalDateTime.now())
                    .stream()
                    .map(BookingMapper::mapToBookingAllFieldsDto)
                    .collect(toList());
        }
        if (States.FUTURE.name().equals(state)) {
            return bookingStorage.findBookingsByItemOwnerAndStartAfterOrderByStartDesc(
                            user, LocalDateTime.now())
                    .stream()
                    .map(BookingMapper::mapToBookingAllFieldsDto)
                    .collect(toList());
        }
        if (stream(values()).anyMatch(bookingState -> bookingState.name().equals(state))) {
            return bookingStorage.findBookingsByItemOwnerIsAndStatusIsOrderByStartDesc(
                            user, valueOf(state))
                    .stream()
                    .map(BookingMapper::mapToBookingAllFieldsDto)
                    .collect(toList());
        }
        throw new IncorrectParameterException("Unknown state: " + state);
    }

    @Override
    public List<BookingAllDto> getBookingsByItem(Long itemId, Long userId) {
        return bookingStorage.findBookingsByItem_IdAndItem_Owner_IdIsOrderByStart(
                        itemId, userId)
                .stream()
                .map(BookingMapper::mapToBookingAllFieldsDto)
                .collect(toList());
    }

    @Override
    public List<BookingAllDto> getAll(Long bookerId, String state) {
        UserDto userDto = userService.get(bookerId);
        User booker = UserMapper.toUser(userDto);
        if (state == null || States.ALL.name().equals(state)) {
            return bookingStorage.findBookingsByBookerIsOrderByStartDesc(booker)
                    .stream()
                    .map(BookingMapper::mapToBookingAllFieldsDto)
                    .collect(toList());
        }
        if (States.PAST.name().equals(state)) {
            return bookingStorage.findBookingsByBookerIsAndEndBeforeOrderByStartDesc(
                            booker, LocalDateTime.now())
                    .stream()
                    .map(BookingMapper::mapToBookingAllFieldsDto)
                    .collect(toList());
        }
        if (States.CURRENT.name().equals(state)) {
            return bookingStorage.findBookingsByBookerIsAndStartBeforeAndEndAfterOrderByStartDesc(
                            booker, LocalDateTime.now(), LocalDateTime.now())
                    .stream()
                    .map(BookingMapper::mapToBookingAllFieldsDto)
                    .collect(toList());
        }
        if (States.FUTURE.name().equals(state)) {
            return bookingStorage.findBookingsByBookerIsAndStartIsAfterOrderByStartDesc(
                            booker, LocalDateTime.now())
                    .stream()
                    .map(BookingMapper::mapToBookingAllFieldsDto)
                    .collect(toList());
        }
        if (stream(values()).anyMatch(bookingState -> bookingState.name().equals(state))) {
            return bookingStorage.findBookingsByBookerIsAndStatusIsOrderByStartDesc(
                            booker, valueOf(state))
                    .stream()
                    .map(BookingMapper::mapToBookingAllFieldsDto)
                    .collect(toList());
        }
        throw new IncorrectParameterException("Unknown state: " + state);
    }

    @Override
    public BookingAllDto get(Long bookingId, Long userId) {
        var booking = bookingStorage.findById(bookingId).orElseThrow(
                () -> new ObjectNotFoundException("Такого бронирования не существует"));
        if (!booking.getBooker().getId().equals(userId)
                && !booking.getItem().getOwner().getId().equals(userId)) {
            throw new ObjectNotFoundException("Данный пользователь не может получить информацию о бронировании");
        }
        return BookingMapper.mapToBookingAllFieldsDto(booking);
    }

    private void valid(BookingControllerDto bookingSavingDto) {
        if (bookingSavingDto.getStart() == null)
            throw new IncorrectParameterException("Не задана дата начала бронирования");
        if (bookingSavingDto.getEnd() == null)
            throw new IncorrectParameterException("Не задана дата окончания бронирования");
        if (bookingSavingDto.getStart().equals(bookingSavingDto.getEnd()))
            throw new IncorrectParameterException("Не задана дата начала бронирования");
        if (bookingSavingDto.getStart().toLocalDate().isBefore(LocalDate.now()))
            throw new IncorrectParameterException("Некорректная дата начала броинрования");
        if (bookingSavingDto.getEnd().isBefore(bookingSavingDto.getStart())
                || bookingSavingDto.getEnd().toLocalDate().isBefore(LocalDate.now()))
            throw new IncorrectParameterException("Некорректная дата бронирования");
    }
}
