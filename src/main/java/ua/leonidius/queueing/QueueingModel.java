package ua.leonidius.queueing;

import ua.leonidius.queueing.elements.Element;
import ua.leonidius.queueing.elements.QueueingSystem;

import java.util.ArrayList;

public class QueueingModel {

    private final ArrayList<Element> list;

    double nextEventTime = 0.0, currentTime = 0.0;

    int event = 0;

    public QueueingModel(ArrayList<Element> elements) {
        list = elements;
    }

    public void simulate(double time) {
        while (currentTime < time) {
            nextEventTime = Double.MAX_VALUE;
            for (Element e : list) {
                if (e.getNextEventTime() < nextEventTime) {
                    nextEventTime = e.getNextEventTime();
                    event = e.getId();
                }
            }
            System.out.println("\nIt's time for event in " +
                    list.get(event).getName() +
                    ", time = " + nextEventTime);
            for (Element e : list) {
                e.doStatistics(nextEventTime - currentTime);
            }
            currentTime = nextEventTime;
            for (Element e : list) {
                e.setCurrentTime(currentTime);
            }
            list.get(event).onServiceCompletion();
            for (Element e : list) {
                if (e.getNextEventTime() == currentTime) {
                    e.onServiceCompletion();
                }
            }
            printInfo();
        }
        printResult();
    }
    public void printInfo() {
        for (Element e : list) {
            e.printInfo();
        }
    }
    public void printResult() {
        System.out.println("\n-------------RESULTS-------------");
        for (Element e : list) {
            e.printResult();
            if (e instanceof QueueingSystem qSystem) {
                System.out.println("mean length of queue = " +
                        qSystem.getMeanQueueLength() / currentTime
                        + "\nfailure probability = " +
                        qSystem.getNumberOfDropouts() / (double) qSystem.getNumberOfCustomersServed());
            }
        }
    }

}
