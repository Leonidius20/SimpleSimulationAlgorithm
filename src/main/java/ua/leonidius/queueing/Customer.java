package ua.leonidius.queueing;

public record Customer(int type, double creationTime) {

    public Customer copy() {
        return new Customer(type, creationTime);
    }

}
