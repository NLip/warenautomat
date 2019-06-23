package nlip.github.vendingmachine.extensions;

import static java.lang.Math.max;
import static java.lang.Math.min;

public final class MathUtils {
	public static int clamp(int value, int lowerLimit, int upperLimit) {
		return max(min(value,  upperLimit), lowerLimit);
	}
}
