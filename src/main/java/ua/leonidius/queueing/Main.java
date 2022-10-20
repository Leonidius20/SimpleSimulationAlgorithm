package ua.leonidius.queueing;

import ua.leonidius.queueing.beans.input_params.InputParameters;
import ua.leonidius.queueing.beans.input_params.QSystemParameters;
import ua.leonidius.queueing.elements.Create;
import ua.leonidius.queueing.elements.Element;
import ua.leonidius.queueing.elements.QueueingSystem;
import ua.leonidius.queueing.utils.ProbabilityDistribution;

import java.util.ArrayList;
import java.util.LinkedList;

public class Main {

    public static void main(String[] args) {


        double baseCreationDelay = 1.2;
        double baseServingTime = 1.0;
        int baseQCapacity = 5;

        double[] altCreationDelays = new double[] {0.8, 1};
        double[] altServingTimes = new double[] {0.8, 1.2};
        int[] altQCapacities = new int[] {1, 7};

        var allInputParams = new LinkedList<InputParameters>();

        int numberOfQSystems = 3;

        var baseInputParams = new InputParameters(numberOfQSystems, baseCreationDelay, new QSystemParameters[]{
                new QSystemParameters(baseServingTime, baseQCapacity),
                new QSystemParameters(baseServingTime, baseQCapacity),
                new QSystemParameters(baseServingTime, baseQCapacity)
        });

        allInputParams.add(baseInputParams);

        for (double altCreationDelay : altCreationDelays) {
            allInputParams.add(new InputParameters(numberOfQSystems, altCreationDelay, new QSystemParameters[]{
                    new QSystemParameters(baseServingTime, baseQCapacity),
                    new QSystemParameters(baseServingTime, baseQCapacity),
                    new QSystemParameters(baseServingTime, baseQCapacity)
            }));
        }

        for (double altServingTime: altServingTimes) {
            for (int i = 0; i < numberOfQSystems; i++) {
                var qSystemsParams = new QSystemParameters[numberOfQSystems];

                for (int j = 0; j < numberOfQSystems; j++) {
                    if (i == j) {
                        qSystemsParams[j] = new QSystemParameters(altServingTime, baseQCapacity);
                    } else qSystemsParams[j] = new QSystemParameters(baseServingTime, baseQCapacity);
                }

                allInputParams.add(new InputParameters(numberOfQSystems, baseCreationDelay, qSystemsParams));
            }
        }

        for (int altQCapacity: altQCapacities) {
            for (int i = 0; i < numberOfQSystems; i++) {
                var qSystemsParams = new QSystemParameters[numberOfQSystems];

                for (int j = 0; j < numberOfQSystems; j++) {
                    if (i == j) {
                        qSystemsParams[j] = new QSystemParameters(baseServingTime, altQCapacity);
                    } else qSystemsParams[j] = new QSystemParameters(baseServingTime, baseQCapacity);
                }

                allInputParams.add(new InputParameters(numberOfQSystems, baseCreationDelay, qSystemsParams));
            }
        }


        StringBuilder finalCsv = new StringBuilder();
        finalCsv.append("creationDelay, qDelay1, qCapacity1, qDelay2, qCapacity2, qDelay3, qCapacity3, totalNumCustomers, totalDropoutProbability, numDropouts1, meanQLength1, meanUtilization1, numDropouts2, meanQLength2, meanUtilization2, numDropouts3, meanQLength3, meanUtilization3\n");

        for (var inputParamSet : allInputParams) {
            // constructing the experiment

            Create creationElement = new Create(inputParamSet.creationDelay());

            var q1Params = inputParamSet.qSystemsParameters()[0];
            QueueingSystem queueingSystem1 = new QueueingSystem(
                    q1Params.delay(), q1Params.queueCapacity(), ProbabilityDistribution.EXPONENTIAL, "PROCESSOR 1");

            var q2Params = inputParamSet.qSystemsParameters()[1];
            QueueingSystem queueingSystem2 = new QueueingSystem(
                    q2Params.delay(), q2Params.queueCapacity(), ProbabilityDistribution.EXPONENTIAL, "PROCESSOR 2");

            var q3Params = inputParamSet.qSystemsParameters()[2];
            QueueingSystem queueingSystem3 = new QueueingSystem(
                    q3Params.delay(), q3Params.queueCapacity(), ProbabilityDistribution.EXPONENTIAL, "PROCESSOR 3");

            creationElement.setNextElement(queueingSystem1);
            queueingSystem1.setNextElement(queueingSystem2);
            queueingSystem2.setNextElement(queueingSystem3);

            ArrayList<Element> list = new ArrayList<>();
            list.add(creationElement);
            list.add(queueingSystem1);
            list.add(queueingSystem2);
            list.add(queueingSystem3);

            QueueingModel model = new QueueingModel(list);
            var output = model.simulate(1000.0);

            finalCsv.append(inputParamSet.toString());
            finalCsv.append(',');
            finalCsv.append(output.toString());
            finalCsv.append('\n');
        }

        System.out.println(finalCsv.toString());

    }

}
