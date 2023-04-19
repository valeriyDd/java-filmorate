package ru.yandex.practicum.filmorate.storage.friend;

import ru.yandex.practicum.filmorate.model.User;

import java.util.List;

public interface FriendsStorage {
    List<User> getCommonFriends(int id1, int id2);

    List<User> getFriends(int id);

    void addFriend(int id1, int id2);

    void deleteFriend(int id1, int id2);
}
