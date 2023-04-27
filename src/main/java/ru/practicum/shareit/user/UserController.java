package ru.practicum.shareit.user;

import lombok.AllArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.user.dto.Create;
import ru.practicum.shareit.user.dto.Update;
import ru.practicum.shareit.user.dto.UserDto;

import javax.validation.constraints.NotNull;
import java.util.Collection;

@RestController
@RequestMapping(path = "/users")
@AllArgsConstructor
public class UserController {
    UserService userService;

    @PostMapping()
    public UserDto create(@RequestBody @Validated(Create.class) UserDto user) {
        return userService.save(user);
    }

    @PatchMapping("/{userId}")
    public UserDto update(@RequestBody @Validated(Update.class) UserDto user,
                          @PathVariable long userId) {
        return userService.update(user, userId);
    }

    @DeleteMapping("/{userId}")
    public void delete(@PathVariable long userId) {
        userService.delete(userId);
    }

    @GetMapping("/{userId}")
    public UserDto get(@PathVariable @NotNull long userId) {
        return userService.get(userId);
    }

    @GetMapping
    public Collection<UserDto> getAll() {
        return userService.getAll();
    }
}
