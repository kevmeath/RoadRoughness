package km.roadroughness.util;

public class RRMath {
    private RRMath(){};

    public static float[] transposeMatrix(float[] matrix) {
        float[] transposed = new float[9];

        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 3; j++) {
                transposed[(i+j)+2*i] = matrix[(i+j)+2*j];
            }
        }

        return transposed;
    }

    public static float[] multiplyMatrix(float[] matrixA, float[] matrixB) {
        float[] multiplied = new float[3];

        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 3; j++) {
                multiplied[i] += matrixA[j] * matrixB[(i+j)+2*j];
            }
        }

        return multiplied;
    }
}
