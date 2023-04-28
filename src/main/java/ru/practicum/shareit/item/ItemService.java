package ru.practicum.shareit.item;

import ru.practicum.shareit.item.dto.CommentDto;
import ru.practicum.shareit.item.dto.ItemAllDto;
import ru.practicum.shareit.item.dto.ItemDto;

import java.util.List;

public interface ItemService {

    ItemAllDto get(Long id, Long userId);

    ItemDto save(ItemDto item, Long userId);

    ItemDto update(ItemDto item, Long id, Long userId);

    List<ItemAllDto> getAll(Long id);

    List<ItemDto> search(String text);

    CommentDto createComment(CommentDto comment, Long itemId, Long userId);

    List<CommentDto> getAllComments();
}
