package values;

import java.math.BigDecimal;
import java.time.LocalDate;

public class Transaction {
  private String productName;
  private BigDecimal priceAtDate;
  private LocalDate date;

  public Transaction(String productName, BigDecimal priceAtDate, LocalDate date) {
    this.productName = productName;
    this.priceAtDate = priceAtDate;
    this.date = date;
  }

  public String getProductName() {
    return productName;
  }

  public BigDecimal getPriceAtDate() {
    return priceAtDate;
  }

  public LocalDate getDate() {
    return date;
  }
}