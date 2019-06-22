package values;

import java.math.BigDecimal;
import java.time.LocalDate;

public class Product {
  private final String name;
  private final BigDecimal price;
  private final LocalDate bestBeforeDate;

  public Product(String name, BigDecimal price, LocalDate bestBeforeDate) {
    this.name = name;
    this.price = price;
    this.bestBeforeDate = bestBeforeDate;
  }

  public String getName() {
    return name;
  }

  public BigDecimal getPrice() {
    return price;
  }

  public LocalDate getBestBeforeDate() {
    return bestBeforeDate;
  }
}
