package ru.yandex.practicum.filmorate.storage.mpa;

import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.rowset.SqlRowSet;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Mpa;

import java.util.Collection;

@Component
@Slf4j
public class MpaDbStorage implements MpaStorage {
    JdbcTemplate jdbcTemplate;

    public MpaDbStorage(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public Collection<Mpa> getMpaAll() {
        log.info("Рейтинги получены");
        return jdbcTemplate.query("SELECT * FROM RATES_MPA",
                ((rs, rowNum) -> new Mpa(
                        rs.getInt("mpa_id"),
                        rs.getString("mpa_name"))
                ));
    }

    @Override
    public Mpa getMpa(int id) {
        SqlRowSet userRows =
                jdbcTemplate.queryForRowSet("SELECT MPA_NAME FROM RATES_MPA WHERE MPA_ID = ?", id);
        if (userRows.next()) {
            Mpa mpa = new Mpa(
                    id,
                    userRows.getString("mpa_name")
            );
            log.info("Получение рейтинга {} ", mpa);
            return mpa;
        } else throw new NotFoundException("Не найдено рейтинга для этого id");
    }
}
