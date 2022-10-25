package ua.leonidius.queueing;

import ua.leonidius.queueing.elements.*;
import ua.leonidius.queueing.utils.ProbabilityDistribution;

import java.util.ArrayList;

public class Main {

    public static void main(String[] args) {


        double baseCreationDelay = 0.6;
        double baseServingTime = 1.2;
        int baseQCapacity = 5;

        Create creationElement = new Create(baseCreationDelay);
        QueueingSystem queueingSystem1 = new QueueingSystem(2,
               baseServingTime, baseQCapacity, ProbabilityDistribution.EXPONENTIAL, "PROCESSOR 1");


        QueueingSystem queueingSystem2 = new QueueingSystem(1,
                baseServingTime, baseQCapacity, ProbabilityDistribution.EXPONENTIAL, "PROCESSOR 2");
        QueueingSystem queueingSystem3 = new QueueingSystem(1,
                baseServingTime, baseQCapacity, ProbabilityDistribution.EXPONENTIAL, "PROCESSOR 3");
        Dispose dispose = new Dispose();

        var branching = new PriorityBranching(new QueueingSystem[]{ queueingSystem2, queueingSystem3  }, new int[] { 1, 2 });

        creationElement.setNextElement(queueingSystem1);
        queueingSystem1.setNextElement(branching);

        ArrayList<Element> list = new ArrayList<>();
        list.add(creationElement);
        list.add(queueingSystem1);
        list.add(queueingSystem2);
        list.add(queueingSystem3);

        QueueingModel model = new QueueingModel(list);
        var out = model.simulate(1000.0);

        System.out.println("totalNumCustomers, totalDropoutProbability, numDropouts1, meanQLength1, meanUtilization1, numDropouts2, meanQLength2, meanUtilization2, numDropouts3, meanQLength3, meanUtilization3\n");

        System.out.println(out.toString());

    }

}
