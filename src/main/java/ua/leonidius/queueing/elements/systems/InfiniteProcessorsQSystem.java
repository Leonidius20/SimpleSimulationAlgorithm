package ua.leonidius.queueing.elements.systems;

import ua.leonidius.queueing.Customer;
import ua.leonidius.queueing.distributions.ProbabilityDistribution;

import java.util.Map;

/**
 * A queueing system that adds more processors as needed. It doesn't have a queue
 */
public class InfiniteProcessorsQSystem extends QueueingSystem {

    public InfiniteProcessorsQSystem(ProbabilityDistribution distribution, String name, Map<Integer, Integer> transformationRules) {
        super(1, distribution, new NoQueue(), name, transformationRules);
    }

    public InfiniteProcessorsQSystem(ProbabilityDistribution distribution, String name) {
        super(1, distribution, new NoQueue(), name);
    }

    @Override
    public void onCustomerArrival(Customer customer) {
        updateArrivalsStats();

        var freeProcessorOptional = processors.stream()
                .filter(Processor::isFree).findFirst();

        if (freeProcessorOptional.isPresent()) {
            var freeProcessor = freeProcessorOptional.get();
            freeProcessor.acceptCustomer(customer);
            updateNextEventProcessorIndex();
        } else {
            var newProcessor = new Processor(this);
            newProcessor.acceptCustomer(customer);
            processors.add(newProcessor);
            updateNextEventProcessorIndex();
        }
    }

}
