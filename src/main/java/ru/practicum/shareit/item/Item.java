package ru.practicum.shareit.item;

import lombok.Data;
import ru.practicum.shareit.request.ItemRequest;

@Data
public class Item {
    private Long id;
    private String name;
    private String description;
    private Boolean availability;
    private Long ownerId;
    private ItemRequest request;
}
