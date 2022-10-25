package ua.leonidius.queueing.elements;

import lombok.Getter;
import lombok.Setter;
import ua.leonidius.queueing.utils.ProbabilityDistribution;
import ua.leonidius.queueing.utils.ProbabilityDistributions;

public class Element {

    @Getter @Setter private String name;
    @Getter @Setter private double currentTime = 0.0;
    @Setter private double nextEventTime = 0.0;
    @Getter @Setter private double meanServiceTime;
    @Getter @Setter private double serviceTimeStdDeviation;
    @Getter @Setter private ProbabilityDistribution distribution;
    @Getter @Setter private int numberOfCustomersServed;
    @Getter @Setter private int state = 0;

    /**
     * The element to which an object goes after finishing being served by this element
     */
    @Getter @Setter private Element nextElement = null;

    private static int nextId = 0;
    @Getter @Setter private int id;

    /**
     * Whether this element can generate events (service completion for customer)
     * and should therefore be appropriately processed by simulation algorithm
     */
    @Getter protected boolean generatesEvents = true;

    public Element() {
        meanServiceTime = 1.0;

        distribution = ProbabilityDistribution.EXPONENTIAL;

        id = nextId;
        nextId++;
        name = "element" + id;
    }

    public Element(double meanServiceTime) {
        name = "anonymous";

        this.meanServiceTime = meanServiceTime;
        distribution = ProbabilityDistribution.NONE;

        id = nextId;
        nextId++;
        name = "element" + id;
    }

    public Element(ProbabilityDistribution distribution, double[] distParams) {
        name = "anonymous";

        if (distribution == ProbabilityDistribution.EXPONENTIAL
                || distribution == ProbabilityDistribution.NONE) {
            this.meanServiceTime = distParams[0];
        } else {
            this.meanServiceTime = distParams[0];
            this.serviceTimeStdDeviation = distParams[1];
        }

        this.distribution = distribution;

        id = nextId;
        nextId++;
        name = "element" + id;
    }

    public Element(String nameOfElement, double meanServiceTime) {
        name = nameOfElement;

        this.meanServiceTime = meanServiceTime;

        distribution = ProbabilityDistribution.EXPONENTIAL;

        id = nextId;
        nextId++;
        name = "element" + id;
    }

    public double getServiceTime() {
        return switch (distribution) {
            case UNIFORM ->  ProbabilityDistributions.randomUniform(meanServiceTime,
                    serviceTimeStdDeviation);
            case GAUSSIAN -> ProbabilityDistributions.randomNormal(meanServiceTime,
                    serviceTimeStdDeviation);
            case EXPONENTIAL -> ProbabilityDistributions.randomExponential(meanServiceTime);
            default -> meanServiceTime;
        };
    }

    public void onCustomerArrival() {

    }

    public void onServiceCompletion() {
        numberOfCustomersServed++;
    }

    public void printResult() {
        System.out.println(getName() + " quantity = " + numberOfCustomersServed);
    }

    public void printInfo() {
        System.out.println(getName() + " state= " + state +
                " quantity = " + numberOfCustomersServed +
                " tnext= " + nextEventTime);
    }

    public void doStatistics(double delta) {

    }

    public double getNextEventTime() {return nextEventTime; }

}
