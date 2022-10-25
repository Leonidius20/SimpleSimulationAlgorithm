package ua.leonidius.queueing.elements;

import ua.leonidius.queueing.utils.ProbabilityDistribution;

public class Create extends Element {

    public Create(ProbabilityDistribution distribution, double[] distParams) {
        super(distribution, distParams);
    }

    @Override
    public void onServiceCompletion() {
        super.onServiceCompletion();
        super.setNextEventTime(super.getCurrentTime() + super.getServiceTime());
        super.getNextElement().onCustomerArrival();
    }

}
