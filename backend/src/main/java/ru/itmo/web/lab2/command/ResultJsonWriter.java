package ru.itmo.web.lab2.command;

import ru.itmo.web.lab2.domain.HitResult;
import ru.itmo.web.lab2.http.Json;

import java.time.Instant;
import java.util.List;


public class ResultJsonWriter {

    private final Json json;

    public ResultJsonWriter(Json json) {
        this.json = json;
    }

    public String writeCheckResponse(HitResult result, List<HitResult> history, Instant now) {
        return "{\"result\":" + writeResult(result)
                + ",\"history\":" + writeHistory(history)
                + ",\"currentTime\":" + now.toEpochMilli() + "}";
    }

    private String writeHistory(List<HitResult> history) {
        StringBuilder sb = new StringBuilder("[");
        for (int i = 0; i < history.size(); i++) {
            if (i > 0) {
                sb.append(',');
            }
            sb.append(writeResult(history.get(i)));
        }
        return sb.append(']').toString();
    }

    private String writeResult(HitResult r) {
        return "{\"x\":" + r.point().x().toPlainString()
                + ",\"y\":" + json.quote(r.point().y().toPlainString())
                + ",\"r\":" + r.point().r().toPlainString()
                + ",\"hit\":" + r.hit()
                + ",\"time\":" + r.time().toEpochMilli()
                + ",\"execTimeNs\":" + r.execTimeNs() + "}";
    }
}