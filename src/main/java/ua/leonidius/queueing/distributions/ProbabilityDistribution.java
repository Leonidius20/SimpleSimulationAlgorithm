package ua.leonidius.queueing.distributions;

public interface ProbabilityDistribution {

    /**
     * @return a randomly generated value according to this distribution
     */
    double next();

}
