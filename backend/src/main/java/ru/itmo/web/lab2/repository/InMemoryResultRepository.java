package ru.itmo.web.lab2.repository;

import ru.itmo.web.lab2.domain.HitResult;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class InMemoryResultRepository implements ResultRepository{

    private final Map<String, List<HitResult>> historyBySession = new HashMap<>();

    @Override
    public void add(String sessionId, HitResult hitResult){
        historyBySession.computeIfAbsent(sessionId, id -> new ArrayList<>()).add(hitResult);
    }

    @Override
    public List<HitResult> findAll(String sessionId){
        return List.copyOf(historyBySession.getOrDefault(sessionId, List.of()));
    }
}
