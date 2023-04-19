package ru.yandex.practicum.filmorate.storage.user;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.model.User;

import javax.validation.ValidationException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@Component
public class InMemoryUserStorage implements UserStorage {

    private final Map<Integer, User> users = new HashMap<>();
    private static Integer generatorUserId = 0;

    public Collection<User> getUsers() {
        return users.values();
    }

    private Integer getNextId() {
        return ++generatorUserId;
    }


    @Override
    public User getUser(int id) {
        return users.get(id);
    }

    @Override
    public User addUser(User user) {
        user.setId(getNextId());
        if (user.getName() == null || user.getName().isBlank()) {
            user.setName(user.getLogin());
        }
        if (user.getFriends() == null) {
            user.setFriends(new ArrayList<>());
        }
        users.put(user.getId(), user);
        log.debug("Пользователь добавлен {}", user);
        return user;
    }

    @Override
    public User updateUser(User user) {
        if (!users.containsKey(user.getId()) || user.getId() == null) {
            throw new ValidationException("Ошибка обновления! Такого пользователя не существует");
        }
        if (user.getFriends() == null) {
            user.setFriends(new ArrayList<>());
        }
        if (user.getName().isBlank() || user.getName() == null) {
            user.setName(user.getLogin());
        }
        users.put(user.getId(), user);
        log.debug("пользователь обновлен {}", user);
        return user;
    }

    @Override
    public User removeUser(User user) {
        if (users.containsKey(user.getId())) {
            User deletedUser = users.remove(user.getId());
            log.debug("пользователь удален {}", user);
            return deletedUser;
        } else {
            throw new ValidationException("Ошибка! Такого пользователя не существует");
        }
    }
}
