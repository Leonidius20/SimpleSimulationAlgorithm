package ua.leonidius.queueing;

import ua.leonidius.queueing.distributions.*;
import ua.leonidius.queueing.elements.Element;
import ua.leonidius.queueing.elements.Sink;
import ua.leonidius.queueing.elements.Source;
import ua.leonidius.queueing.elements.branchings.ProbabilisticBranching;
import ua.leonidius.queueing.elements.systems.QueueingSystem;

import java.util.ArrayList;

public class Main {

    public static void main(String[] args) {
        var list = new ArrayList<Element>();

        var source = new Source(new ExponentialDistribution(2));
        list.add(source);

        // K1
        var k1 = new QueueingSystem(1,
                new ExponentialDistribution(0.6),
                new QueueingSystem.InfiniteQueue(), "K1");
        source.setNextElement(k1);
        list.add(k1);

        // Sink
        var sink = new Sink();
        list.add(sink);

        // K2
        var k2 = new QueueingSystem(1,
                new ExponentialDistribution(0.3),
                new QueueingSystem.InfiniteQueue(), "K2");
        k2.setNextElement(k1);
        list.add(k2);

        // K3
        var k3 = new QueueingSystem(1,
                new ExponentialDistribution(0.4),
                new QueueingSystem.InfiniteQueue(), "K3");
        k3.setNextElement(k1);
        list.add(k3);

        // K4
        var k4 = new QueueingSystem(2,
                new ExponentialDistribution(0.1),
                new QueueingSystem.InfiniteQueue(), "K4");
        k4.setNextElement(k1);
        list.add(k4);

        // branching
        var branching = new ProbabilisticBranching(
                new Element[]{sink, k4, k2, k3},
                new double[]{0.42, 0.3, 0.15, 0.13}
        );
        k1.setNextElement(branching);

        QueueingModel model = new QueueingModel(list);

        long lengthAcc = 0;
        double atomTimeEstAcc = 0;

        for (int i = 0; i < 10; i++) {
            var beforeTimestamp = System.currentTimeMillis();
            int numOfEvents = model.simulate(100000.0);
            System.out.println("events " + numOfEvents);

            var afterTimestamp = System.currentTimeMillis();

            long lengthInMillis = afterTimestamp - beforeTimestamp;
            lengthAcc += lengthInMillis;
            double atomTimeEst = lengthInMillis / (numOfEvents * 7.0);
            atomTimeEstAcc += atomTimeEst;
        }

        System.out.println("TIME: " + lengthAcc / 10 + "ms");
        System.out.println("Elementaer time est " + atomTimeEstAcc / 10);
    }

}
