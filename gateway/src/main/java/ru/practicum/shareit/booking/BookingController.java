package ru.practicum.shareit.booking;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.booking.dto.BookingRequestDto;
import ru.practicum.shareit.booking.dto.BookingState;
import ru.practicum.shareit.exception.IncorrectParameterException;

import javax.validation.Valid;
import javax.validation.constraints.Positive;
import javax.validation.constraints.PositiveOrZero;
import java.time.LocalDate;

@Validated
@Controller
@RequiredArgsConstructor
@RequestMapping(path = "/bookings")
public class BookingController {
    private static final String HEADER_SHARER_USER_ID = "X-Sharer-User-Id";
    private final BookingClient bookingClient;

    @GetMapping("/owner")
    public ResponseEntity<Object> getOwnerBookings(@PositiveOrZero @RequestParam(name = "from", defaultValue = "0") Integer from,
                                                   @Positive @RequestParam(name = "size", defaultValue = "10") Integer size,
                                                   @RequestParam(name = "state", defaultValue = "all") String stateParam,
                                                   @RequestHeader(HEADER_SHARER_USER_ID) Long userId) {
        BookingState state = BookingState.from(stateParam).orElseThrow(
                () -> new IncorrectParameterException("Unknown state: " + stateParam));
        return bookingClient.getOwnerBookings(userId, state, from, size);
    }

    @GetMapping
    public ResponseEntity<Object> getBookings(@PositiveOrZero @RequestParam(name = "from", defaultValue = "0") Integer from,
                                              @Positive @RequestParam(name = "size", defaultValue = "10") Integer size,
                                              @RequestParam(name = "state", defaultValue = "all") String stateParam,
                                              @RequestHeader(HEADER_SHARER_USER_ID) Long userId) {
        BookingState state = BookingState.from(stateParam).orElseThrow(
                () -> new IncorrectParameterException("Unknown state: " + stateParam));
        return bookingClient.getBookings(userId, state, from, size);
    }

    @PatchMapping("/{bookingId}")
    public ResponseEntity<Object> approveBooking(@RequestHeader(value = HEADER_SHARER_USER_ID) Long userId,
                                                 @RequestParam(required = false) Boolean approved,
                                                 @PathVariable Integer bookingId) {
        return bookingClient.approveBooking(bookingId, approved, userId);
    }

    @PostMapping
    public ResponseEntity<Object> createBooking(@RequestHeader(HEADER_SHARER_USER_ID) Long userId,
                                                @RequestBody @Valid BookingRequestDto requestDto) {
        if (requestDto.getStart() == null)
            throw new IncorrectParameterException("Не задана дата начала бронирования");
        if (requestDto.getEnd() == null)
            throw new IncorrectParameterException("Не задана дата окончания бронирования");
        if (requestDto.getStart().isAfter(requestDto.getEnd()))
            throw new IncorrectParameterException("Некорректная дата бронирования");
        if (requestDto.getStart().toLocalDate().isBefore(LocalDate.now()))
            throw new IncorrectParameterException("Некорректная дата начала бронирования");
        if (requestDto.getEnd().isBefore(requestDto.getStart())
                || requestDto.getEnd().toLocalDate().isBefore(LocalDate.now()))
            throw new IncorrectParameterException("Некорректная дата бронирования");
        return bookingClient.createBooking(userId, requestDto);
    }

    @GetMapping("/{bookingId}")
    public ResponseEntity<Object> getBooking(@RequestHeader(HEADER_SHARER_USER_ID) Long userId,
                                             @PathVariable Long bookingId) {
        return bookingClient.getBooking(userId, bookingId);
    }
}
