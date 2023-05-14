package ru.practicum.shareit.booking;

import lombok.AllArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.booking.dto.BookingAllDto;
import ru.practicum.shareit.booking.dto.BookingControllerDto;
import ru.practicum.shareit.enums.Status;
import ru.practicum.shareit.exception.IncorrectParameterException;
import ru.practicum.shareit.exception.ObjectNotFoundException;
import ru.practicum.shareit.item.Item;
import ru.practicum.shareit.item.ItemMapper;
import ru.practicum.shareit.item.dto.ItemAllDto;
import ru.practicum.shareit.user.User;
import ru.practicum.shareit.user.UserMapper;
import ru.practicum.shareit.user.UserService;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Stream;

import static java.time.LocalDateTime.now;
import static java.util.stream.Collectors.toList;
import static ru.practicum.shareit.enums.State.*;
import static ru.practicum.shareit.enums.Status.*;
import static ru.practicum.shareit.util.Pagination.makePageRequest;

@Service
@AllArgsConstructor
@Transactional(readOnly = true)
public class BookingServiceImpl implements BookingService {
    private final UserService userService;
    private final BookingRepository bookingRepository;

    @Override
    @Transactional
    public BookingAllDto save(BookingControllerDto bookingControllerDto, ItemAllDto itemDto, Long id) {
        if (itemDto.getOwnerId().equals(id))
            throw new ObjectNotFoundException("Вещь с id = " + itemDto.getId() + " не может быть арендована");
        if (!itemDto.getAvailable())
            throw new IncorrectParameterException("Вещь с id = " + itemDto.getId() + " уже арендована");
        valid(bookingControllerDto);
        User booker = UserMapper.toUser(userService.get(id));
        Item item = ItemMapper.toItem(itemDto);
        Booking booking = BookingMapper.toBooking(bookingControllerDto);
        booking.setStatus(WAITING);
        booking.setBooker(booker);
        booking.setItem(item);
        return BookingMapper.mapToBookingAllFieldsDto(bookingRepository.save(booking));
    }

    @Override
    @Transactional
    public BookingAllDto approve(Long bookingId, boolean approved, Long userId) {
        Booking booking = bookingRepository.findById(bookingId).orElseThrow(
                () -> new ObjectNotFoundException("Такого бронирования не существует"));
        if (booking.getBooker().getId().equals(userId))
            throw new ObjectNotFoundException("Пользователь с id = " + userId + " не может одобрить заявку");
        if (!booking.getItem().getOwner().getId().equals(userId)
                || !booking.getStatus().equals(WAITING))
            throw new IncorrectParameterException("Бронирование не может быть обновлено");
        booking.setStatus(approved ? APPROVED : REJECTED);
        Booking savedBooking = bookingRepository.save(booking);
        return BookingMapper.mapToBookingAllFieldsDto(savedBooking);
    }

    @Override
    public List<BookingAllDto> getBookingsByOwner(Long userId, String state) {
        Stream<Booking> stream = null;
        User user = UserMapper.toUser(userService.get(userId));
        if (state == null || ALL.name().equals(state))
            stream = bookingRepository.findBookingsByItemOwnerIsOrderByStartDesc(user)
                    .stream();
        if (PAST.name().equals(state))
            stream = bookingRepository
                    .findBookingsByItemOwnerAndEndBeforeOrderByStartDesc(user, now())
                    .stream();
        if (CURRENT.name().equals(state))
            stream = bookingRepository
                    .findBookingsByItemOwnerIsAndStartBeforeAndEndAfterOrderByStartDesc(user, now(), now())
                    .stream();
        if (FUTURE.name().equals(state))
            stream = bookingRepository
                    .findBookingsByItemOwnerAndStartAfterOrderByStartDesc(user, now())
                    .stream();
        if (Arrays.stream(Status.values()).anyMatch(bookingState -> bookingState.name().equals(state)))
            stream = bookingRepository
                    .findBookingsByItemOwnerIsAndStatusIsOrderByStartDesc(user, Status.valueOf(state))
                    .stream();
        if (stream != null)
            return stream
                    .map(BookingMapper::mapToBookingAllFieldsDto)
                    .collect(toList());
        else
            throw new IncorrectParameterException("Unknown state: " + state);
    }

    @Override
    public List<BookingAllDto> getBookingsByOwner(Long userId, String state, Integer from, Integer size) {
        Stream<Booking> stream = null;
        PageRequest pageRequest = makePageRequest(from, size, Sort.by("start").descending());
        User user = UserMapper.toUser(userService.get(userId));
        if (state == null || state.equals(ALL.name())) {
            if (pageRequest == null)
                stream = bookingRepository
                        .findBookingsByItemOwnerIsOrderByStartDesc(user)
                        .stream();
            else
                stream = bookingRepository
                        .findBookingsByItemOwnerIsOrderByStartDesc(user, pageRequest)
                        .stream();
        }
        if (PAST.name().equals(state)) {
            if (pageRequest == null)
                stream = bookingRepository
                        .findBookingsByItemOwnerAndEndBeforeOrderByStartDesc(user, LocalDateTime.now())
                        .stream();
            else
                stream = bookingRepository
                        .findBookingsByItemOwnerAndEndBeforeOrderByStartDesc(user, LocalDateTime.now(), pageRequest)
                        .stream();
        }
        if (CURRENT.name().equals(state)) {
            if (pageRequest == null)
                stream = bookingRepository
                        .findBookingsByItemOwnerIsAndStartBeforeAndEndAfterOrderByStartDesc(user, LocalDateTime.now(), LocalDateTime.now())
                        .stream();
            else
                stream = bookingRepository
                        .findBookingsByItemOwnerIsAndStartBeforeAndEndAfterOrderByStartDesc(user, LocalDateTime.now(), LocalDateTime.now(), pageRequest)
                        .stream();
        }
        if (FUTURE.name().equals(state)) {
            if (pageRequest == null)
                stream = bookingRepository
                        .findBookingsByItemOwnerAndStartAfterOrderByStartDesc(user, now())
                        .stream();
            else
                stream = bookingRepository
                        .findBookingsByItemOwnerAndStartAfterOrderByStartDesc(user, now(), pageRequest)
                        .stream();
        }
        if (Arrays.stream(Status.values()).anyMatch(bookingState -> bookingState.name().equals(state))) {
            if (pageRequest == null)
                stream = bookingRepository
                        .findBookingsByItemOwnerIsAndStatusIsOrderByStartDesc(user, Status.valueOf(state))
                        .stream();
            else
                stream = bookingRepository
                        .findBookingsByItemOwnerIsAndStatusIsOrderByStartDesc(user, Status.valueOf(state), pageRequest)
                        .stream();
        }
        if (stream != null)
            return stream
                    .map(BookingMapper::mapToBookingAllFieldsDto)
                    .collect(toList());
        else
            throw new IncorrectParameterException("Unknown state: " + state);
    }

    @Override
    public List<BookingAllDto> getBookingsByItem(Long itemId, Long userId) {
        return bookingRepository.findBookingsByItem_IdAndItem_Owner_IdIsOrderByStart(
                        itemId, userId)
                .stream()
                .map(BookingMapper::mapToBookingAllFieldsDto)
                .collect(toList());
    }

    @Override
    public List<BookingAllDto> getAll(Long bookerId, String state) {
        Stream<Booking> stream = null;
        User user = UserMapper.toUser(userService.get(bookerId));
        if (state == null || ALL.name().equals(state))
            stream = bookingRepository
                    .findBookingsByBookerIsOrderByStartDesc(user)
                    .stream();
        if (PAST.name().equals(state))
            stream = bookingRepository
                    .findBookingsByBookerIsAndEndBeforeOrderByStartDesc(user, LocalDateTime.now())
                    .stream();
        if (CURRENT.name().equals(state))
            stream = bookingRepository
                    .findBookingsByBookerIsAndStartBeforeAndEndAfterOrderByStartDesc(user, LocalDateTime.now(), LocalDateTime.now())
                    .stream();
        if (FUTURE.name().equals(state))
            stream = bookingRepository
                    .findBookingsByBookerIsAndStartIsAfterOrderByStartDesc(user, LocalDateTime.now())
                    .stream();
        if (Arrays.stream(Status.values()).anyMatch(bookingState -> bookingState.name().equals(state)))
            stream = bookingRepository
                    .findBookingsByBookerIsAndStatusIsOrderByStartDesc(user, Status.valueOf(state))
                    .stream();
        if (stream != null)
            return stream
                    .map(BookingMapper::mapToBookingAllFieldsDto)
                    .collect(toList());
        else
            throw new IncorrectParameterException("Unknown state: " + state);
    }

    @Override
    public List<BookingAllDto> getAll(Long bookerId, String state, Integer from, Integer size) {
        Stream<Booking> stream = null;
        PageRequest pageRequest = makePageRequest(from, size, Sort.by("start").descending());
        User user = UserMapper.toUser(userService.get(bookerId));
        if (state == null || ALL.name().equals(state)) {
            if (pageRequest == null)
                stream = bookingRepository
                        .findBookingsByBookerIsOrderByStartDesc(user)
                        .stream();
            else
                stream = bookingRepository
                        .findBookingsByBookerIsOrderByStartDesc(user, pageRequest)
                        .stream();
        }
        if (PAST.name().equals(state)) {
            if (pageRequest == null)
                stream = bookingRepository
                        .findBookingsByBookerIsAndEndBeforeOrderByStartDesc(user, LocalDateTime.now())
                        .stream();
            else
                stream = bookingRepository
                        .findBookingsByBookerIsAndEndBeforeOrderByStartDesc(user, LocalDateTime.now(), pageRequest)
                        .stream();
        }
        if (CURRENT.name().equals(state)) {
            if (pageRequest == null)
                stream = bookingRepository
                        .findBookingsByBookerIsAndStartBeforeAndEndAfterOrderByStartDesc(user, LocalDateTime.now(), LocalDateTime.now())
                        .stream();
            else
                stream = bookingRepository
                        .findBookingsByBookerIsAndStartBeforeAndEndAfterOrderByStartDesc(user, LocalDateTime.now(), LocalDateTime.now(), pageRequest)
                        .stream();
        }
        if (FUTURE.name().equals(state)) {
            if (pageRequest == null)
                stream = bookingRepository
                        .findBookingsByBookerIsAndStartIsAfterOrderByStartDesc(user, LocalDateTime.now())
                        .stream();
            else
                stream = bookingRepository
                        .findBookingsByBookerIsAndStartIsAfterOrderByStartDesc(user, LocalDateTime.now(), pageRequest)
                        .stream();
        }
        if (Arrays.stream(Status.values()).anyMatch(bookingState -> bookingState.name().equals(state))) {
            if (pageRequest == null)
                stream = bookingRepository
                        .findBookingsByBookerIsAndStatusIsOrderByStartDesc(user, Status.valueOf(state))
                        .stream();
            else
                stream = bookingRepository
                        .findBookingsByBookerIsAndStatusIsOrderByStartDesc(user, Status.valueOf(state), pageRequest)
                        .stream();
        }
        if (stream != null)
            return stream
                    .map(BookingMapper::mapToBookingAllFieldsDto)
                    .collect(toList());
        else
            throw new IncorrectParameterException("Unknown state: " + state);
    }

    @Override
    public BookingAllDto get(Long bookingId, Long userId) {
        Booking booking = bookingRepository.findById(bookingId).orElseThrow(
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
            throw new IncorrectParameterException("Некорректная дата начала бронирования");
        if (bookingSavingDto.getEnd().isBefore(bookingSavingDto.getStart())
                || bookingSavingDto.getEnd().toLocalDate().isBefore(LocalDate.now()))
            throw new IncorrectParameterException("Некорректная дата бронирования");
    }
}
