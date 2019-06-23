package nlip.github.vendingmachine.internalstate;

import nlip.github.vendingmachine.values.Product;

import java.util.Optional;

public final class Compartment {
  private Product product;

  public Compartment() {
    this.product = null;
  }

  public Optional<Product> getProduct() {
    return Optional.ofNullable(product);
  }

  public void putProduct(Product product) {
    this.product = product;
  }

  public void removeProduct() {
    if (product == null) {
      throw new IllegalStateException("Compartment is already empty.");
    }

    product = null;
  }
}
