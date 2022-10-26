package ua.leonidius.queueing.distributions;

public class UniformDistribution implements ProbabilityDistribution {

    private final double minValue;
    private final double maxValue;

    public UniformDistribution(double minValue, double maxValue) {
        this.minValue = minValue;
        this.maxValue = maxValue;
    }

    @Override
    public double next() {
        double a = 0;
        while (a == 0) {
            a = Math.random();
        }
        a = minValue + a * (maxValue - minValue);
        return a;
    }
}
