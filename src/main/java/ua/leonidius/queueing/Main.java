package ua.leonidius.queueing;

import ua.leonidius.queueing.elements.*;
import ua.leonidius.queueing.utils.ProbabilityDistribution;

import java.util.ArrayList;

public class Main {

    public static void main(String[] args) {
        var creationElement = new Create(ProbabilityDistribution.EXPONENTIAL, new double[]{0.5});
        creationElement.setNextEventTime(0.1);

        QueueingSystem queueingSystem1 = new QueueingSystem(2,
                1, 3, ProbabilityDistribution.GAUSSIAN, "CASHIER 1");
        queueingSystem1.setServiceTimeStdDeviation(0.3);
        queueingSystem1.setCurrentQueueLength(2);

        QueueingSystem queueingSystem2 = new QueueingSystem(1,
                1, 3, ProbabilityDistribution.GAUSSIAN, "CASHIER 2");
        queueingSystem2.setServiceTimeStdDeviation(0.3);
        queueingSystem2.setCurrentQueueLength(2);

        queueingSystem1.setTwinQSystem(queueingSystem2);
        queueingSystem2.setTwinQSystem(queueingSystem1);

        var branching = new PriorityBranching(new QueueingSystem[]{ queueingSystem1, queueingSystem2  }, new int[] { 1, 2 });

        creationElement.setNextElement(branching);

        ArrayList<Element> list = new ArrayList<>();
        list.add(creationElement);
        list.add(queueingSystem1);
        list.add(queueingSystem2);

        QueueingModel model = new QueueingModel(list);
        var out = model.simulate(1000.0);

        System.out.println("totalNumCustomers, totalDropoutProbability, numDropouts1, meanQLength1, meanUtilization1, numDropouts2, meanQLength2, meanUtilization2\n"/*, numDropouts3, meanQLength3, meanUtilization3\n"*/);

        System.out.println(out.toString());

    }

}
