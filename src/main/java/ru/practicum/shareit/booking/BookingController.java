package ru.practicum.shareit.booking;

import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.booking.dto.BookingAllDto;
import ru.practicum.shareit.booking.dto.BookingControllerDto;
import ru.practicum.shareit.item.ItemService;
import ru.practicum.shareit.item.dto.ItemAllDto;

import java.util.List;

@RestController
@AllArgsConstructor
@RequestMapping("/bookings")
public class BookingController {
    private final BookingService bookingService;
    private final ItemService itemService;

    @PostMapping()
    public BookingAllDto save(@RequestBody BookingControllerDto bookingControllerDto,
                              @RequestHeader(value = "X-Sharer-User-Id")
                              Long userId) {
        ItemAllDto item = itemService.get(bookingControllerDto.getItemId(), userId);
        return bookingService.save(bookingControllerDto, item, userId);
    }

    @PatchMapping("/{bookingId}")
    public BookingAllDto approve(@PathVariable Long bookingId,
                                 @RequestParam boolean approved,
                                 @RequestHeader(value = "X-Sharer-User-Id")
                                 Long userId) {
        return bookingService.approve(bookingId, approved, userId);
    }

    @GetMapping("/owner")
    public List<BookingAllDto> getBookingsByOwner(@RequestParam(defaultValue = "ALL") String state,
                                                  @RequestHeader(value = "X-Sharer-User-Id")
                                                  Long userId) {
        return bookingService.getBookingsByOwner(userId, state);
    }

    @GetMapping()
    public List<BookingAllDto> getAll(@RequestParam(defaultValue = "ALL") String state,
                                      @RequestHeader(value = "X-Sharer-User-Id")
                                      Long userId) {
        return bookingService.getAll(userId, state);
    }

    @GetMapping("/{bookingId}")
    public BookingAllDto get(@PathVariable Long bookingId,
                             @RequestHeader(value = "X-Sharer-User-Id")
                             Long userId) {
        return bookingService.get(bookingId, userId);
    }
}
