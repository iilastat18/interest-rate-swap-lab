package com.lingxin.interestrates;

import java.util.ArrayList;
import java.util.List;

public final class SwapAnalytics {

    private SwapAnalytics() {
    }

    public static double parSwapRate(final YieldCurve curve, final double maturity, final double periodLength) {
        final PlainSwap placeholder = PlainSwap.standard(maturity, periodLength, 0.0, 1.0);
        final double[] periodStarts = placeholder.getPeriodStarts();
        final double[] periodEnds = placeholder.getPeriodEnds();

        double floatingLeg = 0.0;
        double fixedLegAnnuity = 0.0;

        for (int index = 0; index < periodStarts.length; index++) {
            final double accrual = periodEnds[index] - periodStarts[index];
            floatingLeg += curve.discountFactor(periodStarts[index]) - curve.discountFactor(periodEnds[index]);
            fixedLegAnnuity += accrual * curve.discountFactor(periodEnds[index]);
        }

        return floatingLeg / fixedLegAnnuity;
    }

    public static List<PlainSwap> buildBenchmarkSwaps(
        final YieldCurve curve,
        final double[] maturities,
        final double periodLength,
        final double notional
    ) {
        final List<PlainSwap> swaps = new ArrayList<>();
        for (double maturity : maturities) {
            final double parRate = parSwapRate(curve, maturity, periodLength);
            swaps.add(PlainSwap.standard(maturity, periodLength, parRate, notional));
        }
        return swaps;
    }

    public static double[] bucketedCurveRisk(
        final PlainSwap swap,
        final YieldCurve curve,
        final double bumpSize
    ) {
        final double basePresentValue = swap.presentValue(curve);
        final double[] zeroRates = curve.getZeroRates();
        final double[] bucketedRisk = new double[zeroRates.length];

        for (int index = 0; index < zeroRates.length; index++) {
            final YieldCurve bumpedCurve = curve.bumpedRate(index, bumpSize);
            final double bumpedPresentValue = swap.presentValue(bumpedCurve);
            bucketedRisk[index] = (bumpedPresentValue - basePresentValue) / bumpSize;
        }
        return bucketedRisk;
    }

    public static HedgePortfolioResult hedgeWithBenchmarks(
        final PlainSwap targetSwap,
        final List<PlainSwap> benchmarkSwaps,
        final YieldCurve curve,
        final double bumpSize
    ) {
        if (benchmarkSwaps.isEmpty()) {
            throw new IllegalArgumentException("At least one benchmark swap is required.");
        }

        final double[] targetSensitivities = bucketedCurveRisk(targetSwap, curve, bumpSize);
        final double[][] sensitivityMatrix = new double[targetSensitivities.length][benchmarkSwaps.size()];

        for (int column = 0; column < benchmarkSwaps.size(); column++) {
            final double[] benchmarkSensitivities = bucketedCurveRisk(benchmarkSwaps.get(column), curve, bumpSize);
            for (int row = 0; row < targetSensitivities.length; row++) {
                sensitivityMatrix[row][column] = benchmarkSensitivities[row];
            }
        }

        final double[] weights = MatrixAlgebra.leastSquares(sensitivityMatrix, targetSensitivities);
        final double[] replicatedSensitivities = MatrixAlgebra.multiply(sensitivityMatrix, weights);
        return new HedgePortfolioResult(weights, targetSensitivities, replicatedSensitivities);
    }
}
