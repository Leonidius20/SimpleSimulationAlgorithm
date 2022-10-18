package ua.leonidius.queueing;

import ua.leonidius.queueing.beans.input_params.InputParameters;
import ua.leonidius.queueing.beans.input_params.QSystemParameters;
import ua.leonidius.queueing.elements.Create;
import ua.leonidius.queueing.elements.Element;
import ua.leonidius.queueing.elements.QueueingSystem;
import ua.leonidius.queueing.utils.ProbabilityDistribution;

import java.util.ArrayList;

public class Main {

    public static void main(String[] args) {
        Create creationElement = new Create(1.2);

        var inputParams = new InputParameters(3, new QSystemParameters[]{

        });

        QueueingSystem queueingSystem1 = new QueueingSystem(
                1.0, 5, ProbabilityDistribution.EXPONENTIAL, "PROCESSOR 1");
        QueueingSystem queueingSystem2 = new QueueingSystem(
                1.0, 5, ProbabilityDistribution.EXPONENTIAL, "PROCESSOR 2");
        QueueingSystem queueingSystem3 = new QueueingSystem(
                1.0, 5, ProbabilityDistribution.EXPONENTIAL, "PROCESSOR 3");

        creationElement.setNextElement(queueingSystem1);
        queueingSystem1.setNextElement(queueingSystem2);
        queueingSystem2.setNextElement(queueingSystem3);

        ArrayList<Element> list = new ArrayList<>();
        list.add(creationElement);
        list.add(queueingSystem1);
        list.add(queueingSystem2);
        list.add(queueingSystem3);

        QueueingModel model = new QueueingModel(list);
        model.simulate(1000.0);
    }

}
