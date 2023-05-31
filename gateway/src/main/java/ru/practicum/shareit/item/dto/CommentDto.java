package ru.practicum.shareit.item.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import javax.validation.constraints.NotEmpty;
import java.time.LocalDateTime;

@Data
@Builder
@AllArgsConstructor
public class CommentDto {
    private final Long id;
    @NotEmpty(message = "Text cannot be null")
    private final String text;
    private final Long itemId;
    private final String authorName;
    private final LocalDateTime created;
}
