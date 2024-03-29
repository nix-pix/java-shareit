package ru.practicum.shareit.user.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotEmpty;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class UserDto {
    private Long id;
    @NotEmpty(groups = {Create.class}, message = "Имя не может быть пустым.")
    private String name;
    @Email(groups = {Update.class, Create.class}, message = "Поле не является адресом электронной почты.")
    @NotEmpty(groups = {Create.class}, message = "Адрес электронной почты не может быть пустым.")
    private String email;
}
