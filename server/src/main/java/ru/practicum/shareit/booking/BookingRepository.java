package ru.practicum.shareit.booking;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import ru.practicum.shareit.enums.Status;
import ru.practicum.shareit.user.User;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface BookingRepository extends JpaRepository<Booking, Long> {

    //for owner
    List<Booking> findBookingsByItemOwnerIsAndStartBeforeAndEndAfterOrderByStartDesc(User owner,
                                                                                     LocalDateTime startDateTime,
                                                                                     LocalDateTime endDateTime);

    Page<Booking> findBookingsByItemOwnerIsAndStartBeforeAndEndAfterOrderByStartDesc(User owner,
                                                                                     LocalDateTime startDateTime,
                                                                                     LocalDateTime endDateTime,
                                                                                     Pageable pageable);

    List<Booking> findBookingsByItemOwnerAndStartAfterOrderByStartDesc(User owner,
                                                                       LocalDateTime localDateTime);

    Page<Booking> findBookingsByItemOwnerAndStartAfterOrderByStartDesc(User owner,
                                                                       LocalDateTime localDateTime,
                                                                       Pageable pageable);

    List<Booking> findBookingsByItemOwnerAndEndBeforeOrderByStartDesc(User owner,
                                                                      LocalDateTime localDateTime);

    Page<Booking> findBookingsByItemOwnerAndEndBeforeOrderByStartDesc(User owner,
                                                                      LocalDateTime localDateTime,
                                                                      Pageable pageable);

    List<Booking> findBookingsByItemOwnerIsAndStatusIsOrderByStartDesc(User owner,
                                                                       Status bookingState);

    Page<Booking> findBookingsByItemOwnerIsAndStatusIsOrderByStartDesc(User owner,
                                                                       Status bookingState,
                                                                       Pageable pageable);

    List<Booking> findBookingsByItem_IdAndItem_Owner_IdIsOrderByStart(Long itemId,
                                                                      Long userId);

    Page<Booking> findBookingsByItemOwnerIsOrderByStartDesc(User owner,
                                                            Pageable pageable);

    List<Booking> findBookingsByItemOwnerIsOrderByStartDesc(User owner);

    //for booker
    List<Booking> findBookingsByBookerIsAndStartBeforeAndEndAfterOrderByStartDesc(User booker,
                                                                                  LocalDateTime startDateTime,
                                                                                  LocalDateTime endDateTime);

    Page<Booking> findBookingsByBookerIsAndStartBeforeAndEndAfterOrderByStartDesc(User booker,
                                                                                  LocalDateTime startDateTime,
                                                                                  LocalDateTime endDateTime,
                                                                                  Pageable pageable);

    List<Booking> findBookingsByBookerIsAndStartIsAfterOrderByStartDesc(User booker,
                                                                        LocalDateTime localDateTime);

    Page<Booking> findBookingsByBookerIsAndStartIsAfterOrderByStartDesc(User booker,
                                                                        LocalDateTime localDateTime,
                                                                        Pageable pageable);

    List<Booking> findBookingsByBookerIsAndEndBeforeOrderByStartDesc(User booker,
                                                                     LocalDateTime localDateTime);

    Page<Booking> findBookingsByBookerIsAndEndBeforeOrderByStartDesc(User booker,
                                                                     LocalDateTime localDateTime,
                                                                     Pageable pageable);

    List<Booking> findBookingsByItem_IdIsAndStatusIsAndEndIsAfter(Long itemId,
                                                                  Status bookingState,
                                                                  LocalDateTime localDateTime);

    List<Booking> findBookingsByBookerIsAndStatusIsOrderByStartDesc(User booker,
                                                                    Status bookingState);

    Page<Booking> findBookingsByBookerIsAndStatusIsOrderByStartDesc(User booker,
                                                                    Status bookingState,
                                                                    Pageable pageable);

    Page<Booking> findBookingsByBookerIsOrderByStartDesc(User booker,
                                                         Pageable pageable);

    List<Booking> findBookingsByBookerIsOrderByStartDesc(User booker);

    @Query("select b from Booking b where b.item.id = ?1 and b.item.owner.id = ?2 and b.status = 'APPROVED' order by b.start DESC")
    List<Booking> findApprovedBookings(Long itemId, Long userId);
}
