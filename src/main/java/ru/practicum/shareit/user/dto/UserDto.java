package ru.practicum.shareit.user.dto;

import lombok.Builder;
import lombok.Data;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotEmpty;

@Data
@Builder
public class UserDto {
    private Long id;
    @NotEmpty(groups = {Create.class}, message = "Адрес электронной почты не может быть пустым.")
    private String name;
    @Email(groups = {Update.class, Create.class}, message = "Название не может быть пустым.")
    @NotEmpty(groups = {Create.class}, message = "Адрес электронной почты не может быть пустым.")
    private String email;

//    public UserDto(Long id, String name, String email) {
//        this.id = id;
//        this.name = name;
//        this.email = email;
//    }
}
