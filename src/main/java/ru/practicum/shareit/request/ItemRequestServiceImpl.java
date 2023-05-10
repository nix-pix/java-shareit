package ru.practicum.shareit.request;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.exception.IncorrectParameterException;
import ru.practicum.shareit.exception.ObjectNotFoundException;
import ru.practicum.shareit.item.ItemService;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.request.dto.ItemRequestDto;
import ru.practicum.shareit.user.User;
import ru.practicum.shareit.user.UserMapper;
import ru.practicum.shareit.user.UserService;

import javax.transaction.Transactional;
import java.util.List;
import java.util.Map;

import static java.time.LocalDateTime.now;
import static java.util.stream.Collectors.groupingBy;
import static java.util.stream.Collectors.toList;
import static ru.practicum.shareit.util.Pagination.makePageRequest;

@Slf4j
@Service
@Transactional
public class ItemRequestServiceImpl implements ItemRequestService {
    private final ItemRequestRepository itemRequestRepository;
    private final UserService userService;
    private final ItemService itemService;

    @Autowired
    public ItemRequestServiceImpl(ItemRequestRepository itemRequestRepository, UserService userService, ItemService itemService) {
        this.itemRequestRepository = itemRequestRepository;
        this.userService = userService;
        this.itemService = itemService;
    }

    @Override
    public List<ItemRequestDto> getAllItemRequests(Integer from, Integer size, Long userId) {
        List<ItemRequest> requests;
        if (from == null) {
            from = 0;
        } else if (size == null) {
            size = 10;
        }
        PageRequest pageRequest = makePageRequest(from, size, Sort.by("created").descending());
        if (pageRequest == null) {
            requests = itemRequestRepository.findItemRequestByRequester_IdIsNotOrderByCreatedDesc(userId);
        } else {
            requests = itemRequestRepository.findItemRequestByRequester_IdIsNotOrderByCreatedDesc(userId, pageRequest)
                    .stream()
                    .collect(toList());
        }
        List<ItemDto> items = itemService.getItemsByRequests(requests);
        return requests
                .stream()
                .map(itemRequest -> ItemRequestMapper.mapToItemRequestDto(itemRequest, items))
                .collect(toList());
    }

    @Override
    public List<ItemRequestDto> getAllItemRequests(Long userId) {
        User user = UserMapper.toUser(userService.get(userId));
        List<ItemRequest> itemRequests = itemRequestRepository.findItemRequestByRequesterOrderByCreatedDesc(user);
        List<ItemDto> items = itemService.getItemsByRequests(itemRequests);
        Map<Long, List<ItemDto>> itemsByRequest = items
                .stream()
                .collect(groupingBy(ItemDto::getRequestId, toList()));
        return itemRequests.stream()
                .map(itemRequest -> ItemRequestMapper.mapToItemRequestDto(itemRequest, itemsByRequest.get(itemRequest.getId())))
                .collect(toList());
    }

    @Override
    public ItemRequestDto save(ItemRequestDto itemRequestDto, Long requesterId) {
        valid(itemRequestDto);
        User user = UserMapper.toUser(userService.get(requesterId));
        ItemRequest itemRequest = ItemRequestMapper.mapToItemRequest(itemRequestDto);
        itemRequest.setRequester(user);
        itemRequest.setCreated(now());
        ItemRequest requestForSave = itemRequestRepository.save(itemRequest);
        return ItemRequestMapper.mapToItemRequestDto(requestForSave);
    }

    @Override
    public ItemRequestDto getItemRequestById(long requestId, Long userId) {
        User owner = UserMapper.toUser(userService.get(userId));
        if (owner != null) {
            List<ItemDto> items = itemService.getItemsByRequestId(requestId);
            ItemRequest itemRequest = itemRequestRepository.findById(requestId).orElseThrow(
                    () -> new ObjectNotFoundException("Запрос с id = " + requestId + " не найден"));
            return ItemRequestMapper.mapToItemRequestDto(itemRequest, items);
        } else {
            throw new ObjectNotFoundException("Пользователь с id" + userId + "не найден");
        }
    }

    private void valid(ItemRequestDto itemRequestDto) {
        if (itemRequestDto.getDescription() == null || itemRequestDto.getDescription().isBlank()) {
            throw new IncorrectParameterException("Запрос не может быть null или пустым");
        }
    }
}
