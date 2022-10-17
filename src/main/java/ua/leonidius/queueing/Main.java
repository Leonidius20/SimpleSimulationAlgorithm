package ua.leonidius.queueing;

import ua.leonidius.queueing.elements.Create;
import ua.leonidius.queueing.elements.Element;
import ua.leonidius.queueing.elements.QueueingSystem;
import ua.leonidius.queueing.utils.ProbabilityDistribution;

import java.util.ArrayList;

public class Main {

    public static void main(String[] args) {
        Create creationElement = new Create(1.2);
        QueueingSystem queueingSystem = new QueueingSystem(1.0);

        System.out.println("id0 = " + creationElement.getId() + " id1=" +
                queueingSystem.getId());

        creationElement.setNextElement(queueingSystem);
        creationElement.setName("CREATOR");
        creationElement.setDistribution(ProbabilityDistribution.EXPONENTIAL);

        queueingSystem.setQueueCapacity(5);
        queueingSystem.setName("PROCESSOR");
        queueingSystem.setDistribution(ProbabilityDistribution.EXPONENTIAL);

        ArrayList<Element> list = new ArrayList<>();
        list.add(creationElement);
        list.add(queueingSystem);
        QueueingModel model = new QueueingModel(list);
        model.simulate(1000.0);
    }

}
