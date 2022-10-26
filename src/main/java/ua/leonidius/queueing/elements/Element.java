package ua.leonidius.queueing.elements;

import lombok.Getter;
import lombok.Setter;
import ua.leonidius.queueing.Customer;
import ua.leonidius.queueing.distributions.ConstantValue;
import ua.leonidius.queueing.distributions.ExponentialDistribution;
import ua.leonidius.queueing.distributions.ProbabilityDistribution;

public class Element {

    @Getter @Setter private String name;
    @Getter @Setter private double currentTime = 0.0;
    @Setter private double nextEventTime = 0.0;

    @Getter @Setter private ProbabilityDistribution distribution;

    @Getter @Setter protected int numberOfCustomersServed;
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
        distribution = new ExponentialDistribution(1.0);

        id = nextId;
        nextId++;
        name = "element" + id;
    }

    public Element(double constantServiceTime) {
        name = "anonymous";

        // this.meanServiceTime = meanServiceTime;
        distribution = new ConstantValue(constantServiceTime);

        id = nextId;
        nextId++;
        name = "element" + id;
    }

    public Element(ProbabilityDistribution distribution) {
        name = "anonymous";

        this.distribution = distribution;

        id = nextId;
        nextId++;
        name = "element" + id;
    }

    public Element(ProbabilityDistribution distribution, String nameOfElement) {
        name = nameOfElement;

        this.distribution = distribution;

        id = nextId;
        nextId++;
        name = "element" + id;
    }

    public double getServiceTime() {
        return distribution.next();
    }

    public void onCustomerArrival(Customer customer) {

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
