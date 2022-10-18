package ua.leonidius.queueing.beans.input_params;

public record InputParameters(
        int numberOfQSystems, // in parallel
        QSystemParameters[] qSystemsParameters
) {

    @Override
    public String toString() {
        var sb = new StringBuilder();

        for (int i = 0; i < qSystemsParameters.length; i++) {
            var qSystemParameters = qSystemsParameters[i];

            sb.append(qSystemParameters.delay()).append(',');
            sb.append(qSystemParameters.queueCapacity());

            if (i < qSystemsParameters.length - 1) {
                sb.append(',');
            }
        }

        return sb.toString();
    }

}
