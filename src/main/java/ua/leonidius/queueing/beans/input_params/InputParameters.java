package ua.leonidius.queueing.beans.input_params;

// creationDelay, qDelay1, qCapacity1, qDelay2, qCapacity2, qDelay3, qCapacity3
public record InputParameters(
        int numberOfQSystems, // in parallel
        double creationDelay,
        QSystemParameters[] qSystemsParameters
) {

    @Override
    public String toString() {
        var sb = new StringBuilder();

        sb.append(creationDelay).append(',');

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
