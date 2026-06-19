package com.lingxin.interestrates;

import java.util.Arrays;

public final class YieldCurve {

    private final double[] times;
    private final double[] zeroRates;
    private final InterpolationMode interpolationMode;

    public YieldCurve(final double[] times, final double[] zeroRates, final InterpolationMode interpolationMode) {
        if (times == null || zeroRates == null || interpolationMode == null) {
            throw new IllegalArgumentException("Curve inputs must not be null.");
        }
        if (times.length == 0 || times.length != zeroRates.length) {
            throw new IllegalArgumentException("Times and zero rates must have the same positive length.");
        }

        double previousTime = 0.0;
        for (double time : times) {
            if (time <= previousTime) {
                throw new IllegalArgumentException("Curve times must be strictly increasing and positive.");
            }
            previousTime = time;
        }

        this.times = Arrays.copyOf(times, times.length);
        this.zeroRates = Arrays.copyOf(zeroRates, zeroRates.length);
        this.interpolationMode = interpolationMode;
    }

    public double[] getTimes() {
        return Arrays.copyOf(times, times.length);
    }

    public double[] getZeroRates() {
        return Arrays.copyOf(zeroRates, zeroRates.length);
    }

    public InterpolationMode getInterpolationMode() {
        return interpolationMode;
    }

    public YieldCurve bumpedRate(final int index, final double shift) {
        if (index < 0 || index >= zeroRates.length) {
            throw new IllegalArgumentException("Curve bump index out of range.");
        }

        final double[] bumpedRates = Arrays.copyOf(zeroRates, zeroRates.length);
        bumpedRates[index] += shift;
        return new YieldCurve(times, bumpedRates, interpolationMode);
    }

    public double discountFactor(final double maturity) {
        if (maturity < 0.0) {
            throw new IllegalArgumentException("Maturity cannot be negative.");
        }
        if (maturity == 0.0) {
            return 1.0;
        }

        return switch (interpolationMode) {
            case LINEAR_ZERO_RATES -> Math.exp(-interpolatedZeroRate(maturity) * maturity);
            case LOG_LINEAR_DISCOUNT_FACTORS -> Math.exp(interpolatedLogDiscountFactor(maturity));
        };
    }

    public double forwardRate(final double periodStart, final double periodEnd) {
        if (periodStart < 0.0 || periodEnd <= periodStart) {
            throw new IllegalArgumentException("Forward period must satisfy 0 <= start < end.");
        }

        final double discountStart = discountFactor(periodStart);
        final double discountEnd = discountFactor(periodEnd);
        return (discountStart / discountEnd - 1.0) / (periodEnd - periodStart);
    }

    private double interpolatedZeroRate(final double maturity) {
        if (maturity <= times[0]) {
            return zeroRates[0];
        }
        if (maturity >= times[times.length - 1]) {
            return zeroRates[zeroRates.length - 1];
        }

        for (int index = 1; index < times.length; index++) {
            if (maturity <= times[index]) {
                return linearInterpolate(
                    maturity,
                    times[index - 1],
                    zeroRates[index - 1],
                    times[index],
                    zeroRates[index]
                );
            }
        }

        throw new IllegalStateException("Interpolation failed.");
    }

    private double interpolatedLogDiscountFactor(final double maturity) {
        if (maturity <= times[0]) {
            final double firstLogDiscount = -zeroRates[0] * times[0];
            return linearInterpolate(maturity, 0.0, 0.0, times[0], firstLogDiscount);
        }
        if (maturity >= times[times.length - 1]) {
            final double terminalRate = zeroRates[zeroRates.length - 1];
            return -terminalRate * maturity;
        }

        for (int index = 1; index < times.length; index++) {
            if (maturity <= times[index]) {
                final double leftLogDiscount = -zeroRates[index - 1] * times[index - 1];
                final double rightLogDiscount = -zeroRates[index] * times[index];
                return linearInterpolate(
                    maturity,
                    times[index - 1],
                    leftLogDiscount,
                    times[index],
                    rightLogDiscount
                );
            }
        }

        throw new IllegalStateException("Log-discount interpolation failed.");
    }

    private static double linearInterpolate(
        final double x,
        final double x0,
        final double y0,
        final double x1,
        final double y1
    ) {
        if (x1 == x0) {
            return y0;
        }
        final double weight = (x - x0) / (x1 - x0);
        return y0 + weight * (y1 - y0);
    }
}
