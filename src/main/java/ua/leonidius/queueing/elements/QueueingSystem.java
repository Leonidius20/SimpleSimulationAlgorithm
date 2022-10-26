package ua.leonidius.queueing.elements;

import lombok.Getter;
import lombok.Setter;
import ua.leonidius.queueing.Customer;
import ua.leonidius.queueing.utils.ProbabilityDistribution;

import java.util.Arrays;
import java.util.Deque;
import java.util.LinkedList;

/**
 * A Queueing system element (Система масового обслуговування)
 */
public class QueueingSystem extends Element {

   // @Getter @Setter private int currentQueueLength;
    @Getter @Setter private int queueCapacity;

    // @Getter private int numberOfProcessors = 1;
    private final Processor[] processors;

    //@Getter private final int[] states;
    //@Getter private final double[] nextEventTimes;

    // private final Customer[] customersBeingProcessed;

    @Getter private final Deque<Customer> queue = new LinkedList<>();

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

    @Getter private int numberOfDropouts;
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
     * who chose to switch to this system
     */
    @Getter private int numberOfRefugees = 0;

    public QueueingSystem(double delay) {
        super(delay);
        // currentQueueLength = 0;
        queueCapacity = Integer.MAX_VALUE;
        meanQueueLengthAccumulator = 0.0;

        processors = new Processor[1];
        processors[0] = new Processor(this);

        this.nextEventProcessorIndex = 0;
    }

    public QueueingSystem(double delay, int queueCapacity,
                          ProbabilityDistribution distribution, String name) {
        super(name, delay);
        setQueueCapacity(queueCapacity);
        setDistribution(distribution);

        processors = new Processor[1];
        processors[0] = new Processor(this);

        this.nextEventProcessorIndex = 0;
    }

    public QueueingSystem(int numberOfProcessors, double delay, int queueCapacity,
                          ProbabilityDistribution distribution, String name) {
        super(name, delay);
        setQueueCapacity(queueCapacity);
        setDistribution(distribution);

        processors = new Processor[numberOfProcessors];
        for (int i = 0; i < processors.length; i++) {
            processors[i] = new Processor(this);
        }

        this.nextEventProcessorIndex = 0;
    }

    @Override
    public void onCustomerArrival(Customer customer) {
        var freeProcessorOptional = Arrays.stream(processors)
                .filter(Processor::isFree).findFirst();

        if (freeProcessorOptional.isPresent()) {
            var freeProcessor = freeProcessorOptional.get();
            freeProcessor.acceptCustomer(customer);
            updateNextEventProcessorIndex();
        } else if (queue.size() < getQueueCapacity()) { // no free processors, but q isn't full
            // currentQueueLength++;
            queue.addLast(customer);
        } else {
            numberOfDropouts++;
            dropoutTimestampsAccumulator += getCurrentTime();
        }
    }

    @Override
    public void onServiceCompletion() {
        super.onServiceCompletion();

        if (processors[nextEventProcessorIndex].isFree()) {
            throw new RuntimeException("Processor designated as first to finish service is actually empty");
        }

        var processedCustomer
                = processors[nextEventProcessorIndex].onServingComplete();

        // accepting a customer form q for service instead of the one that finished
        if (!queue.isEmpty()) {
            // setCurrentQueueLength(getCurrentQueueLength() - 1);
            var customerFromQ = queue.pollFirst();
            processors[nextEventProcessorIndex].acceptCustomer(customerFromQ);
        }

        updateNextEventProcessorIndex();

        // accepting a refugee from a twin system (is possible)
        if (twinQSystem != null) {
            // if the twin's system q is at least 2 customers longer than our q
            // and we have a free place is our q
            if (queue.size() < queueCapacity
                    && (twinQSystem.queue.size() - this.queue.size()) >= 2) {
                // stealing a guy from their q
                // twinQSystem.setCurrentQueueLength(twinQSystem.getCurrentQueueLength() - 1);
                var stolenCustomer = twinQSystem.queue.pollLast();

                onCustomerArrival(stolenCustomer);

                numberOfRefugees++;
            }
        }

        if (getNextElement() != null) {
            getNextElement().onCustomerArrival(processedCustomer);
        }
    }

    private void updateNextEventProcessorIndex() {
        int nextProcessorIndex = 0;
        double nextEventTime = processors[0].getNextEventTime();

        for (int i = 1; i < processors.length; i++) {
            double procINextEventTime = processors[i].getNextEventTime();
            if (procINextEventTime < nextEventTime) {
                nextProcessorIndex = i;
                nextEventTime = procINextEventTime;
            }
        }

        this.nextEventProcessorIndex = nextProcessorIndex;
    }

    @Override
    public void printInfo() {
        System.out.println(getName() + " states= " + Arrays.stream(processors).mapToInt(Processor::getState).toString() +
                " quantity served = " + getNumberOfCustomersServed() +
                " tnext= " + getNextEventTime() + " queue length " + queue.size());
        System.out.println("failure = " + this.getNumberOfDropouts());
    }

    @Override
    public void doStatistics(double delta) {
        meanQueueLengthAccumulator += queue.size() * delta;

        var numCustomersBeingProcessed
                = Arrays.stream(processors).mapToInt(Processor::getState).sum();

        meanUtilizationAccumulator +=
                + (numCustomersBeingProcessed / (double)processors.length) * delta;

        meanNumberOfCustomersInSystemAccumulator +=
                (queue.size() + numCustomersBeingProcessed) * delta;
        // TODO: change if states become not only 0 and 1
    }

    @Override
    public double getNextEventTime() {
        return processors[nextEventProcessorIndex].getNextEventTime();
    }

    /**
     * Represents one processor of the queueing system
     */
    static class Processor {

        private final QueueingSystem parentQSystem;
        @Getter @Setter private int state = 0;
        @Getter @Setter private double nextEventTime = Double.MAX_VALUE;
        @Getter @Setter private Customer customerBeingServed = null;

        Processor(QueueingSystem parentQSystem) {
            this.parentQSystem = parentQSystem;
        }

        /**
         * Called by QueueingSystem when the time is equal to nextEventTime
         * (i.e. the customer should finish being served)
         * @return customer that was served
         */
        public Customer onServingComplete() {
            if (isFree())
                throw new RuntimeException("onServingComplete() called on free processor");
            var customer = customerBeingServed.copy();
            state = 0;
            nextEventTime = Double.MAX_VALUE;
            customerBeingServed = null;
            return customer;
        }

        public boolean isFree() {
            return state == 0;
        }

        /**
         * Called by the parent QueueingSystem if it wants this processor to
         * accept a new customer for serving
         * @param customer customer to start serving
         */
        public void acceptCustomer(Customer customer) {
            if (!isFree())
                throw new RuntimeException("Called acceptCustomer() on a busy processor");

            state = 1;
            nextEventTime = parentQSystem.getCurrentTime() + parentQSystem.getServiceTime();
            customerBeingServed = customer;
        }

    }

}
