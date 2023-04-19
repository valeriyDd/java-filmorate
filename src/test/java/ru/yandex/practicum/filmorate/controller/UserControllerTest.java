
package ru.yandex.practicum.filmorate.controller;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.InvalidDataAccessApiUsageException;
import org.springframework.test.annotation.DirtiesContext;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.friend.FriendsStorage;
import ru.yandex.practicum.filmorate.storage.user.UserDbStorage;

import javax.validation.ConstraintViolation;
import javax.validation.Validation;
import javax.validation.Validator;
import javax.validation.ValidatorFactory;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

@SpringBootTest
@AutoConfigureTestDatabase
@RequiredArgsConstructor(onConstructor_ = @Autowired)
class UserControllerTest {
    private final UserDbStorage userDbStorage;
    private static Validator validator;
    private final FriendsStorage friendsStorage;

    @BeforeEach
    void beforeEach() {
        ValidatorFactory validatorFactory = Validation.buildDefaultValidatorFactory();
        validator = validatorFactory.usingContext().getValidator();
    }


    @Test
    @DirtiesContext
    void createUser() {
        User user = User.builder().email("user@yandex.ru").login("user_login").name("name")
                .birthday(LocalDate.of(2000, 1, 1)).build();
        assertEquals(user, userDbStorage.addUser(user));
    }

    @Test
    @DirtiesContext
    void updateUser() {
        User user = User.builder().email("email@yandex.ru").login("login").name("name")
                .birthday(LocalDate.of(2000, 1, 1)).build();
        assertEquals(user, userDbStorage.addUser(user));
        String newName = "newName";
        user.setName(newName);
        String newEmail = "newEmail@yandex.ru";
        user.setEmail(newEmail);
        User updatedUser = userDbStorage.updateUser(user);
        assertEquals(newName, updatedUser.getName());
        assertEquals(newEmail, updatedUser.getEmail());
    }

    @Test
    @DirtiesContext
    void getAllUsers() {
        int len = userDbStorage.getUsers().size();
        User user = User.builder().email("email@yandex.ru").login("login").name("name")
                .birthday(LocalDate.of(2000, 1, 1)).build();
        User user2 = User.builder().email("email2@yandex.ru").login("login2").name("name2")
                .birthday(LocalDate.of(2001, 2, 2)).build();
        userDbStorage.addUser(user);
        userDbStorage.addUser(user2);
        List<User> users = new ArrayList<>(userDbStorage.getUsers());
        assertEquals(len + 2, users.size());
    }

    @Test
    @DirtiesContext
    void createUserWithWrongEmail() {
        User user = User.builder().email(null).login("login").name("name")
                .birthday(LocalDate.of(2000, 1, 1)).build();
        Set<ConstraintViolation<User>> violations = validator.validate(user);
        assertEquals(1, violations.size(), "email = null");
        user.setEmail("");
        Set<ConstraintViolation<User>> violations2 = validator.validate(user);
        assertEquals(1, violations2.size(), "email without @");
        user.setEmail("emailyandex.ru");
        Set<ConstraintViolation<User>> violations3 = validator.validate(user);
        assertEquals(1, violations3.size(), "Wrong Email");
    }


    @Test
    @DirtiesContext
    void createUserWithWrongLogin() {
        User user = User.builder().email("email@yandex.ru").login(null).name("name")
                .birthday(LocalDate.of(2000, 1, 1)).build();
        Set<ConstraintViolation<User>> violations = validator.validate(user);
        assertEquals(1, violations.size(), "login = null");
        user.setLogin("");
        Set<ConstraintViolation<User>> violations2 = validator.validate(user);
        assertEquals(1, violations2.size(), "empty login");
        user.setLogin("log in");
        Set<ConstraintViolation<User>> violations3 = validator.validate(user);
        assertEquals(1, violations3.size(), "login with space");
    }

    @Test
    @DirtiesContext
    void createUserWithoutName() {
        User user = User.builder().email("email@yandex.ru").login("login").name(null)
                .birthday(LocalDate.of(2000, 1, 1)).build();
        User createdUser = userDbStorage.addUser(user);
        assertEquals(user.getLogin(), createdUser.getName());
        user.setName("");
        createdUser = userDbStorage.updateUser(user);
        assertEquals(user.getLogin(), createdUser.getName());
    }

    @Test
    @DirtiesContext
    void createUserWithWrongBirthdate() {
        LocalDate now = LocalDate.now();
        User user = User.builder().email("email@yandex.ru").login("login").name("name")
                .birthday(null).build();
        Set<ConstraintViolation<User>> violations = validator.validate(user);
        assertEquals(1, violations.size(), "birthday = null");
        user.setBirthday(now.plusDays(1));
        Set<ConstraintViolation<User>> violations2 = validator.validate(user);
        assertEquals(1, violations2.size(), "birthday is tommorow");
        user.setBirthday(now);
        assertEquals(user, userDbStorage.addUser(user));
    }

    @Test
    void updateUserWithWrongId() {
        User user = User.builder().email("email@yandex.ru").login("login").name("name")
                .birthday(LocalDate.of(2000, 1, 1)).build();
        userDbStorage.addUser(user);
        user.setId(999);
        assertThrows(InvalidDataAccessApiUsageException.class, () -> userDbStorage.updateUser(user));
    }

    @Test
    @DirtiesContext
    void addFriend() {
        User user = User.builder().email("email@yandex.ru").login("login").name("name")
                .birthday(LocalDate.of(2000, 1, 1)).build();
        User user2 = User.builder().email("email2@yandex.ru").login("login2").name("name2")
                .birthday(LocalDate.of(2001, 2, 2)).build();
        user = userDbStorage.addUser(user);
        user2 = userDbStorage.addUser(user2);
        friendsStorage.addFriend(user.getId(), user2.getId());
        assertEquals(1, friendsStorage.getFriends(user.getId()).size());
        assertEquals(0, friendsStorage.getFriends(user2.getId()).size());
        friendsStorage.addFriend(user2.getId(), user.getId());
        assertEquals(1, friendsStorage.getFriends(user.getId()).size());
        assertEquals(1, friendsStorage.getFriends(user2.getId()).size());
    }

    @Test
    @DirtiesContext
    void addFriendWithWrongId() {
        User user = User.builder().email("email@yandex.ru").login("login").name("name")
                .birthday(LocalDate.of(2000, 1, 1)).build();
        userDbStorage.addUser(user);
        assertThrows(DataIntegrityViolationException.class, () -> friendsStorage.addFriend(user.getId(), 999));
        assertThrows(DataIntegrityViolationException.class, () -> friendsStorage.addFriend(999, user.getId()));
    }

    @Test
    @DirtiesContext
    void deleteFriend() {
        User user = User.builder().email("email@yandex.ru").login("login").name("name")
                .birthday(LocalDate.of(2000, 1, 1)).build();
        User user2 = User.builder().email("email2@yandex.ru").login("login2").name("name2")
                .birthday(LocalDate.of(2001, 2, 2)).build();
        userDbStorage.addUser(user);
        userDbStorage.addUser(user2);
        friendsStorage.addFriend(user.getId(), user2.getId());
        friendsStorage.addFriend(user2.getId(), user.getId());
        assertEquals(1, friendsStorage.getFriends(user.getId()).size());
        assertEquals(1, friendsStorage.getFriends(user2.getId()).size());
        friendsStorage.deleteFriend(user.getId(), user2.getId());
        friendsStorage.deleteFriend(user2.getId(), user.getId());
        assertEquals(0, friendsStorage.getFriends(user.getId()).size());
        assertEquals(0, friendsStorage.getFriends(user2.getId()).size());
    }


    @Test
    @DirtiesContext
    void getFriends() {
        User user = User.builder().email("email@yandex.ru").login("login").name("name")
                .birthday(LocalDate.of(2000, 1, 1)).build();
        User user2 = User.builder().email("email2@yandex.ru").login("login2").name("name2")
                .birthday(LocalDate.of(2001, 2, 2)).build();
        User user3 = User.builder().email("email3@yandex.ru").login("login3").name("name3")
                .birthday(LocalDate.of(2002, 3, 3)).build();
        userDbStorage.addUser(user);
        userDbStorage.addUser(user2);
        userDbStorage.addUser(user3);
        friendsStorage.addFriend(user.getId(), user2.getId());
        friendsStorage.addFriend(user.getId(), user3.getId());
        friendsStorage.addFriend(user2.getId(), user3.getId());
        friendsStorage.addFriend(user3.getId(), user2.getId());
        assertEquals(2, friendsStorage.getFriends(user.getId()).size());
        assertEquals(1, friendsStorage.getFriends(user2.getId()).size());
        assertEquals(1, friendsStorage.getFriends(user3.getId()).size());
    }


    @Test
    @DirtiesContext
    void getCommonFriends() {
        User user = User.builder().email("email@yandex.ru").login("login").name("name")
                .birthday(LocalDate.of(2000, 1, 1)).build();
        User user2 = User.builder().email("email2@yandex.ru").login("login2").name("name2")
                .birthday(LocalDate.of(2001, 2, 2)).build();
        User user3 = User.builder().email("email3@yandex.ru").login("login3").name("name3")
                .birthday(LocalDate.of(2002, 3, 3)).build();
        User user4 = User.builder().email("email4@yandex.ru").login("login4").name("name4")
                .birthday(LocalDate.of(2002, 3, 3)).build();
        userDbStorage.addUser(user);
        userDbStorage.addUser(user2);
        userDbStorage.addUser(user3);
        userDbStorage.addUser(user4);
        friendsStorage.addFriend(user.getId(), user2.getId());
        friendsStorage.addFriend(user.getId(), user3.getId());
        friendsStorage.addFriend(user4.getId(), user2.getId());
        friendsStorage.addFriend(user4.getId(), user3.getId());
        List<User> commonUsers = friendsStorage.getCommonFriends(user.getId(), user4.getId());
        assertEquals(2, commonUsers.size());
    }


    @Test
    @DirtiesContext
    void getUserById() {
        User user = User.builder().email("email@yandex.ru").login("login").name("name")
                .birthday(LocalDate.of(2000, 1, 1)).build();
        userDbStorage.addUser(user);
        assertEquals(user, userDbStorage.getUser(user.getId()));
    }

    @Test
    @DirtiesContext
    void getUserWithWrongId() {
        assertThrows(InvalidDataAccessApiUsageException.class, () -> userDbStorage.getUser(999));
    }
}
