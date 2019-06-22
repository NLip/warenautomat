package state;

import static java.util.stream.Collectors.toList;
import static java.util.stream.Stream.generate;

import values.CustomerProblem;
import values.Product;
import values.RegisterDisplay;
import values.Transaction;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import warenautomat.SystemSoftware;

public final class Machine {

  private static final int NUMBER_OF_ROTARY_PLATES = 7;
  private final List<RotaryPlate> rotaryPlates;
  private final Register register;
  private final TransactionLog transactionLog;
  private final RegisterDisplay registerDisplay;

  public Machine(Register register, TransactionLog transactionLog) {
    this.register = register;
    this.transactionLog = transactionLog;
    this.registerDisplay = new RegisterDisplay();
    this.rotaryPlates = generate(RotaryPlate::new).limit(NUMBER_OF_ROTARY_PLATES).collect(toList());
  }

  public void readNewProduct(int plateNumber, Product product) {
    rotaryPlates.get(plateNumber).insertProduct(product);
  }

  public void rotatePlates() {
    rotaryPlates.forEach(RotaryPlate::rotatePlate);
  }

  public boolean openRotaryPlate(int plateNumber) {
    var plate = rotaryPlates.get(plateNumber);

    var problem = proposeOpening(plate);
    if (problem.isPresent()) {
      registerDisplay.display(problem.get());
      return false;
    }

    BigDecimal price = plate.getProductOrThrow().getPrice();
    String productName = plate.getProductOrThrow().getName();
    register.purchase(price);
    transactionLog
        .addTransaction(new Transaction(productName, price, SystemSoftware.gibAktuellesDatum()));
    return true;
  }

  private Optional<CustomerProblem> proposeOpening(RotaryPlate plate) {
    return plate
        .proposePurchase()
        .or(() -> register.proposePurchase(plate.getProductOrThrow().getPrice()));
  }

  public BigDecimal computeTotalOfProducts() {
    return rotaryPlates
        .stream()
        .flatMap(RotaryPlate::getAllProducts)
        .map(Product::getPrice)
        .reduce(BigDecimal.ZERO, BigDecimal::add);
  }

  public int numberOfSalesPerProductSince(String name, LocalDate date) {
    return (int)
        transactionLog.getTransactions()
            .stream()
            .filter(transaction -> transaction.getProductName().equalsIgnoreCase(name))
            .filter(transaction -> !transaction.getDate().isBefore(date))
            .count();
  }
}
