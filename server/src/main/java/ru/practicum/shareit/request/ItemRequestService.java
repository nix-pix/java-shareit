package ru.practicum.shareit.request;

import ru.practicum.shareit.request.dto.ItemRequestDto;

import java.util.List;

public interface ItemRequestService {

    List<ItemRequestDto> getAllItemRequests(Long userId);

    List<ItemRequestDto> getAllItemRequests(Integer from, Integer size, Long userId);

    ItemRequestDto save(ItemRequestDto itemRequestDto, Long requesterId);

    ItemRequestDto getItemRequestById(long requestId, Long userId);
}
