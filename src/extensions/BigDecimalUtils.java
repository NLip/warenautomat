package extensions;

import static extensions.BigDecimalUtils.Comparison.*;
import static java.math.BigDecimal.ZERO;

import java.math.BigDecimal;

public final class BigDecimalUtils {
  private BigDecimalUtils() {}

  public enum Comparison {
    GT,
    EQ,
    LT,
  }

  public static Comparison compare(BigDecimal a, BigDecimal b) {
    switch (a.compareTo(b)) {
      case 0:
        return EQ;
      case 1:
        return GT;
      case -1:
        return LT;
      default:
        throw new RuntimeException("Unable to compare");
    }
  }

  public static boolean isNegative(BigDecimal a) {
    return compare(a, ZERO) == LT;
  }

  public static boolean eq(BigDecimal a, BigDecimal b) {
    return compare(a, b) == EQ;
  }
}
