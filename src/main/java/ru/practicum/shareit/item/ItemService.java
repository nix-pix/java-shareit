package ru.practicum.shareit.item;

import java.util.List;

public interface ItemService {

    ItemAllDto get(Long id, Long userId);

    ItemDto save(ItemDto item, Long userId);

    ItemDto update(ItemDto item, Long id, Long userId);

    List<ItemAllDto> getAll(Long id);

    List<ItemDto> getByText(String text);

    CommentDto createComment(CommentDto comment, Long itemId, Long userId);

    List<CommentDto> getAllComments();
}
