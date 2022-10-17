package ua.leonidius.queueing;

import ua.leonidius.queueing.elements.Element;
import ua.leonidius.queueing.elements.QueueingSystem;

import java.util.ArrayList;
import java.util.Comparator;

public class QueueingModel {

    private final ArrayList<Element> listOfElements;

    double nextEventTime = 0.0, currentTime = 0.0;

    int nextEventId = 0;

    public QueueingModel(ArrayList<Element> elements) {
        listOfElements = elements;
    }

    public void simulate(double simulationTime) {
        while (currentTime < simulationTime) {

            var nextEventElement =
                    listOfElements.stream()
                            .min(Comparator.comparingDouble(Element::getNextEventTime))
                            .get();
            nextEventTime = nextEventElement.getNextEventTime();
            nextEventId = nextEventElement.getId();

            System.out.println("\nIt's time for event in " +
                    nextEventElement.getName() +
                    ", id="+ nextEventId + ", time=" + nextEventTime);


            double delta = nextEventTime - currentTime;

            currentTime = nextEventTime;

            listOfElements.forEach(e -> {
                e.doStatistics(delta);
                e.setCurrentTime(currentTime);
            });

            nextEventElement.onServiceCompletion(); // it depends on updated currentTime

            for (Element e : listOfElements) {
                if (e.getNextEventTime() == currentTime) {
                    e.onServiceCompletion();
                }
            }

            printIterationInfo();
        }

        printFinalResult();
    }

    public void printIterationInfo() {
        listOfElements.forEach(Element::printInfo);
    }

    public void printFinalResult() {
        System.out.println("\n-------------RESULTS-------------");

        for (Element e : listOfElements) {
            e.printResult();
            if (e instanceof QueueingSystem qSystem) {
                double meanQueueLength = qSystem.getMeanQueueLengthAccumulator() / currentTime;
                double meanUtilization = qSystem.getMeanUtilizationAccumulator() / currentTime;
                double dropoutProbability = qSystem.getNumberOfDropouts()
                        / (double)qSystem.getNumberOfCustomersServed();

                System.out.println(
                        "mean length of queue = " + meanQueueLength
                                + "\nmean utilization = " + meanUtilization
                                + "\nfailure probability = " + dropoutProbability);
            }
        }
    }

}
