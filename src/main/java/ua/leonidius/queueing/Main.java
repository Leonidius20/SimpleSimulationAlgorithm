package ua.leonidius.queueing;

import ua.leonidius.queueing.distributions.ExponentialDistribution;
import ua.leonidius.queueing.distributions.NormalDistribution;
import ua.leonidius.queueing.elements.*;

import java.util.ArrayList;

public class Main {

    public static void main(String[] args) {
        var creationElement = new Create(new ExponentialDistribution(15),
                new int[]{1, 2, 3}, new double[]{0.5, 0.1, 0.4});


        QueueingSystem queueingSystem1 = new QueueingSystem(2,
                new NormalDistribution(1, 0.3),
                new QueueingSystem.LimitedQueue(3), "CASHIER 1");

        // queueingSystem1.setCurrentQueueLength(2);

        QueueingSystem queueingSystem2 = new QueueingSystem(2,
                new NormalDistribution(1, 0.3),
                new QueueingSystem.LimitedQueue(3), "CASHIER 2");
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
