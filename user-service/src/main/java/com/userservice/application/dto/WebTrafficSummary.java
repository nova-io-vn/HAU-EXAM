package com.userservice.application.dto;

import com.userservice.application.port.out.WebAnalyticsPort.DailyTraffic;

import java.time.Instant;
import java.util.List;

public record WebTrafficSummary(
        boolean configured,
        TrafficCount today,
        TrafficCount lastSevenDays,
        TrafficCount monthToDate,
        List<DailyTraffic> daily,
        Instant generatedAt
) {
    public record TrafficCount(long visitors, long pageviews) { }
}
