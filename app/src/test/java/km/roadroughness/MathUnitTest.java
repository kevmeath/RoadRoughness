package km.roadroughness;

import org.junit.Test;

import static org.junit.Assert.*;

import km.roadroughness.util.RRMath;

public class MathUnitTest {
    @Test
    public void calcVelocity_isCorrect() {
        // Input
        float initialVelocity = 0.8200719f;
        float acceleration = 0.9389397f;
        float time = 0.019796629f;

        // Expected
        float finalVelocity = 0.8386597f;
        float delta = 0f;

        assertEquals(finalVelocity, RRMath.calcVelocity(initialVelocity, acceleration, time), delta);

        assertEquals(10f, RRMath.calcVelocity(4f, 2f, 3f), delta);
    }

    @Test
    public void calcDistance_isCorrect() {
        // Input
        float velocity = 0.9232932f;
        float acceleration = -0.016082548f;
        float time = 0.01979654f;

        // Expected
        float distance = 0.018274859f;
        float delta = 0f;

        assertEquals(distance, RRMath.calcDistance(velocity, acceleration, time), delta);

        assertEquals(21f, RRMath.calcDistance(4f, 2f, 3f), delta);
    }
}
