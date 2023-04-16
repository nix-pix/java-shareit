package ru.practicum.shareit.item;

import org.springframework.stereotype.Component;
import ru.practicum.shareit.exceptions.NotOwnerException;
import ru.practicum.shareit.user.UserRepository;

import java.util.*;

@Component
public class ItemRepositoryImpl implements ItemRepository {
    final UserRepository userRepository;
    Map<Long, Map<Long, Item>> items = new HashMap<>();
    Map<Long, Item> allItems = new HashMap<>();
    Long id = 0L;

    public ItemRepositoryImpl(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public Item create(long userId, Item item) {
        userRepository.get(userId);
        item.setId(++id);
        item.setOwnerId(userId);
        if (items.containsKey(userId)) {
            items.get(userId).put(item.getId(), item);
            allItems.put(item.getId(), item);
        } else {
            Map<Long, Item> itemsList = new HashMap<>();
            itemsList.put(item.getId(), item);
            items.put(userId, itemsList);
            allItems.put(item.getId(), item);
        }
        return item;
    }

    @Override
    public Item update(long userId, long itemId, Item item) {
        if (allItems.get(itemId).getOwnerId() != userId) {
            throw new NotOwnerException("Редактирование вещи доступно только владельцу");
        }
        Item oldItem = allItems.get(itemId);
        if (item.getName() != null) {
            oldItem.setName(item.getName());
        }
        if (item.getDescription() != null) {
            oldItem.setDescription(item.getDescription());
        }
        if (item.getAvailable() != null) {
            oldItem.setAvailable(item.getAvailable());
        }
        items.get(userId).put(itemId, oldItem);
        allItems.put(itemId, oldItem);
        return oldItem;
    }

    @Override
    public void delete(long userId, long itemId) {
        items.get(userId).remove(itemId);
        allItems.remove(itemId);
    }

    @Override
    public Item get(long itemId) {
        return allItems.get(itemId);
    }

    @Override
    public List<Item> getAllUsersItems(long userId) {
        return new ArrayList<>(items.get(userId).values());
    }

    @Override
    public Collection<Item> getAll() {
        return allItems.values();
    }
}
