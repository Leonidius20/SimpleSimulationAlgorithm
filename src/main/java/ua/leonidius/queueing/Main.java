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
        // queueingSystem1.setCurrentQueueLength(2);

        QueueingSystem queueingSystem2 = new QueueingSystem(1,
                1, 3, ProbabilityDistribution.GAUSSIAN, "CASHIER 2");
        queueingSystem2.setServiceTimeStdDeviation(0.3);
        // queueingSystem2.setCurrentQueueLength(2);

        queueingSystem1.setTwinQSystem(queueingSystem2);
        queueingSystem2.setTwinQSystem(queueingSystem1);

        var dispose = new Dispose();

        queueingSystem1.setNextElement(dispose);
        queueingSystem2.setNextElement(dispose);

        var branching = new PriorityBranching(new QueueingSystem[]{ queueingSystem1, queueingSystem2  }, new int[] { 1, 2 });

        creationElement.setNextElement(branching);

        ArrayList<Element> list = new ArrayList<>();
        list.add(creationElement);
        list.add(queueingSystem1);
        list.add(queueingSystem2);
        list.add(dispose);

        QueueingModel model = new QueueingModel(list);
        model.simulate(1000.0);

        // ) середній час перебування клієнта в банку - сума часів. коли елемент вийшов з системи мінус сума часів, коли війшов всистему (окрім дропауьів) поділити на всю кількість оброблених додіків
        // середній інтервал часу між від'їздами клієнтів від вікон - в кожній СМО сумувати інтервали між івентами та потім поділити їх на кількість оброблдених додіків
    }

}
