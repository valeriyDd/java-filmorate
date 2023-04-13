# java-filmorate

![alt text](https://github.com/winmord/java-filmorate/blob/main/repo_scheme.png)

### Основные операции:

- Получить всех пользователей
  ```sql
    SELECT *
    FROM user
    WHERE user.deleted_at IS NULL;
  ```
- Получить пользователя по id
  ```sql
    SELECT *
    FROM user
    WHERE user.user_id = target_id
      AND user.deleted_at IS NULL;
  ```
- Получить всех друзей пользователя (подтверждённых и неподтверждённых)
  ```sql
    SELECT *
    FROM user
    WHERE user.user_id IN (SELECT friend_id
                           FROM friendship
                           WHERE friendship.user_id = target_id
                             AND friendship.deleted_at IS NULL)
      AND user.deleted_at IS NULL;
  ```
- Получить только подтверждённых друзей пользователя
  ```sql
    SELECT *
    FROM user
    WHERE user.user_id IN (SELECT friend_id
                           FROM friendship
                           WHERE friendship.user_id = target_id
                             AND friendship.confirmed_at IS NOT NULL
                             AND friendship.deleted_at IS NULL)
      AND user.deleted_at IS NULL;
  ```
- Получить все фильмы
  ```sql
    SELECT *
    FROM film
    WHERE film.deleted_at IS NULL;
  ```
- Получить фильм по id
  ```sql
    SELECT *
    FROM film
    WHERE film.film_id = target_id
      AND user.deleted_at IS NULL;
  ```
- Получить Топ-10 фильмов по количеству лайков
  ```sql
    SELECT *
    FROM film
        LEFT JOIN (SELECT film_id, count(film_like.film_id) AS count
          FROM film_like
          WHERE film_like.deleted_at IS NULL
          GROUP BY film_like.film_id) AS top ON top.film_id = film.film_id
             INNER JOIN mpa_rating ON film.mpa_rating_id = mpa_rating.mpa_rating_id
    WHERE film.deleted_at IS NULL
    ORDER BY count DESC
    LIMIT 10;
  ```