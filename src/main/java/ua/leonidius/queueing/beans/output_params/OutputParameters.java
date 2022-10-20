package ua.leonidius.queueing.beans.output_params;

// totalNumCustomers, totalDropoutProbability, numDropouts1, meanQLength1, meanUtilization1, numDropouts2, meanQLength2, meanUtilization2, numDropouts3, meanQLength3, meanUtilization3
public record OutputParameters(
        int totalNumCustomers, double totalDropoutProbability,
        QSystemPerformanceMetrics[] qSystemsPerformanceMetrics
) {

    @Override
    public String toString() {
        var sb = new StringBuilder();

        sb.append(totalNumCustomers).append(',');
        sb.append(totalDropoutProbability).append(',');

        for (int i = 0; i < qSystemsPerformanceMetrics.length; i++) {
            var qSystemPerformanceMetrics = qSystemsPerformanceMetrics[i];

            sb.append(qSystemPerformanceMetrics.numDropouts()).append(',');
            sb.append(qSystemPerformanceMetrics.meanQLength()).append(',');
            sb.append(qSystemPerformanceMetrics.meanUtilization());

            if (i < qSystemsPerformanceMetrics.length - 1) {
                sb.append(',');
            }
        }

        return sb.toString();
    }

}
