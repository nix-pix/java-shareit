package ru.practicum.shareit.booking;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
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

    List<Booking> findBookingsByItemOwnerIsOrderByStartDesc(User user);

    List<Booking> findBookingsByBookerIsAndStartBeforeAndEndAfterOrderByStartDesc(User booker,
                                                                                  LocalDateTime startDateTime,
                                                                                  LocalDateTime endDateTime);

    List<Booking> findBookingsByBookerIsAndStartIsAfterOrderByStartDesc(User booker,
                                                                        LocalDateTime localDateTime);

    List<Booking> findBookingsByBookerIsAndEndBeforeOrderByStartDesc(User booker,
                                                                     LocalDateTime localDateTime);

    List<Booking> findBookingsByBookerIsAndStatusIsOrderByStartDesc(User booker,
                                                                    Status bookingState);

    List<Booking> findBookingsByBookerIsOrderByStartDesc(User booker);

    @Query("select b from Booking b where b.item.id = ?1 and b.item.owner.id = ?2 and b.status = 'APPROVED' order by b.start DESC")
    List<Booking> findApprovedBookings(Long itemId, Long userId);
}
