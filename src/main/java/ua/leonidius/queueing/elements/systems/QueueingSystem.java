package ua.leonidius.queueing.elements.systems;

import lombok.Getter;
import lombok.Setter;
import ua.leonidius.queueing.Customer;
import ua.leonidius.queueing.distributions.ProbabilityDistribution;
import ua.leonidius.queueing.elements.Element;

import java.util.*;

/**
 * A Queueing system element (Система масового обслуговування)
 */
public class QueueingSystem extends Element {

    protected final ArrayList<Processor> processors;

    @Getter @Setter private QSQueue queue;

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

    @Getter private int numberOfDropouts = 0;
    @Getter private double meanQueueLengthAccumulator = 0.0;
    @Getter private double meanUtilizationAccumulator = 0.0;

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

    /**
     * Accumulates times between customers arriving to the queueing system.
     * Needed to calculate the avg time between customer arrivals.
     */
    @Getter private double timesBetweenArrivalsAcc;

    /**
     * Timestamp of when the last customer arrived to the queueing system.
     * Needed to calculate the avg time between customer arrivals.
     */
    private double lastArrivalTimestamp = -1;

    private final Map<Integer, Integer> transformationRules;

    public QueueingSystem(int numberOfProcessors, ProbabilityDistribution distribution,
                          QSQueue queue, String name) {
        this(numberOfProcessors, distribution, queue, name, null);
    }

    public QueueingSystem(int numberOfProcessors, ProbabilityDistribution distribution,
                          QSQueue queue, String name, Map<Integer, Integer> transformationRules) {
        super(distribution, name);

        this.queue = queue;

        processors = new ArrayList<>(numberOfProcessors);
        for (int i = 0; i < numberOfProcessors; i++) {
            processors.add(new Processor(this));
        }

        this.nextEventProcessorIndex = 0;

        this.transformationRules = transformationRules;
    }

    public QueueingSystem(int numberOfProcessors, Map<Integer, ProbabilityDistribution> distributionsMap,
                          QSQueue queue, String name) {
        this(numberOfProcessors, distributionsMap, queue, name, null);
    }

    /**
     *
     * @param numberOfProcessors
     * @param distributionsMap mapping from types of customers to distributions of their servicing times
     * @param queue
     * @param name
     * @param transformationRules if you want this system to change types of customers after processing,
     *                            it maps from former type to new type. All possible types should be there
     *                            even if you want some of them to stay the same.
     */
    public QueueingSystem(int numberOfProcessors, Map<Integer, ProbabilityDistribution> distributionsMap,
                          QSQueue queue, String name, Map<Integer, Integer> transformationRules) {
        super(distributionsMap, name);

        this.queue = queue;

        processors = new ArrayList<>(numberOfProcessors);
        for (int i = 0; i < numberOfProcessors; i++) {
            processors.add(new Processor(this));
        }

        this.nextEventProcessorIndex = 0;

        this.transformationRules = transformationRules;
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
        } else if (!queue.isFull()) { // no free processors, but q isn't full
            queue.enqueue(customer);
        } else {
            numberOfDropouts++;
            dropoutTimestampsAccumulator += getCurrentTime();
        }
    }

    protected void updateArrivalsStats() {
        if (lastArrivalTimestamp != -1) { // if this is the first time a customer comes
            timesBetweenArrivalsAcc += (getCurrentTime() - lastArrivalTimestamp);
        }
        lastArrivalTimestamp = getCurrentTime();
    }

    @Override
    public void onServiceCompletion() {
        super.onServiceCompletion();

        var completedProcessor = processors.get(nextEventProcessorIndex);

        if (completedProcessor.isFree()) {
            throw new RuntimeException("Processor designated as first to finish service is actually empty");
        }

        var processedCustomer
                = completedProcessor.onServingComplete();

        // accepting a customer form q for service instead of the one that finished
        if (!queue.isEmpty()) {
            var customerFromQ = queue.takeFirst();
            completedProcessor.acceptCustomer(customerFromQ);
        }

        updateNextEventProcessorIndex();

        // accepting a refugee from a twin system (is possible)
        if (twinQSystem != null) {
            // if the twin's system q is at least 2 customers longer than our q
            // and we have a free place is our q
            if ((!queue.isFull())
                    && (twinQSystem.queue.size() - this.queue.size()) >= 2) {
                // stealing a guy from their q
                // twinQSystem.setCurrentQueueLength(twinQSystem.getCurrentQueueLength() - 1);
                var stolenCustomer = twinQSystem.queue.stealLast();

                onCustomerArrival(stolenCustomer);

                numberOfRefugees++;
            }
        }

        if (getNextElement() != null) {
            if (transformationRules != null) {
                var type = processedCustomer.type();
                if (transformationRules.containsKey(type)) {
                    processedCustomer = new Customer(
                            transformationRules.get(type), processedCustomer.creationTime());
                }
            }

            getNextElement().onCustomerArrival(processedCustomer);
        }
    }

    protected void updateNextEventProcessorIndex() {
        int nextProcessorIndex = 0;
        double nextEventTime = processors.get(0).getNextEventTime();

        for (int i = 1; i < processors.size(); i++) {
            double procINextEventTime = processors.get(i).getNextEventTime();
            if (procINextEventTime < nextEventTime) {
                nextProcessorIndex = i;
                nextEventTime = procINextEventTime;
            }
        }

        this.nextEventProcessorIndex = nextProcessorIndex;
    }

    @Override
    public void printInfo() {
        System.out.println(getName() + " states= " + Arrays.toString(processors.stream().mapToInt(Processor::getState).toArray()) +
                " quantity served = " + getNumberOfCustomersServed() +
                " tnext= " + getNextEventTime() + " queue length " + queue.size());
        System.out.println("failure = " + this.getNumberOfDropouts());
    }

    @Override
    public void doStatistics(double delta) {
        meanQueueLengthAccumulator += queue.size() * delta;

        var numCustomersBeingProcessed
                = processors.stream().mapToInt(Processor::getState).sum();

        meanUtilizationAccumulator +=
                + (numCustomersBeingProcessed / (double)processors.size()) * delta;

        meanNumberOfCustomersInSystemAccumulator +=
                (queue.size() + numCustomersBeingProcessed) * delta;
        // TODO: change if states become not only 0 and 1
    }

    @Override
    public double getNextEventTime() {
        return processors.get(nextEventProcessorIndex).getNextEventTime();
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
            nextEventTime = parentQSystem.getCurrentTime()
                    + parentQSystem.getServiceTime(customer);
            customerBeingServed = customer;
        }

    }

    /**
     * Represents the queue in front of processors in this QueueingSystem
     */
    public static interface QSQueue {

        boolean isFull();

        boolean isEmpty();

        int size();

        void enqueue(Customer customer);

        Customer takeFirst();

        Customer stealLast();

    }

    public static class LimitedQueue implements QSQueue {

        private final Deque<Customer> q = new LinkedList<>();
        @Getter private final int capacity;

        public LimitedQueue(int capacity) {
            this.capacity = capacity;
        }

        @Override
        public boolean isFull() {
            return q.size() >= capacity;
        }

        @Override
        public boolean isEmpty() {
            return q.isEmpty();
        }

        @Override
        public int size() {
            return q.size();
        }

        @Override
        public void enqueue(Customer customer) {
            if (isFull())
                throw new RuntimeException("Tried to add element to a full limited queue");

            q.addLast(customer);
        }

        @Override
        public Customer takeFirst() {
            return q.pollFirst();
        }

        @Override
        public Customer stealLast() {
            return q.pollLast();
        }

    }

    public static class InfiniteQueue extends LimitedQueue {

        public InfiniteQueue() {
            super(-1);
        }

        @Override
        public boolean isFull() {
            return false;
        }

    }

    /**
     * Represents a queue before QueueingSystem's processors that has
     * an infinite capacity and gives priority to certain customer types
     */
    public static class InfinitePriorityQueue implements QSQueue {

        private final PriorityQueue<Customer> q;

        public InfinitePriorityQueue(Map<Integer, Integer> typeToPriorityMap) {
            q = new PriorityQueue<>(
                    Comparator.comparingInt(customer -> typeToPriorityMap.get(customer.type())));
        }

        @Override
        public boolean isFull() {
            return false;
        }

        @Override
        public boolean isEmpty() {
            return q.isEmpty();
        }

        @Override
        public int size() {
            return q.size();
        }

        @Override
        public void enqueue(Customer customer) {
            q.add(customer);
        }

        @Override
        public Customer takeFirst() {
            return q.poll();
        }

        @Override
        public Customer stealLast() {
            return q.poll(); // can't really take last element
        }

    }

    public static class NoQueue implements QSQueue {

        @Override
        public boolean isFull() {
            return true;
        }

        @Override
        public boolean isEmpty() {
            return true;
        }

        @Override
        public int size() {
            return 0;
        }

        @Override
        public void enqueue(Customer customer) {
            throw new RuntimeException("Tried to place a customer into a no-queue");
        }

        @Override
        public Customer takeFirst() {
            throw new RuntimeException("Tried to take a customer out of a no-queue");
        }

        @Override
        public Customer stealLast() {
            throw new RuntimeException("Tried to take a customer out of a no-queue");
        }
    }

}
