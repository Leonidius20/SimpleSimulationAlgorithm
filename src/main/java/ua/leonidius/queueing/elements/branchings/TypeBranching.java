package ua.leonidius.queueing.elements.branchings;

import ua.leonidius.queueing.Customer;
import ua.leonidius.queueing.elements.Element;

import java.util.Map;

public class TypeBranching extends Element {

    private final Map<Integer, Element> rules;

    public TypeBranching(Map<Integer, Element> rules) {
        this.rules = rules;
    }

    @Override
    public void onCustomerArrival(Customer customer) {
        if (!rules.containsKey(customer.type()))
            throw new RuntimeException("Rule set doesn't contain branching rule for type " + customer.type());

        var next = rules.get(customer.type());
        next.onCustomerArrival(customer);
    }

}
