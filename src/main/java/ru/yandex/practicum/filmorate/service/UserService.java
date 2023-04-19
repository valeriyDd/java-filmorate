package ru.yandex.practicum.filmorate.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.friend.FriendsStorage;
import ru.yandex.practicum.filmorate.storage.user.UserStorage;

import java.util.Collection;
import java.util.List;

@Service
public class UserService {

    UserStorage userStorage;
    FriendsStorage friendsStorage;

    @Autowired
    public UserService(@Qualifier("userDbStorage") UserStorage userStorage, FriendsStorage friendsStorage) {
        this.friendsStorage = friendsStorage;
        this.userStorage = userStorage;
    }

    public Collection<User> getAllUsers() {
        return userStorage.getUsers();
    }

    public User createUser(User user) {
        return userStorage.addUser(user);
    }

    public User updateUser(User user) {
        return userStorage.updateUser(user);
    }

    public User getUser(int id) {
        if (userStorage.getUser(id) == null) {
            throw new IllegalArgumentException("неправильный id");
        }
        return userStorage.getUser(id);
    }

    public List<User> getFriends(int id) {
        return friendsStorage.getFriends(id);
    }

    public void addFriend(int id1, int id2) {
        friendsStorage.addFriend(id1, id2);
    }

    public void deleteFriend(int id1, int id2) {
        friendsStorage.deleteFriend(id1, id2);
    }

    public List<User> getCommonFriends(int id1, int id2) {
        return friendsStorage.getCommonFriends(id1, id2);
    }
}
