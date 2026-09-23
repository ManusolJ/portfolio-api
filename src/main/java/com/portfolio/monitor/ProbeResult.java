package com.portfolio.monitor;

/** Outcome of one probe; statusCode is null when nothing answered. */
public record ProbeResult(
    boolean up,
    long latencyMs,
    Integer statusCode
) {

}
