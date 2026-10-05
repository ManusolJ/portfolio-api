package com.portfolio.status;

import org.springframework.stereotype.Service;

import com.portfolio.monitor.MonitorProperties;

import java.time.Clock;
import java.time.Duration;
import java.time.LocalDate;
import java.time.ZoneOffset;

import java.util.Map;
import java.util.List;
import java.util.HashMap;
import java.util.ArrayList;

/** Composes the stored monitor data into the shape the status panel consumes. */
@Service
public class StatusService {

    private static final int SECONDS_PER_DAY = 86_400;

    private final Clock clock;
    private final StatusRepository statusRepository;
    private final MonitorProperties monitorProperties;

    public StatusService(StatusRepository statusRepository, MonitorProperties monitorProperties, Clock clock) {
        this.clock = clock;
        this.statusRepository = statusRepository;
        this.monitorProperties = monitorProperties;
    }

    public StatusDto readStatus() {
        Map<String, List<ServiceDay>> days = daysByService();
        Map<String, ServiceCheck> checks = latestCheckByService();
        Map<String, Incident> incidents = latestIncidentByService();

        List<StatusDto.ServiceStatus> services = new ArrayList<>();

        for (MonitorProperties.Service service : monitorProperties.services()) {
            String name = service.name();

            services.add(
                toServiceStatus(name, checks.get(name), days.getOrDefault(name, List.of()), incidents.get(name)));
        }

        return new StatusDto(clock.instant(), services);
    }

    private Map<String, ServiceCheck> latestCheckByService() {
        Map<String, ServiceCheck> byService = new HashMap<>();

        for (ServiceCheck check : statusRepository.getLatestChecks()) {
            byService.put(check.service(), check);
        }

        return byService;
    }

    private Map<String, Incident> latestIncidentByService() {
        Map<String, Incident> byService = new HashMap<>();

        for (Incident incident : statusRepository.getLatestIncidents()) {
            byService.put(incident.service(), incident);
        }

        return byService;
    }

    private Map<String, List<ServiceDay>> daysByService() {
        Map<String, List<ServiceDay>> byService = new HashMap<>();

        for (ServiceDay day : statusRepository.getServiceDays()) {
            byService.computeIfAbsent(day.service(), name -> new ArrayList<>()).add(day);
        }

        return byService;
    }

    private StatusDto.ServiceStatus toServiceStatus(
        String name, ServiceCheck check, List<ServiceDay> days, Incident incident) {
        LocalDate today = LocalDate.now(clock.withZone(ZoneOffset.UTC));

        return new StatusDto.ServiceStatus(
            name,
            check != null && check.up(),
            check == null ? null : check.latencyMs(),
            check == null ? null : check.statusCode(),
            check == null ? null : check.checkedAt(),
            new StatusDto.Uptime(
                uptimeOver(days, 7, today),
                uptimeOver(days, 30, today),
                uptimeOver(days, 90, today)),
            toDayUptimes(days, today),
            toLastIncident(incident));
    }

    private List<StatusDto.DayUptime> toDayUptimes(List<ServiceDay> days, LocalDate today) {
        List<StatusDto.DayUptime> uptimes = new ArrayList<>();

        for (ServiceDay day : days) {
            uptimes.add(toDayUptime(day, today));
        }

        return uptimes;
    }

    private StatusDto.DayUptime toDayUptime(ServiceDay day, LocalDate today) {
        long expected = expectedChecks(day.day(), today);

        return new StatusDto.DayUptime(
            day.day(),
            percentage(day.ups(), expected),
            day.checks() == 0 ? null : Math.toIntExact(day.latencySum() / day.checks()),
            day.checks() < expected);
    }

    private StatusDto.LastIncident toLastIncident(Incident incident) {
        if (incident == null) {
            return null;
        }

        return new StatusDto.LastIncident(
            incident.startedAt(), incident.endedAt(), incident.statusCode(), incident.endedAt() == null);
    }

    private double uptimeOver(List<ServiceDay> days, int window, LocalDate today) {
        LocalDate from = today.minusDays(window - 1L);
        long ups = 0;
        long expected = 0;

        for (ServiceDay day : days) {
            if (!day.day().isBefore(from)) {
                ups += day.ups();
                expected += expectedChecks(day.day(), today);
            }
        }

        return percentage(ups, expected);
    }

    private long expectedChecks(LocalDate day, LocalDate today) {
        Duration interval = monitorProperties.interval();
        long elapsed = day.isEqual(today)
            ? Duration.between(today.atStartOfDay(ZoneOffset.UTC), clock.instant().atZone(ZoneOffset.UTC)).toSeconds()
            : SECONDS_PER_DAY;

        return Math.max(1, elapsed / Math.max(1, interval.toSeconds()));
    }

    private double percentage(long actual, long expected) {
        if (expected <= 0) {
            return 0;
        }

        return Math.round(Math.min(100.0, 100.0 * actual / expected) * 100.0) / 100.0;
    }
}
