package nlip.github.vendingmachine.values;

import java.math.BigDecimal;
import java.time.LocalDate;

public class Transaction {
  private final Product product;
  private final LocalDate date;

  public Transaction(Product product, LocalDate date) {
	this.product = product;
    this.date = date;
  }

  public String getProductName() {
    return product.getName();
  }

  public BigDecimal getPrice() {
    return product.getPrice();
  }

  public LocalDate getDate() {
    return date;
  }
}