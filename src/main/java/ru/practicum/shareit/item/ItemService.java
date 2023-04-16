package ru.practicum.shareit.item;

import java.util.List;

public interface ItemService {

    Item create(long userId, Item item);

    Item update(long userId, long itemId, Item item);

    void delete(long userId, long itemId);

    Item get(long itemId);

    List<Item> getAllUsersItems(long userId);

    List<Item> search(String text);
}
