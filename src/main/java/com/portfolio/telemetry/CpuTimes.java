package com.portfolio.telemetry;

/** Cumulative CPU jiffies since boot, from the first line of /proc/stat. */
record CpuTimes(
    long busy,
    long idle
) {

    /** Share of time spent working between an earlier reading and this one. */
    double percentSince(CpuTimes earlier) {
        double busyDelta = busy - earlier.busy();
        double total = busyDelta + (idle - earlier.idle());

        if (total <= 0) {
            return 0;
        }

        return Math.round(100.0 * busyDelta / total * 100.0) / 100.0;
    }
}
