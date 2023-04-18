package ru.yandex.practicum.filmorate.controllers;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.yandex.practicum.filmorate.model.Mpa;
import ru.yandex.practicum.filmorate.service.mpa.MPAService;

import java.util.Collection;

@RestController
@Slf4j
@RequestMapping("/mpa")
public class MPAController {
    private final MPAService mpaService;

    @Autowired
    public MPAController(MPAService mpaService) {
        this.mpaService = mpaService;
    }

    @GetMapping
    public Collection<Mpa> findAll() {
        log.info("Получение рейтинга фильма");
        return mpaService.getMpaAll();
    }

    @GetMapping("/{id}")
    public Mpa getGenre(@PathVariable int id) {
        log.info("Получение рейтинга фильма с id {}", id);
        return mpaService.getMpa(id);
    }
}
