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
     * Index of processor, in which the next event is going to happen,
     * i.e. index of processor that will finish serving the soonest
     */
    private int nextEventProcessorIndex;

    @Getter private double meanQueueLengthAccumulator;
    @Getter private double meanUtilizationAccumulator;

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
        }
    }

    @Override
    public void onServiceCompletion() {
        super.onServiceCompletion();


        states[nextEventProcessorIndex] = 0;
        nextEventTimes[nextEventProcessorIndex] = Double.MAX_VALUE;
        this.nextEventProcessorIndex = findMinIndex(nextEventTimes);

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
        // TODO: replace getState() when state = blocked is introduced
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
