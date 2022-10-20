package ua.leonidius.queueing;

import ua.leonidius.queueing.beans.input_params.InputParameters;
import ua.leonidius.queueing.beans.input_params.QSystemParameters;
import ua.leonidius.queueing.elements.*;
import ua.leonidius.queueing.utils.ProbabilityDistribution;

import java.util.ArrayList;
import java.util.LinkedList;

public class Main {

    public static void main(String[] args) {


        double baseCreationDelay = 0.6;
        double baseServingTime = 1.2;
        int baseQCapacity = 5;

        Create creationElement = new Create(baseCreationDelay);
        QueueingSystem queueingSystem1 = new QueueingSystem(2,
               baseServingTime, baseQCapacity, ProbabilityDistribution.EXPONENTIAL, "PROCESSOR 1");


        QueueingSystem queueingSystem2 = new QueueingSystem(3,
                baseServingTime, baseQCapacity, ProbabilityDistribution.EXPONENTIAL, "PROCESSOR 2");
        Dispose dispose = new Dispose();

        Branching branching = new Branching(new Element[]{ queueingSystem1, dispose  }, new double[] {0.6, 0.4});

        creationElement.setNextElement(queueingSystem1);
        queueingSystem1.setNextElement(branching);

        ArrayList<Element> list = new ArrayList<>();
        list.add(creationElement);
        list.add(queueingSystem1);
        // list.add(queueingSystem2);

        QueueingModel model = new QueueingModel(list);
        var out = model.simulate(1000.0);

        System.out.println("totalNumCustomers, totalDropoutProbability, numDropouts1, meanQLength1, meanUtilization1, numDropouts2, meanQLength2, meanUtilization2\n" /*, numDropouts3, meanQLength3, meanUtilization3\n"*/);

        System.out.println(out.toString());

    }

}
