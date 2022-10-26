package ua.leonidius.queueing.distributions;

public class ExponentialDistribution implements ProbabilityDistribution {

    private final double mean;

    public ExponentialDistribution(double mean) {
        this.mean = mean;
    }

    @Override
    public double next() {
        double a = 0;
        while (a == 0) {
            a = Math.random();
        }
        a = -mean * Math.log(a);
        return a;
    }
}
