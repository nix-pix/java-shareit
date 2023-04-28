package ru.practicum.shareit.booking;

import lombok.experimental.UtilityClass;
import ru.practicum.shareit.booking.dto.BookingAllDto;
import ru.practicum.shareit.booking.dto.BookingControllerDto;
import ru.practicum.shareit.enums.Status;
import ru.practicum.shareit.item.ItemMapper;
import ru.practicum.shareit.user.UserMapper;

@UtilityClass
public class BookingMapper {

    public Booking toBooking(BookingControllerDto bookingDto) {
        return Booking.builder()
                .start(bookingDto.getStart())
                .end(bookingDto.getEnd())
                .build();
    }

    public BookingAllDto mapToBookingAllFieldsDto(Booking booking) {
        return BookingAllDto.builder()
                .id(booking.getId())
                .start(booking.getStart())
                .end(booking.getEnd())
                .item(booking.getItem() != null ? ItemMapper.toItemShortDto(booking.getItem()) : null)
                .booker(booking.getBooker() != null ? UserMapper.toUserShortDto(booking.getBooker()) : null)
                .status(Status.valueOf(booking.getStatus().name()))
                .build();
    }
}
