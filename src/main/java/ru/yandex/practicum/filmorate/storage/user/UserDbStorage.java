package ru.yandex.practicum.filmorate.storage.user;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.simple.SimpleJdbcInsert;
import org.springframework.jdbc.support.rowset.SqlRowSet;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.friend.FriendsStorage;

import javax.sql.DataSource;
import java.time.LocalDate;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

@Repository
public class UserDbStorage implements UserStorage {
    private final JdbcTemplate jdbcTemplate;
    private final SimpleJdbcInsert insertUser;
    private final FriendsStorage friendsStorage;

    public UserDbStorage(JdbcTemplate jdbcTemplate, DataSource dataSource, FriendsStorage friendsStorage) {
        this.insertUser = new SimpleJdbcInsert(dataSource).withTableName("USERS")
                .usingGeneratedKeyColumns("USER_ID");
        this.jdbcTemplate = jdbcTemplate;
        this.friendsStorage = friendsStorage;
    }

    @Override
    public Collection<User> getUsers() {
        return jdbcTemplate.query("SELECT * FROM USERS", (rs, rowNum) -> {
            User u = new User();
            u.setId(rs.getInt("USER_ID"));
            u.setLogin(rs.getString("LOGIN"));
            u.setName(rs.getString("NAME"));
            u.setEmail(rs.getString("EMAIL"));
            u.setBirthday(LocalDate.parse(rs.getString("BIRTHDAY")));
            u.setFriends(friendsStorage.getFriends(u.getId()));
            return u;
        });
    }

    @Override
    public User addUser(User user) {
        Map<String, Object> parameters = new HashMap<>(4);
        parameters.put("EMAIL", user.getEmail());
        parameters.put("LOGIN", user.getLogin());
        if (user.getName() == null || user.getName().isBlank()) {
            user.setName(user.getLogin());
        }
        parameters.put("NAME", user.getName());
        parameters.put("BIRTHDAY", user.getBirthday());
        Number newId = insertUser.executeAndReturnKey(parameters);
        user.setId(newId.intValue());
        user.setFriends(friendsStorage.getFriends(user.getId()));
        return user;
    }

    @Override
    public User updateUser(User user) {
        if (user.getName() == null || user.getName().isBlank()) {
            user.setName(user.getLogin());
        }
        int status = jdbcTemplate.update("UPDATE USERS SET EMAIL = ?, LOGIN = ?, NAME = ?, BIRTHDAY = ? " +
                        "WHERE USER_ID = ?",
                user.getEmail(), user.getLogin(), user.getName(), user.getBirthday(), user.getId());
        user.setFriends(friendsStorage.getFriends(user.getId()));
        if (status != 1) {
            throw new IllegalArgumentException("неправильный id");
        }
        return user;
    }

    @Override
    public User removeUser(User user) {
        jdbcTemplate.update("DELETE FROM USERS WHERE USER_ID = ?", user.getId());
        return user;
    }

    @Override
    public User getUser(int id) {
        SqlRowSet userRows = jdbcTemplate.queryForRowSet("SELECT * FROM USERS WHERE USER_ID = ?", id);
        if (userRows.next()) {
            User u = new User();
            u.setId(userRows.getInt("USER_ID"));
            u.setLogin(userRows.getString("LOGIN"));
            u.setName(userRows.getString("NAME"));
            u.setEmail(userRows.getString("EMAIL"));
            u.setBirthday(Objects.requireNonNull(userRows.getDate("BIRTHDAY")).toLocalDate());
            u.setFriends(friendsStorage.getFriends(u.getId()));
            return u;
        } else {
            throw new IllegalArgumentException("неправильный id");
        }
    }
}
