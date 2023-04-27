package ru.practicum.shareit.user;

import ru.practicum.shareit.user.dto.UserDto;

import java.util.Collection;

public interface UserService {

    UserDto create(UserDto user);

    UserDto update(UserDto user, long userId);

    void delete(long userId);

    UserDto get(long userId);

    Collection<UserDto> getAll();
}
