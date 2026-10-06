package com.portfolio.telemetry;

import org.springframework.stereotype.Component;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.UncheckedIOException;

import java.nio.file.Path;
import java.nio.file.Files;
import java.nio.file.FileStore;

import java.time.Duration;

import java.util.stream.Stream;

/** Reads the host's vitals from /proc, /sys and the mounted filesystem. */
@Component
public class HostMetricsReader {

    private static final Duration CPU_SAMPLE_WINDOW = Duration.ofSeconds(1);

    private static final Logger log = LoggerFactory.getLogger(HostMetricsReader.class);

    private final TelemetryProperties telemetryProperties;
    private final Path thermalSensor;

    public HostMetricsReader(TelemetryProperties telemetryProperties) {
        this.telemetryProperties = telemetryProperties;
        this.thermalSensor = resolveThermalSensor();

        if (telemetryProperties.thermalSensor() != null && thermalSensor == null) {
            log.warn("No device named {} under {}; temperature will be reported as null",
                telemetryProperties.thermalSensor(), telemetryProperties.devicePath());
        }
    }

    public HostMetrics read() throws InterruptedException {
        String meminfo = readFile(procFile("meminfo"));

        return new HostMetrics(
            cpuPercent(),
            ProcParser.memUsedBytes(meminfo),
            ProcParser.memTotalBytes(meminfo),
            diskUsedBytes(),
            diskTotalBytes(),
            ProcParser.load1m(readFile(procFile("loadavg"))),
            ProcParser.uptimeSeconds(readFile(procFile("uptime"))),
            tempCelsius());
    }

    private double cpuPercent() throws InterruptedException {
        CpuTimes before = ProcParser.cpuTimes(readFile(procFile("stat")));

        Thread.sleep(CPU_SAMPLE_WINDOW);

        return ProcParser.cpuTimes(readFile(procFile("stat"))).percentSince(before);
    }

    Double tempCelsius() {
        Path sensor = thermalSensor;

        if (sensor == null) {
            return null;
        }

        try {
            return ProcParser.celsius(Files.readString(sensor));
        } catch (IOException | NumberFormatException ex) {
            return null;
        }
    }

    private Path resolveThermalSensor() {
        String wanted = telemetryProperties.thermalSensor();

        if (wanted == null || wanted.isBlank()) {
            return null;
        }

        try (Stream<Path> devices = Files.list(Path.of(telemetryProperties.devicePath()))) {
            return devices
                .filter(device -> wanted.equals(nameOf(device)))
                .map(device -> device.resolve("temp1_input"))
                .filter(Files::isRegularFile)
                .findFirst()
                .orElse(null);
        } catch (IOException ex) {
            return null;
        }
    }

    private String nameOf(Path device) {
        try {
            return Files.readString(device.resolve("name")).trim();
        } catch (IOException ex) {
            return null;
        }
    }

    private long diskTotalBytes() {
        try {
            return fileStore().getTotalSpace();
        } catch (IOException ex) {
            throw new UncheckedIOException(ex);
        }
    }

    private long diskUsedBytes() {
        try {
            FileStore store = fileStore();

            return store.getTotalSpace() - store.getUnallocatedSpace();
        } catch (IOException ex) {
            throw new UncheckedIOException(ex);
        }
    }

    private FileStore fileStore() throws IOException {
        return Files.getFileStore(Path.of(telemetryProperties.diskPath()));
    }

    private Path procFile(String name) {
        return Path.of(telemetryProperties.procPath(), name);
    }

    private String readFile(Path path) {
        try {
            return Files.readString(path);
        } catch (IOException ex) {
            throw new UncheckedIOException(ex);
        }
    }
}
