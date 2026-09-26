package com.userservice.application.service;

import com.userservice.application.port.out.WebAnalyticsPort;
import org.junit.jupiter.api.Test;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class WebAnalyticsServiceTest {
    private static final Clock CLOCK = Clock.fixed(Instant.parse("2026-09-26T12:00:00Z"), ZoneOffset.UTC);

    @Test
    void aggregatesTodaySevenDaysAndMonthToDate() {
        WebAnalyticsPort port = new StubAnalytics(true, List.of(
                row("2026-09-01", 5, 9),
                row("2026-09-20", 10, 18),
                row("2026-09-25", 20, 31),
                row("2026-09-26", 7, 12)
        ));

        var summary = new WebAnalyticsService(port, CLOCK).trafficSummary();

        assertTrue(summary.configured());
        assertEquals(7, summary.today().visitors());
        assertEquals(12, summary.today().pageviews());
        assertEquals(37, summary.lastSevenDays().visitors());
        assertEquals(61, summary.lastSevenDays().pageviews());
        assertEquals(42, summary.monthToDate().visitors());
        assertEquals(70, summary.monthToDate().pageviews());
    }

    @Test
    void returnsExplicitUnconfiguredStateWithoutCallingProvider() {
        var summary = new WebAnalyticsService(new StubAnalytics(false, List.of()), CLOCK).trafficSummary();

        assertFalse(summary.configured());
        assertEquals(0, summary.today().visitors());
        assertTrue(summary.daily().isEmpty());
    }

    private static WebAnalyticsPort.DailyTraffic row(String date, long visitors, long pageviews) {
        return new WebAnalyticsPort.DailyTraffic(LocalDate.parse(date), visitors, pageviews);
    }

    private record StubAnalytics(boolean configured, List<DailyTraffic> rows) implements WebAnalyticsPort {
        @Override
        public List<DailyTraffic> dailyTraffic(LocalDate since, LocalDate until) {
            return rows;
        }
    }
}
