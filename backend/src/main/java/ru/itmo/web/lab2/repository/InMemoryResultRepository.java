package ru.itmo.web.lab2.repository;

import ru.itmo.web.lab2.domain.HitResult;

import java.util.ArrayList;
import java.util.List;

public class InMemoryResultRepository implements ResultRepository{

    private final List<HitResult> hitResultList = new ArrayList<>();

    @Override
    public void add(HitResult hitResult){
        hitResultList.add(hitResult);
    }

    @Override
    public List<HitResult> findAll(){
        return List.copyOf(hitResultList);
    }
}
