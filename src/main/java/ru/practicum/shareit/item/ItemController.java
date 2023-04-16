package ru.practicum.shareit.item;

import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/items")
@AllArgsConstructor
public class ItemController {
    ItemService itemService;

    @PostMapping
    public ItemDto create(@RequestHeader("X-Sharer-User-Id") long userId,
                          @Valid @RequestBody ItemDto itemDto) {
        return ItemMapper.toItemDto(itemService.create(userId, ItemMapper.toItem(itemDto)));
    }

    @PatchMapping("/{itemId}")
    public ItemDto update(@RequestHeader("X-Sharer-User-Id") long userId,
                          @RequestBody ItemDto itemDto,
                          @PathVariable long itemId) {
        Item item = ItemMapper.toItem(itemDto);
        return ItemMapper.toItemDto(itemService.update(userId, itemId, item));
    }

    @DeleteMapping("/{itemId}")
    public void delete(@RequestHeader("X-Sharer-User-Id") long userId,
                       @PathVariable long itemId) {
        itemService.delete(userId, itemId);
    }

    @GetMapping("/{itemId}")
    public ItemDto get(@PathVariable long itemId) {
        return ItemMapper.toItemDto(itemService.get(itemId));
    }

    @GetMapping
    public List<ItemDto> getAllUsersItems(@RequestHeader("X-Sharer-User-Id") long userId) {
        List<Item> items = itemService.getAllUsersItems(userId);
        List<ItemDto> dtoItems = new ArrayList<>();
        for (Item i : items) {
            dtoItems.add(ItemMapper.toItemDto(i));
        }
        return dtoItems;
    }

    @GetMapping("/search")
    public List<ItemDto> search(@RequestParam String text) {
        if (text == null || text.isBlank()) {
            return new ArrayList<>();
        }
        return itemService.search(text.toLowerCase(Locale.ROOT)).stream().map(ItemMapper::toItemDto).collect(Collectors.toList());
    }
}
