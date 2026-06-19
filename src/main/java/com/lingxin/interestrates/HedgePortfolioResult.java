package com.lingxin.interestrates;

import java.util.Arrays;

public final class HedgePortfolioResult {

    private final double[] weights;
    private final double[] targetSensitivities;
    private final double[] replicatedSensitivities;

    public HedgePortfolioResult(
        final double[] weights,
        final double[] targetSensitivities,
        final double[] replicatedSensitivities
    ) {
        this.weights = Arrays.copyOf(weights, weights.length);
        this.targetSensitivities = Arrays.copyOf(targetSensitivities, targetSensitivities.length);
        this.replicatedSensitivities = Arrays.copyOf(replicatedSensitivities, replicatedSensitivities.length);
    }

    public double[] getWeights() {
        return Arrays.copyOf(weights, weights.length);
    }

    public double[] getTargetSensitivities() {
        return Arrays.copyOf(targetSensitivities, targetSensitivities.length);
    }

    public double[] getReplicatedSensitivities() {
        return Arrays.copyOf(replicatedSensitivities, replicatedSensitivities.length);
    }

    public double[] residuals() {
        final double[] residuals = new double[targetSensitivities.length];
        for (int index = 0; index < residuals.length; index++) {
            residuals[index] = targetSensitivities[index] - replicatedSensitivities[index];
        }
        return residuals;
    }

    public double rootMeanSquaredResidual() {
        final double[] residuals = residuals();
        double squaredError = 0.0;
        for (double residual : residuals) {
            squaredError += residual * residual;
        }
        return Math.sqrt(squaredError / residuals.length);
    }
}
