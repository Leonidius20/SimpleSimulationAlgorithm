package ua.leonidius.queueing;

import ua.leonidius.queueing.beans.output_params.OutputParameters;
import ua.leonidius.queueing.beans.output_params.QSystemPerformanceMetrics;
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

    public OutputParameters simulate(double simulationTime) {
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

        return getFinalResult();
    }

    public void printIterationInfo() {
        listOfElements.forEach(Element::printInfo);
    }

    public OutputParameters getFinalResult() {
        System.out.println("\n-------------RESULTS-------------");


        var createElement = listOfElements.get(0);
        int totalNumCustomers = createElement.getNumberOfCustomersServed();

        double[] meanQLengths = new double[3];
        double[] meanUtilizations = new double[3];
        int[] dropoutNumbers = new int[3];

        int totalDropouts = 0;
        int totalCustomersServed = 0;

        for (int i = 1; i < listOfElements.size(); i++) {
            var qSystem = (QueueingSystem) listOfElements.get(i);

            meanQLengths[i - 1] = qSystem.getMeanQueueLengthAccumulator() / currentTime;
            meanUtilizations[i - 1] = qSystem.getMeanUtilizationAccumulator() / currentTime;
            dropoutNumbers[i - 1] = qSystem.getNumberOfDropouts();

            totalDropouts += qSystem.getNumberOfDropouts();
            totalCustomersServed += qSystem.getNumberOfCustomersServed();

            double dropoutProbability = qSystem.getNumberOfDropouts()
                    / (double) (qSystem.getNumberOfCustomersServed() + qSystem.getNumberOfDropouts());
        }

        double totalDropoutProbability = totalDropouts
                / (double) (totalCustomersServed + totalDropouts);

        return new OutputParameters(totalNumCustomers, totalDropoutProbability, new QSystemPerformanceMetrics[]{
                new QSystemPerformanceMetrics(dropoutNumbers[0], meanQLengths[0], meanUtilizations[0]),
                new QSystemPerformanceMetrics(dropoutNumbers[1], meanQLengths[1], meanUtilizations[1]),
                // new QSystemPerformanceMetrics(dropoutNumbers[2], meanQLengths[2], meanUtilizations[2]),
        });
    }

}
