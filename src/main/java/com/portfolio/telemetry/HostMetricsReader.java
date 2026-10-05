package com.portfolio.telemetry;

import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.FileStore;
import java.nio.file.Path;
import java.time.Duration;

/** Reads the host's vitals from /proc, /sys and the mounted filesystem. */
@Component
public class HostMetricsReader {

    private static final Duration CPU_SAMPLE_WINDOW = Duration.ofSeconds(1);

    private final TelemetryProperties telemetryProperties;

    public HostMetricsReader(TelemetryProperties telemetryProperties) {
        this.telemetryProperties = telemetryProperties;
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

    private Double tempCelsius() {
        String path = telemetryProperties.thermalPath();

        if (path == null || path.isBlank() || Files.notExists(Path.of(path))) {
            return null;
        }

        return ProcParser.celsius(readFile(Path.of(path)));
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

            return store.getTotalSpace() - store.getUsableSpace();
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
