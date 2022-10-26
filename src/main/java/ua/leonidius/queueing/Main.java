package ua.leonidius.queueing;

import ua.leonidius.queueing.distributions.*;
import ua.leonidius.queueing.elements.Element;
import ua.leonidius.queueing.elements.Sink;
import ua.leonidius.queueing.elements.Source;
import ua.leonidius.queueing.elements.branchings.TypeBranching;
import ua.leonidius.queueing.elements.systems.InfiniteProcessorsQSystem;
import ua.leonidius.queueing.elements.systems.QueueingSystem;

import java.util.ArrayList;
import java.util.HashMap;

public class Main {

    public static void main(String[] args) {
        // SOURCE
        var source = new Source(new ExponentialDistribution(15),
                new int[]{1, 2, 3}, new double[]{0.5, 0.1, 0.4});

        // RECEPTION (K1)
        var doctorsOnReceptionProbabilitiesMap = new HashMap<Integer, ProbabilityDistribution>();
        doctorsOnReceptionProbabilitiesMap.put(1, new ConstantValue(15));
        doctorsOnReceptionProbabilitiesMap.put(2, new ConstantValue(40));
        doctorsOnReceptionProbabilitiesMap.put(3, new ConstantValue(30));
        doctorsOnReceptionProbabilitiesMap.put(4, new ConstantValue(15)); // 4 should be treated like 1

        var doctorsOnReceptionQPriorities = new HashMap<Integer, Integer>();
        doctorsOnReceptionQPriorities.put(1, 2);
        doctorsOnReceptionQPriorities.put(2, 2);
        doctorsOnReceptionQPriorities.put(3, 2);
        doctorsOnReceptionQPriorities.put(4, 1); // 4 is most important

        var doctorsOnReception = new QueueingSystem(2,
                doctorsOnReceptionProbabilitiesMap,
                new QueueingSystem.InfinitePriorityQueue(doctorsOnReceptionQPriorities),
                "RECEPTION");
        source.setNextElement(doctorsOnReception);

        // TRANSFER TO WARD
        var transferToWard = new QueueingSystem(3,
                new UniformDistribution(3, 8),
                new QueueingSystem.InfiniteQueue(),
                "TRANSFER TO WARD");

        // CORRIDOR FROM RECEPTION TO LAB
        var corridorFromReceptionToLab = new InfiniteProcessorsQSystem(
                new UniformDistribution(2, 5), "CORRIDOR FROM RECEPTION TO LAB"
        );

        // BRANCHING TO WARDS OR TO THE LAB
        var wardOrLabBranchingRules = new HashMap<Integer, Element>();
        wardOrLabBranchingRules.put(1, transferToWard);
        wardOrLabBranchingRules.put(2, corridorFromReceptionToLab);
        wardOrLabBranchingRules.put(3, corridorFromReceptionToLab);
        wardOrLabBranchingRules.put(4, transferToWard);

        var wardOrLabBranching = new TypeBranching(wardOrLabBranchingRules);
        doctorsOnReception.setNextElement(wardOrLabBranching);

        // LAB REGISTRATION
        var labRegistration = new QueueingSystem(1,
                new ErlangDistribution(4.5, 3),
                new QueueingSystem.InfiniteQueue(),
                "LAB REGISTRATION");
        corridorFromReceptionToLab.setNextElement(labRegistration);

        // LAB
        var lab = new QueueingSystem(2,
                new ErlangDistribution(4, 2),
                new QueueingSystem.InfiniteQueue(),
                "LAB");
        labRegistration.setNextElement(lab);

        // CORRIDOR FROM LAB BACK TO RECEPTION
        var backCorridorTransformationRules = new HashMap<Integer, Integer>();
        backCorridorTransformationRules.put(1, 1);
        backCorridorTransformationRules.put(2, 4);
        backCorridorTransformationRules.put(3, 3);
        backCorridorTransformationRules.put(4, 4);

        var corridorFromLabToReception = new InfiniteProcessorsQSystem(
                new UniformDistribution(2, 5),
                "CORRIDOR FROM LAB BACK TO RECEPTION",
                backCorridorTransformationRules);
        corridorFromLabToReception.setNextElement(doctorsOnReception);

        // SINK
        var sink = new Sink();
        transferToWard.setNextElement(sink);

        // BRANCHING AFTER LAB
        var branchingAfterLabRules = new HashMap<Integer, Element>();
        branchingAfterLabRules.put(3, sink);
        branchingAfterLabRules.put(2, corridorFromLabToReception); // other types shouldn't be here
        var branchingAfterLab = new TypeBranching(branchingAfterLabRules);
        lab.setNextElement(branchingAfterLab);

        ArrayList<Element> list = new ArrayList<>();
        list.add(source);
        list.add(doctorsOnReception);
        list.add(transferToWard);
        list.add(corridorFromReceptionToLab);
        list.add(labRegistration);
        list.add(lab);
        list.add(corridorFromLabToReception);
        list.add(sink);

        QueueingModel model = new QueueingModel(list);
        model.simulate(1000.0);
    }

}
