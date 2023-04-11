package ru.yandex.practicum.filmorate.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.UserStorage;

import java.util.Collection;
import java.util.List;

@Service
public class UserService {
    @Autowired
    private final UserStorage userStorage;

    public UserService(UserStorage userStorage) {
        this.userStorage = userStorage;
    }

    public void makeFriends(Integer firstFriendId, Integer secondFriendId) {
        userStorage.makeFriends(firstFriendId, secondFriendId);
    }

    public void stopBeingFriends(Integer firstFriendId, Integer secondFriendId) {
        userStorage.stopBeingFriends(firstFriendId, secondFriendId);
    }

    public List<User> getFriends(Integer id) {
        return userStorage.getFriends(id);
    }

    public User getUser(Integer id) {
        return userStorage.getUser(id);
    }

    public User createUser(User user) {
        String name = validateName(user.getName(), user.getLogin());
        user.setName(name);
        return userStorage.createUser(user);
    }

    public User updateUser(User user) {
        String name = validateName(user.getName(), user.getLogin());
        user.setName(name);
        return userStorage.updateUser(user);
    }

    public Collection<User> getAllUsers() {
        return userStorage.getAllUsers();
    }

    public void deleteUser(Integer id) {
        userStorage.deleteUser(id);
    }

    public List<User> getCommonFriends(Integer id, Integer otherId) {
        List<User> commonFriends = getFriends(id);
        List<User> otherFriends = getFriends(otherId);
        commonFriends.retainAll(otherFriends);
        return commonFriends;
    }

    private String validateName(String name, String login) {
        if (name == null || name.isBlank()) {
            name = login;
        }
        return name;
    }
}
