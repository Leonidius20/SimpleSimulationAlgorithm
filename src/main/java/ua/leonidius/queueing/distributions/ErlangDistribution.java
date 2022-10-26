package ua.leonidius.queueing.distributions;

public class ErlangDistribution implements ProbabilityDistribution {

    private final double mean;
    private final int k;
    private final ExponentialDistribution expDist;

    public ErlangDistribution(double mean, int k) {
        this.mean = mean;
        this.k = k;
        this.expDist = new ExponentialDistribution(mean / k);
    }

    @Override
    public double next() {
        double result = 0;

        for (int i = 0; i < k; i++) {
            result += expDist.next();
        }

        return result;
    }
}
