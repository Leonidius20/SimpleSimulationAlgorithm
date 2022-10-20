package ua.leonidius.queueing.elements;

import java.util.Arrays;
import java.util.Random;

public class Branching extends Element {

    private final Element[] elements;
    private final double[] probabilities;
    private final Random randomG = new Random();

    public Branching(Element[] elements, double[] probabilities) {
        if (elements.length != probabilities.length) {
            throw new RuntimeException("Number of elements and probabilities supplied do not match");
        }

        if (Arrays.stream(probabilities).sum() != 1) {
            throw new RuntimeException("Probabilities do not add up to 1");
        }

        for (int i = 0; i < probabilities.length - 1; i++) {
            if (probabilities[i+1] > probabilities[i]) {
                throw new RuntimeException("Probabilities should be sorted from highest to lowest");
            }
        }

        this.setNextEventTime(Double.MAX_VALUE); // do not perform events

        this.elements = elements;
        this.probabilities = probabilities;

    }

    @Override
    public void onCustomerArrival() {
        super.onCustomerArrival(); // it's empty anyway

        Element nextElement = null;

        // selecting path
        double x = randomG.nextDouble();
        for (int i = 0; i < probabilities.length; ++i) {
            x -= probabilities[i];
            if (x <= 0) {
                nextElement = elements[i];
                break;
            }
        }
        if (nextElement == null) nextElement = elements[elements.length-1];


        // going that path
        nextElement.onCustomerArrival();
    }
}
