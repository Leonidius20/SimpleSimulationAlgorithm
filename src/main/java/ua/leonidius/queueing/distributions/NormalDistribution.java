package ua.leonidius.queueing.distributions;

import java.util.Random;

/**
 * Also known as Gaussian distribution
 */
public class NormalDistribution implements ProbabilityDistribution {

    private final double mean;
    private final double deviation;
    private final Random random;

    public NormalDistribution(double mean, double deviation) {
        this.mean = mean;
        this.deviation = deviation;
        this.random = new Random();
    }

    @Override
    public double next() {
        return mean + deviation * random.nextGaussian();
    }
}
