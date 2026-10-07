package com.portfolio.status;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import com.portfolio.monitor.MonitorProperties;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/** Pins the clock and feeds stored rows in, to check the uptime arithmetic. */
class StatusServiceTest {

    private static final String SERVICE = "portfolio-api";
    private static final Instant NOON = Instant.parse("2026-10-05T12:00:00Z");
    private static final LocalDate TODAY = LocalDate.parse("2026-10-05");
    private static final int EXPECTED_PER_DAY = 1440;

    private StatusRepository repository;
    private StatusService statusService;

    @BeforeEach
    void setUp() {
        repository = Mockito.mock(StatusRepository.class);
        MonitorProperties properties = new MonitorProperties(
            true, Duration.ofSeconds(60), "0 5 * * * *",
            List.of(new MonitorProperties.Service("https://x.test", SERVICE)));

        statusService = new StatusService(repository, properties, Clock.fixed(NOON, ZoneOffset.UTC));
    }

    @Test
    void reportsCurrentStateFromTheLatestCheck() {
        Mockito.when(repository.getLatestChecks())
            .thenReturn(List.of(new ServiceCheck(true, SERVICE, NOON, 42, 200)));
        Mockito.when(repository.getServiceDays()).thenReturn(List.of());
        Mockito.when(repository.getLatestIncidents()).thenReturn(List.of());

        StatusDto.ServiceStatus status = statusService.readStatus().services().getFirst();

        assertThat(status.name()).isEqualTo(SERVICE);
        assertThat(status.up()).isTrue();
        assertThat(status.latencyMs()).isEqualTo(42);
        assertThat(status.lastIncident()).isNull();
    }

    @Test
    void countsAFullDayOfChecksAsFullUptime() {
        givenDays(new ServiceDay(EXPECTED_PER_DAY, EXPECTED_PER_DAY, TODAY.minusDays(1), SERVICE, 14_400L));

        StatusDto.DayUptime yesterday = statusService.readStatus().services().getFirst().days().getFirst();

        assertThat(yesterday.uptime()).isEqualTo(100.0);
        assertThat(yesterday.hasGap()).isFalse();
        assertThat(yesterday.avgLatencyMs()).isEqualTo(10);
    }

    @Test
    void countsMissingChecksAgainstUptimeRatherThanIgnoringThem() {
        givenDays(new ServiceDay(720, 720, TODAY.minusDays(1), SERVICE, 7_200L));

        StatusDto.DayUptime yesterday = statusService.readStatus().services().getFirst().days().getFirst();

        assertThat(yesterday.uptime()).isEqualTo(50.0);
        assertThat(yesterday.hasGap()).isTrue();
    }

    @Test
    void toleratesTheHandfulOfChecksSchedulingJitterCosts() {
        givenDays(new ServiceDay(1432, 1432, TODAY.minusDays(1), SERVICE, 14_320L));

        StatusDto.DayUptime yesterday = statusService.readStatus().services().getFirst().days().getFirst();

        assertThat(yesterday.hasGap()).isFalse();
        assertThat(yesterday.uptime()).isEqualTo(99.44);
    }

    @Test
    void flagsADayTheMonitorWasActuallyAbsentFor() {
        givenDays(new ServiceDay(1400, 1400, TODAY.minusDays(1), SERVICE, 14_000L));

        StatusDto.DayUptime yesterday = statusService.readStatus().services().getFirst().days().getFirst();

        assertThat(yesterday.hasGap()).isTrue();
    }

    @Test
    void expectsOnlyTheChecksDueSoFarToday() {
        givenDays(new ServiceDay(720, 720, TODAY, SERVICE, 7_200L));

        StatusDto.DayUptime today = statusService.readStatus().services().getFirst().days().getFirst();

        assertThat(today.uptime()).isEqualTo(100.0);
        assertThat(today.hasGap()).isFalse();
    }

    @Test
    void averagesWindowsOverExpectedChecks() {
        givenDays(
            new ServiceDay(EXPECTED_PER_DAY, EXPECTED_PER_DAY, TODAY.minusDays(2), SERVICE, 14_400L),
            new ServiceDay(0, 0, TODAY.minusDays(1), SERVICE, 0L));

        StatusDto.Uptime uptime = statusService.readStatus().services().getFirst().uptime();

        assertThat(uptime.last7Days()).isEqualTo(50.0);
    }

    @Test
    void reportsAnOngoingIncident() {
        Mockito.when(repository.getLatestChecks()).thenReturn(List.of());
        Mockito.when(repository.getServiceDays()).thenReturn(List.of());
        Mockito.when(repository.getLatestIncidents())
            .thenReturn(List.of(new Incident(1L, SERVICE, null, NOON.minusSeconds(3600), null)));

        StatusDto.LastIncident incident = statusService.readStatus().services().getFirst().lastIncident();

        assertThat(incident.ongoing()).isTrue();
        assertThat(incident.endedAt()).isNull();
    }

    private void givenDays(ServiceDay... days) {
        Mockito.when(repository.getLatestChecks()).thenReturn(List.of());
        Mockito.when(repository.getServiceDays()).thenReturn(List.of(days));
        Mockito.when(repository.getLatestIncidents()).thenReturn(List.of());
    }
}
