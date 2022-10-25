package ua.leonidius.queueing.elements;

import lombok.Getter;

/**
 * The end element of the model, where customers are destroyed. Can be used to
 * gather statistics at the end of the model.
 */
public class Dispose extends Element {

    /**
     *  An accumulator of timestamps at which customers arrive to this element
     *  Used to calculate mean time clients spend in the system.
     */
    @Getter private double customerArrivalTimesAccumulator;

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

    public Dispose() {
        this.generatesEvents = false;
        setNextEventTime(Double.MAX_VALUE); // no events, although it's not added to model anyway
    }

    @Override
    public void onCustomerArrival() {
        customerArrivalTimesAccumulator += getCurrentTime();

        if (lastLeavingTimestamp != -1) { // if this is the first time a customer leaves the system
            timesBetweenLeavingAccumulator += (getCurrentTime() - lastLeavingTimestamp);
        }
        lastLeavingTimestamp = getCurrentTime();

        // TODO: maybe actaully amke it a part of the event system
        // with end event happening right away
        setNumberOfCustomersServed(getNumberOfCustomersServed() + 1);
    }

}
