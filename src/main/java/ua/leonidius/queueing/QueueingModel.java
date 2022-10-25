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

        double meanNumOfCustomersInModel = 0;
        int totalRefugees = 0;

        for (int i = 1; i < listOfElements.size(); i++) {
            var qSystem = (QueueingSystem) listOfElements.get(i);

            meanQLengths[i - 1] = qSystem.getMeanQueueLengthAccumulator() / currentTime;
            meanUtilizations[i - 1] = qSystem.getMeanUtilizationAccumulator() / currentTime;
            dropoutNumbers[i - 1] = qSystem.getNumberOfDropouts();

            totalDropouts += qSystem.getNumberOfDropouts();
            totalCustomersServed += qSystem.getNumberOfCustomersServed();

            double dropoutProbability = qSystem.getNumberOfDropouts()
                    / (double) (qSystem.getNumberOfCustomersServed() + qSystem.getNumberOfDropouts());

            meanNumOfCustomersInModel +=
                    ((double)qSystem.getMeanNumberOfCustomersInSystemAccumulator() / currentTime);
            totalRefugees += qSystem.getNumberOfRefugees();
        }

        double totalDropoutProbability = totalDropouts
                / (double) (totalCustomersServed + totalDropouts);

        System.out.println("totalNumCustomersGenerated, meanNumberOfClientsInSystem, meanLeavingInverval, meanTimeInSystem, totalDropoutProbability, totalRefugees, meanQLength1, meanUtilization1, meanQLength2, meanUtilization2\n"/*, numDropouts3, meanQLength3, meanUtilization3\n"*/);
        var sb = new StringBuilder();
        sb.append(totalNumCustomers).append(',');
        sb.append(meanNumOfCustomersInModel).append(','); // TODO: how is it 12 when there are only 8 max possible?
        sb.append("not implemented").append(',');
        sb.append("not implemented").append(',');
        sb.append(totalDropoutProbability).append(',');
        sb.append(totalRefugees).append(',');
        sb.append(meanQLengths[0]).append(',');
        sb.append(meanUtilizations[0]).append(',');
        sb.append(meanQLengths[1]).append(',');
        sb.append(meanUtilizations[1]).append(',');
        System.out.println(sb.toString());

        return null;
    }

}
