package ua.leonidius.queueing.elements;

import lombok.Getter;

/**
 * The end element of the model, where customers are destroyed. Can be used to
 * gather statistics at the end of the model.
 */
public class Dispose extends Element {

    /**
     *  An accumulator of timestamps at which customers arrive to this elemet
     *  Used to calculate mean time clients spend in the system.
     */
    @Getter private double customerArrivalTimesAccumulator;

    public Dispose() {
        this.generatesEvents = false;
        setNextEventTime(Double.MAX_VALUE); // no events, although it's not added to model anyway
    }

    @Override
    public void onCustomerArrival() {
        customerArrivalTimesAccumulator += getCurrentTime();
    }

}
