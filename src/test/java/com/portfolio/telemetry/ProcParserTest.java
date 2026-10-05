package com.portfolio.telemetry;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/** Fixtures are verbatim output from a Linux host. */
class ProcParserTest {

    private static final String PROC_STAT =
        """
            cpu  682 0 1012 4899985 634 0 158 0 0 0
            cpu0 170 0 253 1224996 158 0 39 0 0 0
            intr 1234567
            """;

    private static final String PROC_MEMINFO =
        """
            MemTotal:       16264320 kB
            MemFree:        15027700 kB
            MemAvailable:   15401756 kB
            Buffers:           35432 kB
            """;

    @Test
    void readsTheAggregateCpuLineOnly() {
        CpuTimes times = ProcParser.cpuTimes(PROC_STAT);

        assertThat(times.busy()).isEqualTo(682 + 0 + 1012 + 0 + 158 + 0);
        assertThat(times.idle()).isEqualTo(4_899_985L + 634);
    }

    @Test
    void measuresCpuAsTheShareOfBusyTimeBetweenTwoReadings() {
        CpuTimes earlier = new CpuTimes(1000, 1000);
        CpuTimes later = new CpuTimes(1150, 1050);

        assertThat(later.percentSince(earlier)).isEqualTo(75.0);
    }

    @Test
    void reportsZeroCpuWhenNoTimePassed() {
        CpuTimes same = new CpuTimes(1000, 1000);

        assertThat(same.percentSince(same)).isZero();
    }

    @Test
    void countsMemoryUsedAsTotalMinusAvailable() {
        assertThat(ProcParser.memTotalBytes(PROC_MEMINFO)).isEqualTo(16_264_320L * 1024);
        assertThat(ProcParser.memUsedBytes(PROC_MEMINFO)).isEqualTo((16_264_320L - 15_401_756L) * 1024);
    }

    @Test
    void failsLoudlyWhenMeminfoLacksAnEntry() {
        assertThatThrownBy(() -> ProcParser.memTotalBytes("MemFree: 100 kB"))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("MemTotal");
    }

    @Test
    void readsTheOneMinuteLoadAverage() {
        assertThat(ProcParser.load1m("0.52 0.31 0.14 1/506 1339")).isEqualTo(0.52);
    }

    @Test
    void readsUptimeAsWholeSeconds() {
        assertThat(ProcParser.uptimeSeconds("1751.07 48999.89")).isEqualTo(1751);
    }

    @Test
    void convertsMillidegreesToCelsius() {
        assertThat(ProcParser.celsius("47000\n")).isEqualTo(47.0);
    }
}
