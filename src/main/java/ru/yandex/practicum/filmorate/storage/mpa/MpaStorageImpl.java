package ru.yandex.practicum.filmorate.storage.mpa;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.rowset.SqlRowSet;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.model.MPA;
import ru.yandex.practicum.filmorate.storage.mpa.MpaStorage;

import java.util.List;

@Repository
public class MpaStorageImpl implements MpaStorage {
    private final JdbcTemplate jdbcTemplate;
    private final RowMapper<MPA> mpaRowMapper = (rs, rowNum) -> {
        MPA mpa = new MPA();
        mpa.setId(rs.getInt("MPA_ID"));
        mpa.setName(rs.getString("NAME"));
        return mpa;
    };

    public MpaStorageImpl(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public List<MPA> getMPAs() {
        return jdbcTemplate.query("SELECT * FROM MPA", mpaRowMapper);
    }

    @Override
    public MPA getMpaById(int id) {
        SqlRowSet mpaRows = jdbcTemplate.queryForRowSet("SELECT * FROM MPA WHERE MPA_ID = ?", id);
        if (mpaRows.next()) {
            return new MPA(mpaRows.getInt("MPA_ID"), mpaRows.getString("NAME"));
        } else {
            throw new IllegalArgumentException("неправильный id");
        }
    }


}
