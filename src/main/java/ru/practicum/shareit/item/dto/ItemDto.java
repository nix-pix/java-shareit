package ru.practicum.shareit.item.dto;

import lombok.Builder;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

@Data
@Builder
//@AllArgsConstructor
//@NoArgsConstructor
public class ItemDto {
    private Long id;
    //    @NotNull
    @NotBlank
    private String name;
    //    @NotNull
    @NotBlank
    private String description;
    @NotNull
    private Boolean available;
    private Long requestId;
}
