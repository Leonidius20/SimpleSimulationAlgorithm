package ua.leonidius.queueing.elements;

import lombok.Getter;
import ua.leonidius.queueing.Customer;

/**
 * The end element of the model, where customers are destroyed. Can be used to
 * gather statistics at the end of the model.
 */
public class Sink extends Element {

    /**
     *  An accumulator of timestamps at which customers arrive to this element
     *  Used to calculate mean time clients spend in the system.
     */
    // @Getter private double customerArrivalTimesAccumulator;

    @Getter private double avgTimeInSystemAcc;

    /**
     * Accumulates times between customers leaving the system.
     * Needed to calculate the avg time between customers leaving the system.
     */
    @Getter private double timesBetweenLeavingAccumulator;

    /**
     * Timestamp of when the last served customer left the  system.
     * Needed to calculate the avg time between customers leaving the system.
     */
    private double lastLeavingTimestamp = -1;

    public Sink() {
        this.generatesEvents = false;
        setNextEventTime(Double.MAX_VALUE); // no events, although it's not added to model anyway
    }

    @Override
    public void onCustomerArrival(Customer customer) {
        // customerArrivalTimesAccumulator += getCurrentTime();
        avgTimeInSystemAcc += (getCurrentTime() - customer.creationTime());

        if (lastLeavingTimestamp != -1) { // if this is the first time a customer leaves the system
            timesBetweenLeavingAccumulator += (getCurrentTime() - lastLeavingTimestamp);
        }
        lastLeavingTimestamp = getCurrentTime();

        // TODO: maybe actaully amke it a part of the event system
        // with end event happening right away
        numberOfCustomersServed++;
    }

}
