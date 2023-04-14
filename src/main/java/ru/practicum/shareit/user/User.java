package ru.practicum.shareit.user;

import lombok.Data;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotEmpty;

@Data
public class User {
    private Long id;
    private String name;
    @NotEmpty
    @Email
    private String email;
}
