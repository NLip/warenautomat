package state;

import exceptions.InvalidOperationException;
import values.Product;
import java.util.Optional;

public final class Compartment {
  private Product product;

  public Compartment() {
    product = null;
  }

  public Optional<Product> getProduct() {
    return Optional.ofNullable(product);
  }

  public void putProduct(Product product) {
    this.product = product;
  }

  public void removeProduct() {
    if (product == null) {
      throw new InvalidOperationException("Compartment is already empty.");
    }

    product = null;
  }
}
