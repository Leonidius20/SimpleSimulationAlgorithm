package ua.leonidius.queueing;

import ua.leonidius.queueing.distributions.ExponentialDistribution;
import ua.leonidius.queueing.distributions.NormalDistribution;
import ua.leonidius.queueing.elements.*;
import ua.leonidius.queueing.elements.branchings.TypeBranching;
import ua.leonidius.queueing.elements.systems.QueueingSystem;

import java.util.ArrayList;
import java.util.HashMap;

public class Main {

    public static void main(String[] args) {
        var creationElement = new Source(new ExponentialDistribution(0.5),
                new int[]{1, 2, 3}, new double[]{0.5, 0.1, 0.4});

        var qs1Transformations = new HashMap<Integer, Integer>();
        qs1Transformations.put(3, 1);

        QueueingSystem queueingSystem1 = new QueueingSystem(1,
                new NormalDistribution(1, 0.3),
                new QueueingSystem.LimitedQueue(3), "CASHIER 1");

        // queueingSystem1.setCurrentQueueLength(2);

        QueueingSystem queueingSystem2 = new QueueingSystem(2,
                new NormalDistribution(1, 0.3),
                new QueueingSystem.LimitedQueue(3), "CASHIER 2");
        // queueingSystem2.setCurrentQueueLength(2);

        //queueingSystem1.setTwinQSystem(queueingSystem2);
        //queueingSystem2.setTwinQSystem(queueingSystem1);

        var dispose = new Sink();

        queueingSystem1.setNextElement(dispose);
        queueingSystem2.setNextElement(dispose);

        var branchingRules = new HashMap<Integer, Element>();
        branchingRules.put(1, queueingSystem1);
        branchingRules.put(2, queueingSystem1);
        branchingRules.put(3, queueingSystem2);
        var branching = new TypeBranching(branchingRules);

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
