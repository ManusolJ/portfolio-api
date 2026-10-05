package com.portfolio.telemetry;

/** Turns the text of /proc and /sys files into numbers; does no I/O of its own. */
final class ProcParser {

    private static final long KIB = 1024;

    private ProcParser() {

    }

    static CpuTimes cpuTimes(String procStat) {
        String[] fields = procStat.lines().findFirst().orElseThrow().trim().split("\s+");
        long user = Long.parseLong(fields[1]);
        long nice = Long.parseLong(fields[2]);
        long system = Long.parseLong(fields[3]);
        long idle = Long.parseLong(fields[4]);
        long iowait = Long.parseLong(fields[5]);
        long irq = Long.parseLong(fields[6]);
        long softirq = Long.parseLong(fields[7]);
        long steal = Long.parseLong(fields[8]);

        return new CpuTimes(user + nice + system + irq + softirq + steal, idle + iowait);
    }

    static long memTotalBytes(String procMeminfo) {
        return kibEntry(procMeminfo, "MemTotal") * KIB;
    }

    static long memUsedBytes(String procMeminfo) {
        return (kibEntry(procMeminfo, "MemTotal") - kibEntry(procMeminfo, "MemAvailable")) * KIB;
    }

    static double load1m(String procLoadavg) {
        return Double.parseDouble(procLoadavg.trim().split("\s+")[0]);
    }

    static long uptimeSeconds(String procUptime) {
        return (long) Double.parseDouble(procUptime.trim().split("\s+")[0]);
    }

    static double celsius(String thermalZoneTemp) {
        return Long.parseLong(thermalZoneTemp.trim()) / 1000.0;
    }

    private static long kibEntry(String procMeminfo, String key) {
        for (String line : procMeminfo.lines().toList()) {
            if (line.startsWith(key + ":")) {
                return Long.parseLong(line.replace(key + ":", "").replace("kB", "").trim());
            }
        }

        throw new IllegalArgumentException("No " + key + " in /proc/meminfo");
    }
}
