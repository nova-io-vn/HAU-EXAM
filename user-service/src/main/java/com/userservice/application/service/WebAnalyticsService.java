package com.userservice.application.service;

import com.userservice.application.dto.WebTrafficSummary;
import com.userservice.application.dto.WebTrafficSummary.TrafficCount;
import com.userservice.application.port.out.WebAnalyticsPort;
import com.userservice.application.port.out.WebAnalyticsPort.DailyTraffic;
import org.springframework.stereotype.Service;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;

@Service
public class WebAnalyticsService {
    private final WebAnalyticsPort analytics;
    private final Clock clock;

    public WebAnalyticsService(WebAnalyticsPort analytics, Clock clock) {
        this.analytics = analytics;
        this.clock = clock;
    }

    public WebTrafficSummary trafficSummary() {
        Instant generatedAt = Instant.now(clock);
        if (!analytics.configured()) {
            TrafficCount empty = new TrafficCount(0, 0);
            return new WebTrafficSummary(false, empty, empty, empty, List.of(), generatedAt);
        }

        LocalDate today = LocalDate.now(clock);
        LocalDate weekStart = today.minusDays(6);
        LocalDate monthStart = today.withDayOfMonth(1);
        LocalDate queryStart = weekStart.isBefore(monthStart) ? weekStart : monthStart;
        List<DailyTraffic> daily = analytics.dailyTraffic(queryStart, today).stream()
                .sorted(java.util.Comparator.comparing(DailyTraffic::date))
                .toList();

        return new WebTrafficSummary(
                true,
                sum(daily, today),
                sumFrom(daily, weekStart),
                sumFrom(daily, monthStart),
                daily,
                generatedAt
        );
    }

    private TrafficCount sum(List<DailyTraffic> rows, LocalDate date) {
        return totals(rows.stream().filter(row -> row.date().equals(date)).toList());
    }

    private TrafficCount sumFrom(List<DailyTraffic> rows, LocalDate start) {
        return totals(rows.stream().filter(row -> !row.date().isBefore(start)).toList());
    }

    private TrafficCount totals(List<DailyTraffic> rows) {
        return new TrafficCount(
                rows.stream().mapToLong(DailyTraffic::visitors).sum(),
                rows.stream().mapToLong(DailyTraffic::pageviews).sum()
        );
    }
}
