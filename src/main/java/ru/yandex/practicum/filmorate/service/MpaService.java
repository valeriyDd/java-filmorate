package ru.yandex.practicum.filmorate.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.model.Mpa;
import ru.yandex.practicum.filmorate.storage.genre.GenreStorage;
import ru.yandex.practicum.filmorate.storage.mpa.MpaStorage;

import java.util.List;

@Service
public class MpaService {
    private final MpaStorage mpas;

    @Autowired
    public MpaService(MpaStorage mpas) {
        this.mpas = mpas;
    }

    public Mpa getMpa(Integer id) {
        return mpas.getMpa(id);
    }

    public List<Mpa> getMpasList() {
        return mpas.getMpasList();
    }
}
