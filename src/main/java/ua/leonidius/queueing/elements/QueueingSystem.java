package ua.leonidius.queueing.elements;

import lombok.Getter;
import lombok.Setter;

/**
 * A Queueing system element (Система масового обслуговування)
 */
public class QueueingSystem extends Element {

    @Getter @Setter private int currentQueueLength;
    @Getter @Setter private int queueCapacity;
    @Getter private int numberOfDropouts;
    @Getter private double meanQueueLengthAccumulator;

    public QueueingSystem(double delay) {
        super(delay);
        currentQueueLength = 0;
        queueCapacity = Integer.MAX_VALUE;
        meanQueueLengthAccumulator = 0.0;
    }

    @Override
    public void onCustomerArrival() {
        if (super.getState() == 0) { // state 0 - free
            super.setState(1); // state 1 - busy
            super.setNextEventTime(super.getCurrentTime() + super.getServiceTime());
        } else {
            if (getCurrentQueueLength() < getQueueCapacity()) {
                setCurrentQueueLength(getCurrentQueueLength() + 1);
            } else {
                numberOfDropouts++;
            }
        }
    }

    @Override
    public void onServiceCompletion() {
        super.onServiceCompletion();
        super.setNextEventTime(Double.MAX_VALUE);
        super.setState(0);
        if (getCurrentQueueLength() > 0) {
            setCurrentQueueLength(getCurrentQueueLength() - 1);
            super.setState(1);
            super.setNextEventTime(super.getCurrentTime() + super.getServiceTime());
        }
    }

    @Override
    public void printInfo() {
        super.printInfo();
        System.out.println("failure = " + this.getNumberOfDropouts());
    }

    @Override
    public void doStatistics(double delta) {
        meanQueueLengthAccumulator = getMeanQueueLengthAccumulator() + currentQueueLength * delta;
    }

}
