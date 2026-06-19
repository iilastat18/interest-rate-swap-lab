package com.lingxin.interestrates;

public final class MatrixAlgebra {

    private MatrixAlgebra() {
    }

    public static double[] leastSquares(final double[][] matrix, final double[] rhs) {
        final double[][] transposed = transpose(matrix);
        final double[][] normalMatrix = multiply(transposed, matrix);
        final double[] normalRhs = multiply(transposed, rhs);
        return solve(normalMatrix, normalRhs);
    }

    public static double[][] transpose(final double[][] matrix) {
        final int rows = matrix.length;
        final int columns = matrix[0].length;
        final double[][] transposed = new double[columns][rows];

        for (int row = 0; row < rows; row++) {
            for (int column = 0; column < columns; column++) {
                transposed[column][row] = matrix[row][column];
            }
        }
        return transposed;
    }

    public static double[][] multiply(final double[][] left, final double[][] right) {
        final int rows = left.length;
        final int inner = left[0].length;
        final int columns = right[0].length;
        final double[][] result = new double[rows][columns];

        for (int row = 0; row < rows; row++) {
            for (int column = 0; column < columns; column++) {
                double value = 0.0;
                for (int index = 0; index < inner; index++) {
                    value += left[row][index] * right[index][column];
                }
                result[row][column] = value;
            }
        }
        return result;
    }

    public static double[] multiply(final double[][] matrix, final double[] vector) {
        final int rows = matrix.length;
        final int columns = matrix[0].length;
        final double[] result = new double[rows];

        for (int row = 0; row < rows; row++) {
            double value = 0.0;
            for (int column = 0; column < columns; column++) {
                value += matrix[row][column] * vector[column];
            }
            result[row] = value;
        }
        return result;
    }

    public static double[] solve(final double[][] coefficients, final double[] rhs) {
        final int size = rhs.length;
        final double[][] matrix = new double[size][size];
        final double[] constants = rhs.clone();

        for (int row = 0; row < size; row++) {
            System.arraycopy(coefficients[row], 0, matrix[row], 0, size);
        }

        for (int pivot = 0; pivot < size; pivot++) {
            int maxRow = pivot;
            for (int row = pivot + 1; row < size; row++) {
                if (Math.abs(matrix[row][pivot]) > Math.abs(matrix[maxRow][pivot])) {
                    maxRow = row;
                }
            }

            if (Math.abs(matrix[maxRow][pivot]) < 1E-12) {
                throw new IllegalArgumentException("Linear system is singular or ill-conditioned.");
            }

            swapRows(matrix, constants, pivot, maxRow);

            for (int row = pivot + 1; row < size; row++) {
                final double factor = matrix[row][pivot] / matrix[pivot][pivot];
                constants[row] -= factor * constants[pivot];
                for (int column = pivot; column < size; column++) {
                    matrix[row][column] -= factor * matrix[pivot][column];
                }
            }
        }

        final double[] solution = new double[size];
        for (int row = size - 1; row >= 0; row--) {
            double value = constants[row];
            for (int column = row + 1; column < size; column++) {
                value -= matrix[row][column] * solution[column];
            }
            solution[row] = value / matrix[row][row];
        }
        return solution;
    }

    private static void swapRows(final double[][] matrix, final double[] rhs, final int left, final int right) {
        if (left == right) {
            return;
        }

        final double[] matrixRow = matrix[left];
        matrix[left] = matrix[right];
        matrix[right] = matrixRow;

        final double rhsValue = rhs[left];
        rhs[left] = rhs[right];
        rhs[right] = rhsValue;
    }
}
