package ru.practicum.shareit.user;

import org.hibernate.exception.ConstraintViolationException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.exception.IncorrectParameterException;
import ru.practicum.shareit.exception.ObjectNotFoundException;
import ru.practicum.shareit.exception.ParameterException;
import ru.practicum.shareit.user.dto.UserDto;

import java.util.List;

import static java.util.stream.Collectors.toList;

@Service
@Transactional(readOnly = true)
public class UserServiceImpl implements UserService {
    private final UserRepository userRepository;

    @Autowired
    public UserServiceImpl(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    @Transactional
    public UserDto save(UserDto userDto) {
        return UserMapper.toUserDto(userRepository.save(UserMapper.toUser(userDto)));
    }

    @Override
    @Transactional
    public UserDto update(UserDto userDto, Long id) {
        User user = userRepository.findById(id).orElseThrow(() -> {
            throw new ObjectNotFoundException("Пользователь с id = " + id + " не найден.");
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
    public UserDto get(Long id) {
        if (id == null) {
            throw new IncorrectParameterException("Id пользователя не может быть null");
        }
        User user = userRepository.findById(id).orElseThrow(() -> {
            throw new ObjectNotFoundException("Пользователь с id = " + id + " не найден");
        });
        return UserMapper.toUserDto(user);
    }

    @Override
    @Transactional
    public void delete(Long id) {
        if (id == null) {
            throw new IncorrectParameterException("Id не может быть пустым!");
        }
        userRepository.deleteById(id);
    }

    @Override
    public List<UserDto> getAll() {
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
        }
    }
}
