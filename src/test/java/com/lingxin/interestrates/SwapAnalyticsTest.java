package com.lingxin.interestrates;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;

import org.junit.jupiter.api.Test;

class SwapAnalyticsTest {

    @Test
    void parRateProducesNearZeroPresentValue() {
        final YieldCurve curve = new YieldCurve(
            new double[] {1.0, 2.0, 3.0, 5.0, 7.0, 10.0},
            new double[] {0.0280, 0.0300, 0.0315, 0.0335, 0.0345, 0.0355},
            InterpolationMode.LOG_LINEAR_DISCOUNT_FACTORS
        );

        final double parRate = SwapAnalytics.parSwapRate(curve, 5.0, 0.5);
        final PlainSwap parSwap = PlainSwap.standard(5.0, 0.5, parRate, 1_000_000.0);

        assertTrue(Math.abs(parSwap.presentValue(curve)) < 1.0E-6);
    }

    @Test
    void hedgePortfolioKeepsResidualRiskSmall() {
        final YieldCurve curve = new YieldCurve(
            new double[] {1.0, 2.0, 3.0, 5.0, 7.0, 10.0},
            new double[] {0.0280, 0.0300, 0.0315, 0.0335, 0.0345, 0.0355},
            InterpolationMode.LINEAR_ZERO_RATES
        );

        final PlainSwap targetSwap = PlainSwap.standard(6.5, 0.5, 0.0380, 1_000_000.0);
        final List<PlainSwap> benchmarks = SwapAnalytics.buildBenchmarkSwaps(
            curve,
            new double[] {1.0, 2.0, 3.0, 5.0, 7.0, 10.0},
            0.5,
            1_000_000.0
        );

        final HedgePortfolioResult hedge = SwapAnalytics.hedgeWithBenchmarks(targetSwap, benchmarks, curve, 1.0E-4);

        assertTrue(hedge.rootMeanSquaredResidual() < 500.0);
    }
}
