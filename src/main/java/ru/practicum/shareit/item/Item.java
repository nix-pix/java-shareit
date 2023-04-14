package ru.practicum.shareit.item;

import lombok.Data;

@Data
public class Item {
    private Long id;
    private String name;
    private String description;
    private Boolean isAvailable;
    private Long ownerId;
    private Long requestId;
}
