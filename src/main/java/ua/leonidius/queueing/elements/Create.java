package ua.leonidius.queueing.elements;

public class Create extends Element {

    public Create(double delay) {
        super(delay);
    }
    @Override
    public void onServiceCompletion() {
        super.onServiceCompletion();
        super.setNextEventTime(super.getCurrentTime() + super.getServiceTime());
        super.getNextElement().onCustomerArrival();
    }

}
