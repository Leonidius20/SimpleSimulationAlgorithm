package ua.leonidius.queueing.beans.output_params;

public record QSystemPerformanceMetrics(
        int numDropouts, double meanQLength, double meanUtilization
) {}
