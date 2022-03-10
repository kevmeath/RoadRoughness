package km.roadroughness.util;

public class RRMath {
    private RRMath(){};

    /**
     * @param matrix A 3x3 matrix in the form of a 9 element array
     * @return Transposed matrix
     */
    public static float[] transposeMatrix(float[] matrix) {
        float[] transposed = new float[9];

        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 3; j++) {
                transposed[(i+j)+2*i] = matrix[(i+j)+2*j];
            }
        }

        return transposed;
    }

    /**
     * @param matrixA A 1x3 matrix in the form of a 3 element array
     * @param matrixB A 3x3 matrix in the form of a 9 element array
     * @return The product of matrixA and matrixB
     */
    public static float[] multiplyMatrix(float[] matrixA, float[] matrixB) {
        float[] multiplied = new float[3];

        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 3; j++) {
                multiplied[i] += matrixA[j] * matrixB[(i+j)+2*j];
            }
        }

        return multiplied;
    }

    /**
     * @param velocity Initial velocity in meters per second (m/s)
     * @param acceleration Acceleration in meters per second squared (m/s²)
     * @param time Time in seconds (s) since initial velocity
     * @return Final velocity in meters per second (m/s)
     */
    public static float calcVelocity(float velocity, float acceleration, float time) {
        return velocity + (acceleration * time);
    }

    /**
     * @param velocity Velocity in meters per second (m/s)
     * @param acceleration Acceleration in meters per second squared (m/s²)
     * @param time Time in seconds (s)
     * @return Distance in meters (m)
     */
    public static float calcDistance(float velocity, float acceleration, float time) {
        return (velocity * time) + 0.5f * acceleration * (time * time);
    }
}
