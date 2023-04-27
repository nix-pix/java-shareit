package ru.practicum.shareit.booking;

import ru.practicum.shareit.enums.Status;
import ru.practicum.shareit.item.ItemMapper;
import ru.practicum.shareit.user.UserMapper;

public class BookingMapper {

    public static Booking toBooking(BookingControllerDto bookingDto) {
        return Booking.builder()
                .start(bookingDto.getStart())
                .end(bookingDto.getEnd())
                .build();
    }

    public static BookingAllDto mapToBookingAllFieldsDto(Booking booking) {
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
