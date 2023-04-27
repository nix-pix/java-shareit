package ru.practicum.shareit.item;

import java.util.List;

public interface ItemService {

    ItemDto create(ItemDto item, long userId);

    ItemDto update(ItemDto item, Long id, Long userId);

//    void delete(long userId, long itemId);

    ItemAllDto get(long itemId, Long userId);

    List<ItemAllDto> getAll(long id);

    List<ItemDto> search(String text);
}
