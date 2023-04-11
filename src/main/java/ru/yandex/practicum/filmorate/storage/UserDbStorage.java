package ru.yandex.practicum.filmorate.storage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.jdbc.support.rowset.SqlRowSet;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.User;

import java.sql.PreparedStatement;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

@Component
@Qualifier
@Slf4j
public class UserDbStorage implements UserStorage {
    @Autowired
    private final JdbcTemplate jdbcTemplate;
    private final String insertSQL = "INSERT INTO users (email) VALUES(?);";

    public UserDbStorage(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public User createUser(User user) {
        String message = user.getEmail();
        Integer id = insertMessage(message);
        jdbcTemplate.update("UPDATE users SET login = ?, name = ?, birthday = ? WHERE id = ?;",
                user.getLogin(), user.getName(), user.getBirthday().toString(), id);
        SqlRowSet selectUser = jdbcTemplate.queryForRowSet("SELECT * FROM users WHERE id = ?;", id);
        selectUser.next();
        return newUser(selectUser);
    }

    @Override
    public User updateUser(User user) {
        isPresentUser(user.getId());
            jdbcTemplate.update("UPDATE users SET email = ?, login = ?, name = ?, birthday = ? WHERE id = ?;",
                    user.getEmail(), user.getLogin(), user.getName(), user.getBirthday().toString(), user.getId());
        SqlRowSet updateUser = jdbcTemplate.queryForRowSet("SELECT * FROM users WHERE id = ?;", user.getId());
        updateUser.next();
        return newUser(updateUser);
    }

    @Override
    public List<User> getAllUsers() {
        SqlRowSet selectUsers = jdbcTemplate.queryForRowSet("SELECT * FROM users;");
        List<User> allUsers = new ArrayList<>();
        while (selectUsers.next()) {
            allUsers.add(newUser(selectUsers));
        }
        return allUsers;
    }

    @Override
    public void deleteUser(Integer id) {
        isPresentUser(id);
        jdbcTemplate.update("DELETE FROM friend_request WHERE requester = ? OR recipient = ?;", id, id);
        jdbcTemplate.update("DELETE FROM users WHERE id = ?;", id);
    }

    @Override
    public User getUser(Integer id) {
        SqlRowSet selectUser = isPresentUser(id);
        return newUser(selectUser);
    }

    @Override
    public void makeFriends(Integer firstFriendId, Integer secondFriendId) {
        isPresentUser(firstFriendId);
        isPresentUser(secondFriendId);
        SqlRowSet selectAlreadyRequest = jdbcTemplate.queryForRowSet("SELECT * FROM friend_request " +
                "WHERE requester = ?;", secondFriendId);
        if (selectAlreadyRequest.next()) {
            jdbcTemplate.update("UPDATE friend_request SET status = 'true' WHERE  requester = ?;", secondFriendId);
        } else {
            jdbcTemplate.update("INSERT INTO friend_request (requester, recipient, status) VALUES(?, ?, ?)",
                    firstFriendId, secondFriendId, false);
        }
    }

    @Override
    public void stopBeingFriends(Integer firstFriendId, Integer secondFriendId) {
        isPresentUser(firstFriendId);
        isPresentUser(secondFriendId);
        SqlRowSet selectRequestFalse = jdbcTemplate.queryForRowSet(
                "SELECT * FROM friend_request WHERE requester = ? AND recipient = ? AND status = false;",
                firstFriendId, secondFriendId);
        if (selectRequestFalse.next()) {
            jdbcTemplate.update("DELETE FROM friend_request WHERE requester = ? AND recipient = ?;",
                    firstFriendId, secondFriendId);
        }
        SqlRowSet selectRequestTrue = jdbcTemplate.queryForRowSet("SELECT * FROM friend_request WHERE " +
                        "((requester = ? AND recipient = ?) OR (requester = ? AND recipient = ?)) AND status = true;",
                    firstFriendId, secondFriendId, secondFriendId, firstFriendId);
        if (selectRequestTrue.next()) {
            jdbcTemplate.update("DELETE FROM friend_request WHERE ((requester = ? AND recipient = ?) OR " +
                            "(requester = ? AND recipient = ?)) AND status = true;", firstFriendId, secondFriendId,
                    secondFriendId, firstFriendId);
            jdbcTemplate.update("INSERT INTO friend_request (requester, recipient, status) VALUES(?, ?, ?)",
                    secondFriendId, firstFriendId, false);
        }
    }

    @Override
    public List<User> getFriends(Integer id) {
        isPresentUser(id);
        ArrayList<User> friends = new ArrayList<>();
        SqlRowSet selectRecipients = jdbcTemplate.queryForRowSet("SELECT * FROM users WHERE id IN " +
                "(SELECT recipient FROM friend_request WHERE requester = ?);", id);
        while (selectRecipients.next()) {
            friends.add(newUser(selectRecipients));
        }
        SqlRowSet selectRequesters = jdbcTemplate.queryForRowSet("SELECT * FROM users WHERE id IN " +
                "(SELECT requester FROM friend_request WHERE recipient = ? AND status = true);", id);
        while (selectRequesters.next()) {
            friends.add(newUser(selectRequesters));
            }
        return friends;
    }

    private Integer insertMessage(String message) {
        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbcTemplate.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(insertSQL, Statement.RETURN_GENERATED_KEYS);
            ps.setString(1, message);
            return ps;
        }, keyHolder);
        return (Integer) keyHolder.getKey();
    }

    private SqlRowSet isPresentUser(Integer id) {
        SqlRowSet selectUser = jdbcTemplate.queryForRowSet("SELECT * FROM users WHERE id = ?;", id);
        if (selectUser.next()) {
            return selectUser;
        } else {
            throw new NotFoundException();
        }
    }

    private User newUser(SqlRowSet sqlRowSet) {
        return new User(
                sqlRowSet.getInt("id"),
                sqlRowSet.getString("email"),
                sqlRowSet.getString("login"),
                sqlRowSet.getString("name"),
                sqlRowSet.getDate("birthday").toLocalDate());
    }
}
