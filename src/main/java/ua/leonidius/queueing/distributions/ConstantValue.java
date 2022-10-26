package ua.leonidius.queueing.distributions;

public class ConstantValue implements ProbabilityDistribution {

    private final double value;

    public ConstantValue(double value) {
        this.value = value;
    }

    @Override
    public double next() {
        return value;
    }
}
