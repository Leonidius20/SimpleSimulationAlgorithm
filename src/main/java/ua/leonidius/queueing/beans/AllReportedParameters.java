package ua.leonidius.queueing.beans;

import ua.leonidius.queueing.beans.input_params.InputParameters;
import ua.leonidius.queueing.beans.output_params.OutputParameters;

public record AllReportedParameters(
        InputParameters inputParameters, OutputParameters outputParameters
) {

    @Override
    public String toString() {
        return inputParameters.toString() + "," + outputParameters.toString();
    }
}
