package com.userservice.application.port.out;

import java.time.LocalDate;
import java.util.List;

public interface WebAnalyticsPort {
    boolean configured();

    List<DailyTraffic> dailyTraffic(LocalDate since, LocalDate until);

    record DailyTraffic(LocalDate date, long visitors, long pageviews) { }
}
