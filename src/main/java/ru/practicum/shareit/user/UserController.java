package ru.practicum.shareit.user;

import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.Collection;
import java.util.stream.Collectors;

@RestController
@RequestMapping(path = "/users")
@AllArgsConstructor
public class UserController {
    UserService userService;

    @PostMapping
    public UserDto create(@Valid @RequestBody UserDto userDto) {
        return UserMapper.toUserDto(userService.create(UserMapper.toUser(userDto)));
    }

    @PatchMapping("/{userId}")
    public UserDto update(@RequestBody UserDto userDto,
                          @PathVariable long userId) {
        return UserMapper.toUserDto(userService.update(UserMapper.toUser(userDto), userId));
    }

    @DeleteMapping("/{userId}")
    public void delete(@PathVariable long userId) {
        userService.delete(userId);
    }

    @GetMapping("/{userId}")
    public UserDto get(@PathVariable long userId) {
        return UserMapper.toUserDto(userService.get(userId));
    }

    @GetMapping
    public Collection<UserDto> getAll() {
        return userService.getAll().stream()
                .map(UserMapper::toUserDto)
                .collect(Collectors.toList());
    }
}
