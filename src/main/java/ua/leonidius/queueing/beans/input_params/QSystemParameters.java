package ua.leonidius.queueing.beans.input_params;

/**
 * A record holding a single Queueing System's parameters
 */
public record QSystemParameters(
        double delay, int queueCapacity
) {}
