package task_9;

public final class MatrixPower {
    private final int[][] matrix1;
    private final int[][] matrix2;
    private final int power;
    private int[][] resultMatrix1;
    private int[][] resultMatrix2;

    public MatrixPower(int[][] matrix1, int[][] matrix2, int power) {
        this.matrix1 = matrix1;
        this.matrix2 = matrix2;
        this.power = power;
        this.resultMatrix1 = powerMatrix(matrix1, power);
        this.resultMatrix2 = powerMatrix(matrix2, power);
    }

    public int[][] getResultMatrix1() {
        return resultMatrix1; // Возвращаем ранее вычисленную матрицу
    }

    public int[][] getResultMatrix2() {
        return resultMatrix2; // Возвращаем ранее вычисленную матрицу
    }

    public void powerAndPrintResult() {
        int[][] resultMatrix1 = powerMatrix(matrix1, power);
        int[][] resultMatrix2 = powerMatrix(matrix2, power);
        System.out.println("Результат возведения первой матрицы в степень " + power + ":");
        MatrixMultiplier.printMatrix(resultMatrix1);
        System.out.println("Результат возведения второй матрицы в степень " + power + ":");
        MatrixMultiplier.printMatrix(resultMatrix2);
    }

    public static int[][] powerMatrix(int[][] matrix, int power) {
        switch (power) {
            case 0:
                int size = matrix.length;
                int[][] resultMatrix = new int[size][size];
                for (int i = 0; i < size; i++) {
                    resultMatrix[i][i] = 1; // Identity matrix
                }
                return resultMatrix;
            case 1:
                return matrix;
            default:
                int[][] tempMatrix = powerMatrix(matrix, power / 2);
                if (power % 2 == 0) {
                    return MatrixMultiplier.multiplyMatrices(tempMatrix, tempMatrix);
                } else {
                    return MatrixMultiplier.multiplyMatrices(MatrixMultiplier.multiplyMatrices(tempMatrix, tempMatrix), matrix);
                }
        }
    }

    public static void printMatrix(int[][] matrix) {
        for (int[] row : matrix) {
            for (int value : row) {
                System.out.print(value + " ");
            }
            System.out.println();
        }
    }
}


