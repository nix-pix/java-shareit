package ru.practicum.shareit.booking;

import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.item.ItemAllDto;
import ru.practicum.shareit.item.ItemService;

import java.util.List;

@RestController
@AllArgsConstructor
@RequestMapping("/bookings")
public class BookingController {
    private final BookingService bookingService;
    private final ItemService itemService;

    @PostMapping()
    public BookingAllDto save(@RequestBody BookingControllerDto bookingControllerDto,
                              @RequestHeader(value = "X-Sharer-User-Id", required = false)
                              Long userId) {
        ItemAllDto item = itemService.get(bookingControllerDto.getItemId(), userId);
        return bookingService.save(bookingControllerDto, item, userId);
    }

    @PatchMapping("/{bookingId}")
    public BookingAllDto approve(@PathVariable Long bookingId,
                                 @RequestParam(required = false) boolean approved,
                                 @RequestHeader(value = "X-Sharer-User-Id", required = false)
                                 Long userId) {
        return bookingService.approve(bookingId, approved, userId);
    }

    @GetMapping("/owner")
    public List<BookingAllDto> getBookingsByOwner(@RequestParam(required = false) String state,
                                                  @RequestHeader(value = "X-Sharer-User-Id", required = false)
                                                  Long userId) {
        return bookingService.getBookingsByOwner(userId, state);
    }

    @GetMapping()
    public List<BookingAllDto> getAll(@RequestParam(required = false) String state,
                                      @RequestHeader(value = "X-Sharer-User-Id", required = false)
                                      Long userId) {
        return bookingService.getAll(userId, state);
    }

    @GetMapping("/{bookingId}")
    public BookingAllDto get(@PathVariable Long bookingId,
                             @RequestHeader(value = "X-Sharer-User-Id", required = false)
                             Long userId) {
        return bookingService.get(bookingId, userId);
    }
}
