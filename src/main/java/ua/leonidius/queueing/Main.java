package ua.leonidius.queueing;

import ua.leonidius.queueing.elements.Create;
import ua.leonidius.queueing.elements.Element;
import ua.leonidius.queueing.elements.QueueingSystem;
import ua.leonidius.queueing.utils.ProbabilityDistribution;

import java.util.ArrayList;

public class Main {

    public static void main(String[] args) {
        Create c = new Create(2.0);
        QueueingSystem p = new QueueingSystem(1.0);
        System.out.println("id0 = " + c.getId() + " id1=" +
                p.getId());
        c.setNextElement(p);
        p.setQueueCapacity(5);
        c.setName("CREATOR");
        p.setName("PROCESSOR");
        c.setDistribution(ProbabilityDistribution.EXPONENTIAL);
        p.setDistribution(ProbabilityDistribution.EXPONENTIAL);
        ArrayList<Element> list = new ArrayList<>();
        list.add(c);
        list.add(p);
        QueueingModel model = new QueueingModel(list);
        model.simulate(1000.0);
    }

}
