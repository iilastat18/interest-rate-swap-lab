package com.lingxin.interestrates;

import java.util.Arrays;

public final class PlainSwap {

    private final double[] periodStarts;
    private final double[] periodEnds;
    private final double fixedRate;
    private final double notional;

    public PlainSwap(
        final double[] periodStarts,
        final double[] periodEnds,
        final double fixedRate,
        final double notional
    ) {
        if (periodStarts == null || periodEnds == null) {
            throw new IllegalArgumentException("Swap periods must not be null.");
        }
        if (periodStarts.length == 0 || periodStarts.length != periodEnds.length) {
            throw new IllegalArgumentException("Swap periods must have the same positive length.");
        }

        for (int index = 0; index < periodStarts.length; index++) {
            if (periodStarts[index] < 0.0 || periodEnds[index] <= periodStarts[index]) {
                throw new IllegalArgumentException("Each swap period must satisfy 0 <= start < end.");
            }
            if (index > 0 && periodStarts[index] < periodEnds[index - 1]) {
                throw new IllegalArgumentException("Swap periods must be ordered and non-overlapping.");
            }
        }

        this.periodStarts = Arrays.copyOf(periodStarts, periodStarts.length);
        this.periodEnds = Arrays.copyOf(periodEnds, periodEnds.length);
        this.fixedRate = fixedRate;
        this.notional = notional;
    }

    public static PlainSwap standard(
        final double maturity,
        final double periodLength,
        final double fixedRate,
        final double notional
    ) {
        if (maturity <= 0.0 || periodLength <= 0.0) {
            throw new IllegalArgumentException("Maturity and period length must be positive.");
        }

        final int numberOfPeriods = (int) Math.ceil(maturity / periodLength);
        final double[] starts = new double[numberOfPeriods];
        final double[] ends = new double[numberOfPeriods];

        double currentStart = 0.0;
        for (int index = 0; index < numberOfPeriods; index++) {
            starts[index] = currentStart;
            ends[index] = Math.min(maturity, currentStart + periodLength);
            currentStart = ends[index];
        }

        return new PlainSwap(starts, ends, fixedRate, notional);
    }

    public double getFixedRate() {
        return fixedRate;
    }

    public double getNotional() {
        return notional;
    }

    public double[] getPeriodStarts() {
        return Arrays.copyOf(periodStarts, periodStarts.length);
    }

    public double[] getPeriodEnds() {
        return Arrays.copyOf(periodEnds, periodEnds.length);
    }

    public double getMaturity() {
        return periodEnds[periodEnds.length - 1];
    }

    public double presentValue(final YieldCurve curve) {
        double presentValue = 0.0;
        for (int index = 0; index < periodStarts.length; index++) {
            final double accrual = periodEnds[index] - periodStarts[index];
            final double forward = curve.forwardRate(periodStarts[index], periodEnds[index]);
            final double discount = curve.discountFactor(periodEnds[index]);
            presentValue += notional * (forward - fixedRate) * accrual * discount;
        }
        return presentValue;
    }
}
