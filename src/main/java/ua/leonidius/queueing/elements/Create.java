package ua.leonidius.queueing.elements;

import lombok.Getter;
import ua.leonidius.queueing.Customer;
import ua.leonidius.queueing.utils.ProbabilityDistribution;

public class Create extends Element {

    /**
     * An accumulator of timestamps at which customers where
     * created. Used to calculate mean time clients spend
     * in the system.
     */
    @Getter private double customerEnterTimesAccumulator;

    public Create(ProbabilityDistribution distribution, double[] distParams) {
        super(distribution, distParams);
    }

    @Override
    public void onServiceCompletion() {
        super.onServiceCompletion();
        super.setNextEventTime(super.getCurrentTime() + super.getServiceTime());
        customerEnterTimesAccumulator += getCurrentTime();

        var customer = new Customer(1, getCurrentTime());

        getNextElement().onCustomerArrival(customer);
    }

}
