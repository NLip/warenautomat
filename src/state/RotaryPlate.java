package state;

import static values.CustomerProblem.PRODUCT_EXPIRED;
import static values.CustomerProblem.PRODUCT_MISSING;
import static java.time.LocalDate.now;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Stream.generate;

import exceptions.InvalidOperationException;
import values.CustomerProblem;
import values.Product;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

public final class RotaryPlate {
  private static final int NUMBER_OF_COMPARTMENTS = 16;

  private boolean isLocked;
  private int activeCompartment;
  private final List<Compartment> compartments;

  public RotaryPlate() {
    isLocked = true;
    activeCompartment = 0;
    compartments = generate(Compartment::new).limit(NUMBER_OF_COMPARTMENTS).collect(toList());
  }

  // TODO: Put german stuff and page of requirement in docsting (e.g "Drehteller as specified on
  //  page 10).
  /**
   * Dreht den Drehteller um ein Fach weiter. Wenn das letzte Fach erreicht wird, rotiert es weiter
   * zum ersten Fach.
   */
  public void rotatePlate() {
    activeCompartment = (activeCompartment + 1) % compartments.size();
  }

  public Boolean isLocked() {
    return isLocked;
  }

  public void insertProduct(Product product) {
    getActiveCompartment().putProduct(product);
  }

  public void removeProduct() {
    getActiveCompartment().removeProduct();
  }

  public Optional<CustomerProblem> proposePurchase() {
    var maybeProduct = getProduct();
    if (maybeProduct.isEmpty()) {
      return Optional.of(PRODUCT_MISSING);
    }
    var product = maybeProduct.get();
    if (!now().isBefore(product.getBestBeforeDate())) {
      return Optional.of(PRODUCT_EXPIRED);
    }
    return Optional.empty();
  }

  public Product getProductOrThrow() {
    return getProduct().orElseThrow(() -> new InvalidOperationException("No product available"));
  }

  public Optional<Product> getProduct() {
    return getActiveCompartment().getProduct();
  }

  public Stream<Product> getAllProducts() {
    return compartments
        .stream()
        .map(Compartment::getProduct)
        .filter(Optional::isPresent)
        .map(Optional::get);
  }

  public void unlock() {
    isLocked = false;
  }

  public void lock() {
    // TODO: Move to customerMode   getActiveCompartment().removeProduct();
    isLocked = true;
  }

  private Compartment getActiveCompartment() {
    return compartments.get(activeCompartment);
  }
}
