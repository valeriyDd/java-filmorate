package ru.yandex.practicum.filmorate.storage.user;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Primary;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.simple.SimpleJdbcInsert;
import org.springframework.jdbc.support.rowset.SqlRowSet;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.User;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.Collection;
import java.util.List;


@Slf4j
@Primary
@Component("userDbStorage")
public class UserDbStorage implements UserStorage {
    private final JdbcTemplate jdbcTemplate;

    public UserDbStorage(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public User add(User user) {
        SimpleJdbcInsert simpleJdbcInsert = new SimpleJdbcInsert(jdbcTemplate)
                .withTableName("USERS")
                .usingGeneratedKeyColumns("user_id");
        user.setId(simpleJdbcInsert.executeAndReturnKey(user.toMap()).intValue());
        log.info("Пользователь добавлен: {}", user);
        return user;
    }

    @Override
    public User update(User user) {
        if (isUserExists(user.getId())) {
            String sqlQuery = "UPDATE USERS SET " +
                    "email = ?, login = ?, name = ?, birthday = ? " +
                    "WHERE user_id = ?";
            jdbcTemplate.update(sqlQuery,
                    user.getEmail(),
                    user.getLogin(),
                    user.getName(),
                    user.getBirthday(),
                    user.getId());
            log.info("Пользователь {} обновлен", user);
            return user;
        } else {
            log.error("Пользователь {} не найден", user);
            throw new NotFoundException("Пользователь не найден");
        }
    }

    @Override
    public Collection<User> findAllUsers() {
        log.info("Получение списка пользователей");
        String sql = "SELECT * FROM USERS ";
        return jdbcTemplate.query(sql, (rs, rowNum) -> new User(
                rs.getInt("user_id"),
                rs.getString("email"),
                rs.getString("login"),
                rs.getString("name"),
                rs.getDate("birthday").toLocalDate())
        );
    }

    @Override
    public User delete(User user) {
        if (isUserExists(user.getId())) {
            log.info("Удаление пользователя {} ", user);
            String sql = "DELETE FROM USERS WHERE user_id = ?";
            jdbcTemplate.update(sql, user.getId());
            return user;
        } else {
            log.error("Пользователь {} не найден", user);
            throw new NotFoundException("Пользователь не найден");
        }
    }

    @Override
    public User getUser(int id) throws NotFoundException {
        log.info("Получение пользователя с id {} ", id);
        String sqlQuery =
                "SELECT u.user_id, " +
                        "u.email, " +
                        "u.login, " +
                        "u.name, " +
                        "u.birthday, " +
                        "FROM USERS AS u " +
                        "WHERE u.user_id = ?;";
        return jdbcTemplate.query(sqlQuery, (rs, rowNum) -> makeUser(rs), id)
                .stream()
                .findAny()
                .orElseThrow(() -> new NotFoundException("Пользователь с id=" + id + " не существует"));
    }

    public boolean isUserExists(int id) {
        log.info("Проверка пользователя");
        String sql = "SELECT * FROM USERS WHERE user_id = ?";
        SqlRowSet userRows = jdbcTemplate.queryForRowSet(sql, id);
        return userRows.first();
    }

    @Override
    public List<Integer> getUserFriendsById(int userId) throws NotFoundException {
        log.info("Получение списка друзей для пользователя с id {}", userId);
        String sqlQuery =
                "SELECT fr.friend_id, " +
                        "FROM friendships AS fr " +
                        "WHERE fr.user_id = ?;";
        return jdbcTemplate.queryForList(sqlQuery, Integer.class, userId);
    }

    @Override
    public void makeFriends(int userId, int friendId) {
        log.info("Добавление в друзья user c id {}", userId);
        String sqlQuery = "INSERT INTO friendships (user_id, friend_id) VALUES (?, ?);";
        jdbcTemplate.update(sqlQuery, userId, friendId);
    }

    @Override
    public void removeFriends(int userId, int friendId) {
        String sqlQuery = "DELETE FROM friendships WHERE user_id = ? AND friend_id = ?;";
        jdbcTemplate.update(sqlQuery, userId, friendId);
        log.info("Удаление из друзей пользователя с id {}", userId);
    }

    private User makeUser(ResultSet rs) throws SQLException {
        int id = rs.getInt("user_id");
        String email = rs.getString("email");
        String login = rs.getString("login");
        String name = rs.getString("name");
        LocalDate birthday = rs.getDate("birthday").toLocalDate();
        log.info("Создание пользователя {}", login);
        return new User(id, email, login, name, birthday);
    }

    @Override
    public Collection<User> findFriends(int id) {
        log.info("Поиск друзей");
        String sql = "SELECT FRIEND_ID, EMAIL, LOGIN, NAME, BIRTHDAY FROM FRIENDSHIPS JOIN USERS U " +
                "on FRIENDSHIPS.FRIEND_ID = U.USER_ID WHERE " +
                "FRIENDSHIPS.USER_ID = ?";
        return jdbcTemplate.query(sql, (rs, rowNum) -> new User(
                        rs.getInt("friend_id"),
                        rs.getString("email"),
                        rs.getString("login"),
                        rs.getString("name"),
                        rs.getDate("birthday").toLocalDate()),
                id
        );
    }
}