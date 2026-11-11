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

    @Override
    public void clear(String sessionId){
        historyBySession.remove(sessionId);
    }

    @Override
    public void moveHistory(String fromSessionId, String toSessionId) {
        List<HitResult> history = historyBySession.remove(fromSessionId);
        if (history != null) {
            historyBySession.put(toSessionId, history);
        }
    }

    @Override
    public List<HitResult> findAllSessions() {
        List<HitResult> all = new ArrayList<>();
        for (List<HitResult> history : historyBySession.values()) {
            all.addAll(history);
        }
        return all;
    }
}
