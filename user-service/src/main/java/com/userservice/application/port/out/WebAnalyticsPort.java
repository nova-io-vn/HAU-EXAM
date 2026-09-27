package com.userservice.application.port.out;

import java.time.LocalDate;
import java.util.List;

public interface WebAnalyticsPort {
    boolean configured();

    List<DailyTraffic> dailyTraffic(LocalDate since, LocalDate until);

    default String testConnection() { return configured() ? "WORKING" : "NOT_CONFIGURED"; }

    record DailyTraffic(LocalDate date, long visitors, long pageviews) { }
}
