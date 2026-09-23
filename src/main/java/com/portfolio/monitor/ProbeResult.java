package com.portfolio.monitor;

public record ProbeResult(
    boolean up,
    long latencyMs,
    Integer statusCode
) {

}
