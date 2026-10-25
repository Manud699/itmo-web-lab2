package ru.itmo.web.lab2.repository;

import ru.itmo.web.lab2.domain.HitResult;

import java.util.List;

public interface ResultRepository {

    void add(HitResult hitResult);
    List<HitResult> findAll();
}
