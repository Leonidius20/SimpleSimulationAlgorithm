package ua.leonidius.queueing.elements;

import lombok.Getter;
import ua.leonidius.queueing.Customer;
import ua.leonidius.queueing.distributions.ProbabilityDistribution;

import java.util.Arrays;
import java.util.Random;

public class Source extends Element {

    /**
     * An accumulator of timestamps at which customers where
     * created. Used to calculate mean time clients spend
     * in the system.
     */
    @Getter private double customerEnterTimesAccumulator;

    private final int[] customerTypes;
    private final double[] probabilities;

    private final Random randomG = new Random();

    public Source(ProbabilityDistribution distribution,
                  int[] customerTypes, double[] probabilities) {
        super(distribution);

        if (customerTypes.length < 1)
            throw new RuntimeException("There should be at least 1 customer type provided");

        if (Arrays.stream(probabilities).sum() != 1.0) {
            throw new RuntimeException("Probabilities do not add up to 1");
        }

        this.customerTypes = customerTypes;
        this.probabilities = probabilities;
    }

    @Override
    public void onServiceCompletion() {
        super.onServiceCompletion();
        super.setNextEventTime(super.getCurrentTime() + super.getServiceTime(null));
        customerEnterTimesAccumulator += getCurrentTime();

        // selecting next customer type
        Customer customer = null;
        double x = randomG.nextDouble();
        for (int i = 0; i < probabilities.length; ++i) {
            x -= probabilities[i];
            if (x <= 0) {
                customer = new Customer(customerTypes[i], getCurrentTime());
                break;
            }
        }
        if (customer == null)
            customer = new Customer(customerTypes[customerTypes.length - 1], getCurrentTime());

        getNextElement().onCustomerArrival(customer);
    }

}
