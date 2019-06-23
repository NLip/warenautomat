package nlip.github.vendingmachine.internalstate;

import static java.math.BigDecimal.ZERO;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

import nlip.github.vendingmachine.values.Denomination;

public final class Holdings {
  private final Map<Denomination, Integer> numberOfCoinsByDenomination;

  public Holdings() {
    this.numberOfCoinsByDenomination = new HashMap<>();
  }

  public Integer get(Denomination denomination) {
    return numberOfCoinsByDenomination.getOrDefault(denomination, 0);
  }

  public void put(Denomination denomination, Integer numberOfCoins) {
    numberOfCoinsByDenomination.put(denomination, numberOfCoins);
  }
  
  public void clear() {
	  this.numberOfCoinsByDenomination.clear();
  }

  public BigDecimal getValue() {
    return numberOfCoinsByDenomination
        .entrySet()
        .stream()
        .map(
            e -> {
              var denomination = e.getKey();
              var numberOfCoins = BigDecimal.valueOf(e.getValue());
              return denomination.getValue().multiply(numberOfCoins);
            })
        .reduce(ZERO, BigDecimal::add);
  }
}
