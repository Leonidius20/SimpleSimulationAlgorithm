package ua.leonidius.queueing;

import ua.leonidius.queueing.elements.Source;
import ua.leonidius.queueing.elements.Sink;
import ua.leonidius.queueing.elements.Element;
import ua.leonidius.queueing.elements.systems.QueueingSystem;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;

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
                            .filter(Element::isGeneratesEvents)
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
                if (e.isGeneratesEvents() && e.getNextEventTime() == currentTime) {
                    e.onServiceCompletion();
                }
            }

            printIterationInfo();
        }

        getFinalResult();
    }

    public void printIterationInfo() {
        listOfElements.forEach(Element::printInfo);
    }

    public void getFinalResult() {
        System.out.println("\n-------------RESULTS-------------");


        var createElement = (Source)listOfElements.stream()
                .filter(e -> e instanceof Source).findFirst().get();
        int totalNumCustomersCreated = createElement.getNumberOfCustomersServed();
        // double customerArrivalTimesAcc = createElement.getCustomerEnterTimesAccumulator();

        var disposeElement = (Sink)listOfElements.stream()
                .filter(e -> e instanceof Sink).findFirst().get();
        // double disposeTimesAcc = disposeElement.getCustomerArrivalTimesAccumulator();

        // double dropoutTimesAcc = 0;

        List<Double> meanQLengths = new LinkedList<>();
        List<Double> meanUtilizations =  new LinkedList<>();
        List<Integer> dropoutNumbers = new LinkedList<>();

        int totalDropouts = 0;
        int totalCustomersServed = 0;

        double meanNumOfCustomersInModel = 0;
        int totalRefugees = 0;

        var qSystems = listOfElements.stream()
                .filter(e -> e instanceof QueueingSystem)
                .map(e -> (QueueingSystem)e)
                .sorted(Comparator.comparingInt(QueueingSystem::getId))
                .toList();

        for (var qSystem : qSystems) {
            meanQLengths.add(qSystem.getMeanQueueLengthAccumulator() / currentTime);
            meanUtilizations.add(qSystem.getMeanUtilizationAccumulator() / currentTime);
            dropoutNumbers.add(qSystem.getNumberOfDropouts());

            totalDropouts += qSystem.getNumberOfDropouts();
            totalCustomersServed += qSystem.getNumberOfCustomersServed();

            //double dropoutProbability = qSystem.getNumberOfDropouts()
            //        / (double) (qSystem.getNumberOfCustomersServed() + qSystem.getNumberOfDropouts());

            meanNumOfCustomersInModel +=
                    ((double)qSystem.getMeanNumberOfCustomersInSystemAccumulator() / currentTime);
            totalRefugees += qSystem.getNumberOfRefugees();

            double avgTimeBetweenArrivals = qSystem.getTimesBetweenArrivalsAcc()
                    / qSystem.getNumberOfCustomersServed();

            System.out.println("SYSTEM " + qSystem.getName());
            System.out.println("Avg between arrivals " + avgTimeBetweenArrivals);
            System.out.println();

            // dropoutTimesAcc += qSystem.getDropoutTimestampsAccumulator();
        }

        double totalDropoutProbability = totalDropouts
                / (double) (totalCustomersServed + totalDropouts);

        double meanTimeInSystem = disposeElement.getAvgTimeInSystemAcc()
                / disposeElement.getNumberOfCustomersServed();

        double meanLeavingInterval = disposeElement.getTimesBetweenLeavingAccumulator()
                / disposeElement.getNumberOfCustomersServed();

        System.out.println("totalNumCustomersGenerated, meanNumberOfClientsInSystem, meanLeavingInterval, meanTimeInSystem, totalDropoutProbability, totalRefugees, meanQLength1, meanQLength2, meanUtilization1, meanUtilization2\n"/*, numDropouts3, meanQLength3, meanUtilization3\n"*/);
        var sb = new StringBuilder();
        sb.append(totalNumCustomersCreated).append(',');
        sb.append(meanNumOfCustomersInModel).append(',');
        sb.append(meanLeavingInterval).append(',');
        sb.append(meanTimeInSystem).append(',');
        sb.append(totalDropoutProbability).append(',');
        sb.append(totalRefugees).append(',');

        for (var meanQLength : meanQLengths) {
            sb.append(meanQLength).append(',');
        }
        for (var utilization : meanUtilizations) {
            sb.append(utilization).append(',');
        }

        System.out.println(sb);
    }

}
