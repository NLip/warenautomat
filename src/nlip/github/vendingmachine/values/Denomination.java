package nlip.github.vendingmachine.values;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.stream.Stream;

public enum Denomination {
  CHF_200(2),
  CHF_100(1),
  CHF_050(0.5),
  CHF_020(0.2),
  CHF_010(0.1);

  private final BigDecimal value;

  Denomination(double value) {
    this.value = BigDecimal.valueOf(value);
  }

  public BigDecimal getValue() {
    return value;
  }

  public static Stream<Denomination> stream() {
    return Stream.of(values());
  }

  public static Optional<Denomination> fromDouble(double value) {
    return Stream.of(values())
        .filter(denomination -> value == denomination.getValue().doubleValue())
        .findFirst();
  }
}
