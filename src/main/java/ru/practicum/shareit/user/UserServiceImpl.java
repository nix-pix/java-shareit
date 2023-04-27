package ru.practicum.shareit.user;

import org.hibernate.exception.ConstraintViolationException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.exceptions.IncorrectParameterException;
import ru.practicum.shareit.exceptions.ObjectNotFoundException;
import ru.practicum.shareit.exceptions.ParameterException;
import ru.practicum.shareit.user.dto.UserDto;

import java.util.Collection;
import java.util.Optional;

import static java.util.stream.Collectors.toList;

@Service
@Transactional(readOnly = true)
public class UserServiceImpl implements UserService {
    UserRepository userRepository;

    @Autowired
    public UserServiceImpl(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    @Transactional
    public UserDto create(UserDto user) {
        valid(user);
        return UserMapper.toUserDto(userRepository.save(UserMapper.toUser(user)));
    }

    @Override
    @Transactional
    public UserDto update(UserDto userDto, long userId) {
        User user = userRepository.findById(userId).orElseThrow(() -> {
            throw new ObjectNotFoundException("Пользователь с id = " + userId + " не найден.");
        });

        if (userDto.getName() != null) {
            user.setName(userDto.getName());
        }
        if (userDto.getEmail() != null) {
            user.setEmail(userDto.getEmail());
        }

        try {
            return UserMapper.toUserDto(userRepository.save(user));
        } catch (DataIntegrityViolationException ex) {
            if (ex.getCause() instanceof ConstraintViolationException) {
                throw new ParameterException(ex.getMessage());
            }
        }
        return null;
    }

    @Override
    @Transactional
    public void delete(long userId) {
        userRepository.deleteById(userId);
    }

    @Override
    public UserDto get(long userId) {
        Optional<User> user = userRepository.findById(userId);
        if (user.isEmpty()) {
            throw new ObjectNotFoundException("Пользователь с id = " + userId + " не найден");
        }
        return UserMapper.toUserDto(user.get());
    }

    @Override
    public Collection<UserDto> getAll() {
        return userRepository.findAll()
                .stream()
                .map(UserMapper::toUserDto)
                .collect(toList());
    }

    public void valid(UserDto user) {
        if (user == null) {
            throw new IncorrectParameterException("При создании пользователя передан некорректный параметр");
        } else if (user.getEmail() == null) {
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
