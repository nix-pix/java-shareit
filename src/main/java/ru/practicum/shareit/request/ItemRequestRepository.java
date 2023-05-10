package ru.practicum.shareit.request;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import ru.practicum.shareit.user.User;

import java.util.List;

public interface ItemRequestRepository extends JpaRepository<ItemRequest, Long> {

    Page<ItemRequest> findItemRequestByRequester_IdIsNotOrderByCreatedDesc(Long userId, Pageable pageable);

    List<ItemRequest> findItemRequestByRequester_IdIsNotOrderByCreatedDesc(Long userId);

    List<ItemRequest> findItemRequestByRequesterOrderByCreatedDesc(User user);
}
