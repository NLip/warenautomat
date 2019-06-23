package nlip.github.vendingmachine.internalstate;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Stream.generate;

import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

import nlip.github.vendingmachine.values.Product;

public final class RotaryPlate {
  private static final int NUMBER_OF_COMPARTMENTS = 16;

  private int activeCompartment;
  private final List<Compartment> compartments;

  public RotaryPlate() {
    this.activeCompartment = 0;
    this.compartments = generate(Compartment::new).limit(NUMBER_OF_COMPARTMENTS).collect(toList());
  }

  /**
   * Dreht den Drehteller um ein Fach weiter. Wenn das letzte Fach erreicht wird, rotiert es weiter
   * zum ersten Fach.
   */
  public void rotate() {
    activeCompartment = (activeCompartment + 1) % compartments.size();
  }

  public void insertProduct(Product product) {
    getActiveCompartment().putProduct(product);
  }

  public void removeProduct() {
    getActiveCompartment().removeProduct();
  }

  public Optional<Product> getCurrentProduct() {
    return getActiveCompartment().getProduct();
  }

  public Stream<Product> getAllProducts() {
    return compartments
        .stream()
        .map(Compartment::getProduct)
        .flatMap(Optional::stream);
  }

  private Compartment getActiveCompartment() {
    return compartments.get(activeCompartment);
  }
}
