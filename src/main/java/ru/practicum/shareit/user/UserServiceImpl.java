package ru.practicum.shareit.user;

import lombok.extern.slf4j.Slf4j;
import org.hibernate.exception.ConstraintViolationException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.exceptions.IncorrectParameterException;
import ru.practicum.shareit.exceptions.ObjectNotFoundException;
import ru.practicum.shareit.exceptions.ParameterException;
import ru.practicum.shareit.user.dto.UserDto;

import java.util.List;
import java.util.Optional;

import static java.util.stream.Collectors.toList;

@Slf4j
@Service
@Transactional(readOnly = true)
public class UserServiceImpl implements UserService {
    private final UserRepository userStorage;

    @Autowired
    public UserServiceImpl(UserRepository userStorage) {
        this.userStorage = userStorage;
    }


    @Override
    @Transactional
    public UserDto save(UserDto userDto) {
        valid(userDto);
        return UserMapper.toUserDto(userStorage.save(UserMapper.toUser(userDto)));
    }

    @Override
    @Transactional
    public UserDto update(UserDto userDto, Long id) {
       User user = userStorage.findById(id).orElseThrow(() -> {
           throw new ObjectNotFoundException("Пользователь с id = " + id + " не найден.");
       });

       if (userDto.getName() != null) {
           user.setName(userDto.getName());
       }
       if (userDto.getEmail() != null) {
           user.setEmail(userDto.getEmail());
       }

       try {
           return UserMapper.toUserDto(userStorage.save(user));
       } catch (DataIntegrityViolationException ex) {
           if (ex.getCause() instanceof ConstraintViolationException) {
               throw new ParameterException(ex.getMessage());
           }
       }
       return null;
    }

    @Override
    public UserDto get(Long id) {
        Optional<User> user = userStorage.findById(id);
        if (user.isEmpty()) {
            throw new ObjectNotFoundException("Пользователь с id = " + id + " не найден");
        }
        return UserMapper.toUserDto(user.get());
    }

    @Override
    @Transactional
    public void delete(Long id) {
        userStorage.deleteById(id);
    }

    @Override
    public List<UserDto> getAll() {
        return userStorage.findAll()
                .stream()
                .map(UserMapper::toUserDto)
                .collect(toList());
    }


    public void valid(UserDto user) {
        if (user == null) {
            throw new IncorrectParameterException("При создании пользователя передан некорреткный параметр");
        }  else if (user.getEmail() == null) {
            throw new IncorrectParameterException("Email не может быть пустым");
        } else if (!isValidEmailAddress(user.getEmail())) {
            throw new IncorrectParameterException("Неверно задан email");
        }
    }

    public boolean isValidEmailAddress(String email) {
        String ePattern = "(?:[A-Za-z0-9!#$%&'*+/=?.^_`{|}~]" +
                "+(?:.[a-z0-9!#$%&'*+/=?^_`{|}~]+)*|" +
                "\\\"(?:[x01-x08x0bx0cx0e-x1fx21x23-x5bx5d-x7f]|[x01-x09x0bx0cx0e-x7f])*\\\")" +
                "@(?:(?:[a-z0-9](?:[a-z0-9-]*[a-z0-9])?.)" +
                "+[a-z0-9](?:[a-z0-9-]*[a-z0-9])?|" +
                "[(?:(?:(2(5[0-5]|[0-4][0-9])|1[0-9][0-9]|[1-9]?[0-9])).){3}(?:(2(5[0-5]|" +
                "[0-4][0-9])|1[0-9][0-9]|[1-9]?[0-9])|[a-z0-9-]*[a-z0-9]:(?:[x01-x08x0bx0cx0e-x1fx21-x5ax53-x7f]" +
                "|[x01-x09x0bx0cx0e-x7f])+)])";
        java.util.regex.Pattern p = java.util.regex.Pattern.compile(ePattern);
        java.util.regex.Matcher m = p.matcher(email);
        return m.matches();
    }

}
