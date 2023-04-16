package ru.practicum.shareit.item;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

@Service
@AllArgsConstructor
public class ItemServiceImpl implements ItemService {
    ItemRepository itemRepository;

    @Override
    public Item create(long userId, Item item) {
        return itemRepository.create(userId, item);
    }

    @Override
    public Item update(long userId, long itemId, Item item) {
        return itemRepository.update(userId, itemId, item);
    }

    @Override
    public void delete(long userId, long itemId) {
        itemRepository.delete(userId, itemId);
    }

    @Override
    public Item get(long itemId) {
        return itemRepository.get(itemId);
    }

    @Override
    public List<Item> getAllUsersItems(long userId) {
        return itemRepository.getAllUsersItems(userId);
    }

    @Override
    public List<Item> search(String text) { //Поиск вещи по наличию текста в имени или описании
        List<Item> items = new ArrayList<>();
        for (Item i : itemRepository.getAll()) {
            if (i.getName().toLowerCase(Locale.ROOT).contains(text) || i.getDescription().toLowerCase().contains(text)) {
                if (i.getAvailable()) {
                    items.add(i);
                }
            }
        }
        return items;
    }
}
