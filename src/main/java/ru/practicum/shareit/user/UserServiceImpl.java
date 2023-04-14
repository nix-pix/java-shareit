package ru.practicum.shareit.user;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Collection;

@Service
public class UserServiceImpl implements UserService {
    UserRepository userRepositoryImpl;

    @Autowired
    public UserServiceImpl(UserRepositoryImpl userRepositoryImpl) {
        this.userRepositoryImpl = userRepositoryImpl;
    }

    @Override
    public User create(User user) {
        return userRepositoryImpl.create(user);
    }

    @Override
    public User update(User user, long userId) {
        return userRepositoryImpl.update(user, userId);
    }

    @Override
    public void delete(long userId) {
        userRepositoryImpl.delete(userId);
    }

    @Override
    public User get(long userId) {
        return userRepositoryImpl.get(userId);
    }

    @Override
    public Collection<User> getAll() {
        return userRepositoryImpl.getAll();
    }
}
