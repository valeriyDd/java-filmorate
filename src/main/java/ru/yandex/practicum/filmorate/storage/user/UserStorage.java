package ru.yandex.practicum.filmorate.storage.user;

import ru.yandex.practicum.filmorate.model.User;

import java.util.Collection;
import java.util.List;

public interface UserStorage {
    Collection<User> findAllUsers();

    User add(User user);

    User update(User user);

    User delete(User user);

    User getUser(int id);

    List<Integer> getUserFriendsById(int userId);

    void makeFriends(int userId, int friendId);

    void removeFriends(int userId, int friendId);

    Collection<User> findFriends(int id);

}
