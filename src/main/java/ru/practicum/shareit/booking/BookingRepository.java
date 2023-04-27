package ru.practicum.shareit.booking;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.practicum.shareit.enums.Status;
import ru.practicum.shareit.user.User;

import java.time.LocalDateTime;
import java.util.List;

public interface BookingRepository extends JpaRepository<Booking, Long> {

    List<Booking> findBookingsByItemOwnerIsAndStartBeforeAndEndAfterOrderByStartDesc(User user,
                                                                                     LocalDateTime startDateTime,
                                                                                     LocalDateTime endDateTime);

    List<Booking> findBookingsByItemOwnerAndStartAfterOrderByStartDesc(User user,
                                                                       LocalDateTime localDateTime);

    List<Booking> findBookingsByItemOwnerAndEndBeforeOrderByStartDesc(User user,
                                                                      LocalDateTime localDateTime);

    List<Booking> findBookingsByItemOwnerIsAndStatusIsOrderByStartDesc(User user,
                                                                       Status bookingState);

    List<Booking> findBookingsByItem_IdAndItem_Owner_IdIsOrderByStart(Long itemId, Long userId);

    List<Booking> findBookingsByItemOwnerIsOrderByStartDesc(User user);

    //for booker
    List<Booking> findBookingsByBookerIsAndStartBeforeAndEndAfterOrderByStartDesc(User booker,
                                                                                  LocalDateTime startDateTime,
                                                                                  LocalDateTime endDateTime);

    List<Booking> findBookingsByBookerIsAndStartIsAfterOrderByStartDesc(User booker,
                                                                        LocalDateTime localDateTime);

    List<Booking> findBookingsByBookerIsAndEndBeforeOrderByStartDesc(User booker,
                                                                     LocalDateTime localDateTime);

    List<Booking> findBookingsByBookerIsAndStatusIsOrderByStartDesc(User booker,
                                                                    Status bookingState);

    List<Booking> findBookingsByItem_IdIsAndStatusIsAndEndIsAfter(Long itemId,
                                                                  Status bookingState,
                                                                  LocalDateTime localDateTime);

    List<Booking> findBookingsByBookerIsOrderByStartDesc(User booker);
}
