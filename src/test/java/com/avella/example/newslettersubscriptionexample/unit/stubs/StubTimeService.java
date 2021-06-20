package com.avella.example.newslettersubscriptionexample.unit.stubs;

import com.avella.example.newslettersubscriptionexample.application.commands.handlers.TimeService;

import java.time.LocalDateTime;
import java.time.ZoneOffset;

public class StubTimeService implements TimeService {

    private LocalDateTime time;

    public void withFixedTime(LocalDateTime time) {
        this.time = time;
    }

    @Override
    public LocalDateTime getCurrentTime() {
        return time != null ? time : LocalDateTime.now(ZoneOffset.UTC);
    }
}
