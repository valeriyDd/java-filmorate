package ru.yandex.practicum.filmorate.storage.user;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.User;

import javax.validation.ValidationException;
import java.util.*;

@Component("inMemoryUserStorage")
@Slf4j
public class InMemoryUserStorage implements UserStorage {
    private int id = 1;
    private final HashMap<Integer, User> saveUserStorage = new HashMap<>();

    @Override
    public User add(User user) {
        if (saveUserStorage.values().stream()
                .noneMatch(saveUser -> saveUser.getLogin().equals(user.getLogin()))) {
            user.setId(id++);
            user.setFriends(new HashSet<>());
            saveUserStorage.put(user.getId(), user);
            log.info("Пользователь '{}' успешно добавлен", user.getLogin());
        }
        return user;
    }

    @Override
    public User update(User user) {
        if (saveUserStorage.containsKey(user.getId())) {
            user.setFriends(new HashSet<>());
            saveUserStorage.put(user.getId(), user);
            log.info("Данные пользователя '{}' успешно обновлены", user.getLogin());
            return user;
        } else {
            log.error("Данные пользователя '{}' небыли изменены", user.getName());
            throw new ValidationException("Ошибка обновления данных пользователя");
        }
    }

    @Override
    public Collection<User> findAllUsers() {
        log.debug("Текущее количество пользователей: '{}'", saveUserStorage.size());
        return new ArrayList<>(saveUserStorage.values());
    }

    @Override
    public User getUser(int id) {
        if (!saveUserStorage.containsKey(id)) {
            log.error(String.format("Пользователь с ИД %d не найден", id));
            throw new NotFoundException(String.format("Пользователь с ИД %d не найден", id));
        }
        log.info(String.format("Пользователь с ИД %d найден", id));
        return saveUserStorage.get(id);
    }

    @Override
    public User delete(User user) {
        if (saveUserStorage.containsKey(user.getId())) {
            log.info(String.format("Пользователь с ИД %d найден", id));
            return saveUserStorage.remove(user.getId());
        } else {
            log.error("Пользователь с ИД {} не найден", id);
            throw new NotFoundException(String.format("Пользователь с ИД %d не найден", id));
        }
    }

    @Override
    public void makeFriends(int userId, int friendId) {
        throw new NotFoundException("Не реализованно");
    }

    @Override
    public void removeFriends(int userId, int friendId) {
        throw new NotFoundException("Не реализованно");
    }

    @Override
    public List<Integer> getUserFriendsById(int userId) {
        throw new NotFoundException("Не реализованно");
    }

    @Override
    public Collection<User> findFriends(int id) {
        throw new NotFoundException("Не реализованно");
    }
}
