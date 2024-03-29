package ru.practicum.shareit.booking.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import javax.validation.constraints.Future;
import javax.validation.constraints.FutureOrPresent;
import java.time.LocalDateTime;

@Data
@AllArgsConstructor
public class BookingRequestDto {
    private Long itemId;
    @FutureOrPresent(message = "Incorrect start date of booking")
    private LocalDateTime start;
    @Future(message = "Incorrect end date of booking")
    private LocalDateTime end;
}
