package ru.yandex.practicum.filmorate.storage;

import ru.yandex.practicum.filmorate.model.User;

import java.util.Collection;
import java.util.List;

public interface UserStorage {

    User createUser(User user);

    User updateUser(User user);

    void deleteUser(Integer id);

    Collection<User> getAllUsers();

    User getUser(Integer id);

    void makeFriends(Integer firstFriendId, Integer secondFriendId);

    void stopBeingFriends(Integer firstFriendId, Integer secondFriendId);

    List<User> getFriends(Integer id);
}
