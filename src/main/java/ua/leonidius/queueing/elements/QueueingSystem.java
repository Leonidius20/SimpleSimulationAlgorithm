package ua.leonidius.queueing.elements;

import lombok.Getter;
import lombok.Setter;
import ua.leonidius.queueing.utils.ProbabilityDistribution;

import java.util.Arrays;

/**
 * A Queueing system element (Система масового обслуговування)
 */
public class QueueingSystem extends Element {

    @Getter @Setter private int currentQueueLength;
    @Getter @Setter private int queueCapacity;
    @Getter private int numberOfProcessors = 1;
    @Getter private int numberOfDropouts;

    @Getter private final int[] states;
    @Getter private final double[] nextEventTimes;

    /**
     * A queueing system where customers can move if the queue
     * is shorter there
     */
    @Getter @Setter private QueueingSystem twinQSystem = null;

    /**
     * Index of processor, in which the next event is going to happen,
     * i.e. index of processor that will finish serving the soonest
     */
    private int nextEventProcessorIndex;

    @Getter private double meanQueueLengthAccumulator;
    @Getter private double meanUtilizationAccumulator;

    /**
     * Accumulates the values for the mean number of customers in the system
     * (being processed and in the queue). In order to get the mean value,
     * the value of this variable has to be divided by the total simulation
     * time.
     */
    @Getter private double meanNumberOfCustomersInSystemAccumulator;

    /**
     * Accumulates timestamps at which customers dropped out. Needed to
     * calculate mean time a customer spends in the system.
     */
    @Getter private double dropoutTimestampsAccumulator = 0;

    /**
     * Number of customers from the twin system,
     * who chose to swutch to this system
     */
    @Getter private int numberOfRefugees = 0;

    public QueueingSystem(double delay) {
        super(delay);
        currentQueueLength = 0;
        queueCapacity = Integer.MAX_VALUE;
        meanQueueLengthAccumulator = 0.0;
        this.states = new int[1];
        this.nextEventTimes = new double[] {Double.MAX_VALUE};
        this.nextEventProcessorIndex = 0;
    }

    public QueueingSystem(double delay, int queueCapacity,
                          ProbabilityDistribution distribution, String name) {
        super(name, delay);
        setQueueCapacity(queueCapacity);
        setDistribution(distribution);
        this.states = new int[1];
        this.nextEventTimes = new double[] {Double.MAX_VALUE};
        this.nextEventProcessorIndex = 0;
    }

    public QueueingSystem(int numberOfProcessors, double delay, int queueCapacity,
                          ProbabilityDistribution distribution, String name) {
        super(name, delay);
        setQueueCapacity(queueCapacity);
        setDistribution(distribution);
        this.numberOfProcessors = numberOfProcessors;
        this.states = new int[numberOfProcessors];
        this.nextEventTimes = new double[numberOfProcessors];
        for (int i = 0; i < numberOfProcessors; i++) {
            this.nextEventTimes[i] = Double.MAX_VALUE;
        }
        this.nextEventProcessorIndex = 0;
    }

    @Override
    public void onCustomerArrival() {
        for (int i = 0; i < numberOfProcessors; i++) {
            if (states[i] == 0) {
                states[i] = 1;
                // TODO: each processor will have its own next event
                nextEventTimes[i] = super.getCurrentTime() + super.getServiceTime();

                this.nextEventProcessorIndex = findMinIndex(nextEventTimes);

                return;
            }
        }

        // if not found, try queue
        if (getCurrentQueueLength() < getQueueCapacity()) {
            setCurrentQueueLength(getCurrentQueueLength() + 1);
        } else {
            numberOfDropouts++;
            dropoutTimestampsAccumulator += getCurrentTime();
        }
    }

    @Override
    public void onServiceCompletion() {
        super.onServiceCompletion();

        // making the appropriate processor free
        states[nextEventProcessorIndex] = 0;
        nextEventTimes[nextEventProcessorIndex] = Double.MAX_VALUE;

        this.nextEventProcessorIndex = findMinIndex(nextEventTimes);

        // accepting a customer form q for service
        if (getCurrentQueueLength() > 0) {
            setCurrentQueueLength(getCurrentQueueLength() - 1);

            // find a place for this element from queue
            for (int i = 0; i < numberOfProcessors; i++) {
                if (states[i] == 0) {
                    states[i] = 1;
                    nextEventTimes[i] = super.getCurrentTime() + super.getServiceTime();
                    this.nextEventProcessorIndex = findMinIndex(nextEventTimes);
                }
            }
        }


        // acceoting a refugee from a twin system (is possible)
        if (twinQSystem != null) {
            // if the twin's system q is at least 2 customers longer than our q
            // and we have a free place is our q
            if (currentQueueLength < queueCapacity
                    && twinQSystem.getCurrentQueueLength() - currentQueueLength >= 2) {
                // stealing a guy from their q
                twinQSystem.setCurrentQueueLength(twinQSystem.getCurrentQueueLength() - 1);

                onCustomerArrival();

                numberOfRefugees++;
            }
        }

        if (getNextElement() != null) {
            getNextElement().onCustomerArrival();
        }
    }

    @Override
    public void printInfo() {
        System.out.println(getName() + " states= " + Arrays.toString(states) +
                " quantity served = " + getNumberOfCustomersServed() +
                " tnext= " + getNextEventTime() + " queue length " + getCurrentQueueLength());
        System.out.println("failure = " + this.getNumberOfDropouts());
    }

    @Override
    public void doStatistics(double delta) {
        meanQueueLengthAccumulator = getMeanQueueLengthAccumulator() + currentQueueLength * delta;
        meanUtilizationAccumulator = getMeanUtilizationAccumulator()
                + (Arrays.stream(states).sum() / (double)numberOfProcessors) * delta;

        meanNumberOfCustomersInSystemAccumulator +=
                (currentQueueLength + Arrays.stream(states).sum()) * delta;
        // TODO: change if states become not only 0 and 1
    }

    @Override
    public double getNextEventTime() {
        return Arrays.stream(nextEventTimes).min().getAsDouble();
    }




    private static int findMinIndex(double[] array) {
        // add this
        if (array.length == 0)
            return -1;

        int index = 0;
        double min = array[index];

        for (int i = 1; i < array.length; i++){
            if (array[i] <= min){
                min = array[i];
                index = i;
            }
        }
        return index;
    }

}
