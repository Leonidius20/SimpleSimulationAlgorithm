package ua.leonidius.queueing.elements;

import java.util.ArrayList;

public class PriorityBranching extends Element {

    private final QueueingSystem[] elements;
    private final int[] priorities;

    public PriorityBranching(QueueingSystem[] elements, int[] priorities) {
        if (elements.length != priorities.length) {
            throw new RuntimeException("Number of elements and priorities supplied do not match");
        }

        if (elements.length == 0) {
            throw new RuntimeException("There should be at least 1 path in branching");
        }

        this.setNextEventTime(Double.MAX_VALUE); // do not perform events

        this.elements = elements;
        this.priorities = priorities;
    }

    @Override
    public void onCustomerArrival() {
        super.onCustomerArrival(); // it's empty anyway

        // finding the q systems with the shortest queues
        int minQLength = elements[0].getCurrentQueueLength();
        ArrayList<Integer> indiciesOfElementsWithMinQLength = new ArrayList<>();
        indiciesOfElementsWithMinQLength.add(0);

        for (int i = 1; i < elements.length; i++) {
            if (elements[i].getCurrentQueueLength() < minQLength) {
                minQLength = elements[i].getCurrentQueueLength();
                indiciesOfElementsWithMinQLength.clear();
                indiciesOfElementsWithMinQLength.add(i);
            } else if (elements[i].getCurrentQueueLength() == minQLength && i != 0) {
                indiciesOfElementsWithMinQLength.add(i);
            }
        }

        // selecting the path out of paths with shortest queues based on priority
        int bestIndex = indiciesOfElementsWithMinQLength.get(0);
        int bestPriority = priorities[bestIndex]; // the smaller the better

        for (int elementIndex : indiciesOfElementsWithMinQLength) {
            if (priorities[elementIndex] < bestPriority) {
                bestPriority = priorities[elementIndex];
                bestIndex = elementIndex;
            }
        }

        /// going that path
        elements[bestIndex].onCustomerArrival();
    }

}
