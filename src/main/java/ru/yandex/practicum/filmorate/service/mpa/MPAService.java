package ru.yandex.practicum.filmorate.service.mpa;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.model.Mpa;
import ru.yandex.practicum.filmorate.storage.mpa.MpaStorage;

import java.util.Collection;

@Slf4j
@Service
public class MPAService {

    private final MpaStorage mpaStorage;

    public MPAService(MpaStorage mpaStorage) {
        this.mpaStorage = mpaStorage;
    }

    public Collection<Mpa> getMpaAll() {
        log.info("Рейтинги получены");
        return mpaStorage.getMpaAll();
    }

    public Mpa getMpa(int id) {
        log.info("Рейтинг с id {} получен", id);
        return mpaStorage.getMpa(id);
    }
}