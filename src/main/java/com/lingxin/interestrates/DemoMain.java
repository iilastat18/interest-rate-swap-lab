package com.lingxin.interestrates;

import java.util.List;

public final class DemoMain {

    private DemoMain() {
    }

    public static void main(final String[] args) {
        runScenario(InterpolationMode.LINEAR_ZERO_RATES);
        System.out.println();
        runScenario(InterpolationMode.LOG_LINEAR_DISCOUNT_FACTORS);
    }

    private static void runScenario(final InterpolationMode interpolationMode) {
        final double[] times = {1.0, 2.0, 3.0, 5.0, 7.0, 10.0};
        final double[] zeroRates = {0.0280, 0.0300, 0.0315, 0.0335, 0.0345, 0.0355};
        final YieldCurve curve = new YieldCurve(times, zeroRates, interpolationMode);

        final PlainSwap targetSwap = PlainSwap.standard(6.5, 0.5, 0.0380, 1_000_000.0);
        final double targetPresentValue = targetSwap.presentValue(curve);

        final double[] benchmarkMaturities = {1.0, 2.0, 3.0, 5.0, 7.0, 10.0};
        final List<PlainSwap> benchmarks = SwapAnalytics.buildBenchmarkSwaps(curve, benchmarkMaturities, 0.5, 1_000_000.0);
        final HedgePortfolioResult hedge = SwapAnalytics.hedgeWithBenchmarks(targetSwap, benchmarks, curve, 1.0E-4);

        System.out.println("=== " + interpolationMode + " ===");
        System.out.printf("Target swap PV: %.2f%n", targetPresentValue);
        System.out.println("Benchmark par rates:");
        for (int index = 0; index < benchmarkMaturities.length; index++) {
            final double parRate = benchmarks.get(index).getFixedRate();
            System.out.printf("  %.1fY swap -> %.4f%%%n", benchmarkMaturities[index], 100.0 * parRate);
        }

        System.out.println("Hedge weights:");
        final double[] weights = hedge.getWeights();
        for (int index = 0; index < weights.length; index++) {
            System.out.printf("  %.1fY benchmark -> %.4f%n", benchmarkMaturities[index], weights[index]);
        }

        System.out.println("Bucketed risk residuals:");
        final double[] residuals = hedge.residuals();
        for (int index = 0; index < residuals.length; index++) {
            System.out.printf("  Node %.1fY -> %.6f%n", times[index], residuals[index]);
        }

        System.out.printf("RMS residual: %.6f%n", hedge.rootMeanSquaredResidual());
    }
}
