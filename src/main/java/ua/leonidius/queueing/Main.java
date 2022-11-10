package ua.leonidius.queueing;

import ua.leonidius.queueing.distributions.*;
import ua.leonidius.queueing.elements.Element;
import ua.leonidius.queueing.elements.Sink;
import ua.leonidius.queueing.elements.Source;
import ua.leonidius.queueing.elements.systems.QueueingSystem;

import java.util.ArrayList;

public class Main {

    public static void main(String[] args) {
        int NUM_OF_ELEMENTARY_OPERATIONS = 7;

        System.out.println("numOfSystems,estimatedTime,realTime");

        for (int numberOfSystems = 100; numberOfSystems <= 1000; numberOfSystems += 100) {

            ArrayList<Element> list = new ArrayList<>();

            // SOURCE
            var source = new Source(new ExponentialDistribution(0.6));
            list.add(source);

            Element prevElement = source;
            for (int i = 0; i < numberOfSystems; i++) {
                var qSystem = new QueueingSystem(1,
                        new ExponentialDistribution(0.4),
                        new QueueingSystem.InfiniteQueue(),
                        "Q System" + (i + 1));
                prevElement.setNextElement(qSystem);
                list.add(qSystem);
                prevElement = qSystem;
            }

            // SINK
            var sink = new Sink();
            prevElement.setNextElement(sink);
            list.add(sink);

            QueueingModel model = new QueueingModel(list);

            var beforeTimestamp = System.currentTimeMillis();
            model.simulate(10000.0);
            var afterTimestamp = System.currentTimeMillis();

            long lengthInMillis = afterTimestamp - beforeTimestamp;

            double predictedTime
                    = numberOfSystems * NUM_OF_ELEMENTARY_OPERATIONS;

            System.out.println(numberOfSystems + "," + predictedTime + "," + lengthInMillis);
        }
    }

}
