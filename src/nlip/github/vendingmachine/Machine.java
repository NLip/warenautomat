package nlip.github.vendingmachine;
import static java.math.BigDecimal.ZERO;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Stream.generate;
import static java.util.stream.IntStream.rangeClosed;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import nlip.github.vendingmachine.internalstate.TransactionLog;
import nlip.github.vendingmachine.internalstate.RotaryPlate;
import nlip.github.vendingmachine.values.Product;
import nlip.github.vendingmachine.values.Transaction;
import nlip.github.vendingmachine.integration.SystemIntegration;

import static nlip.github.vendingmachine.integration.SystemIntegration.ExpiryStatus.*;

public final class Machine {
  private static final int NUMBER_OF_ROTARY_PLATES = 7;
  
  private final List<RotaryPlate> rotaryPlates;
  private final Register register;
  private final TransactionLog transactionLog;

  public Machine(Register register, TransactionLog transactionLog) {
    this.register = register;
    this.transactionLog = transactionLog;
    this.rotaryPlates = generate(RotaryPlate::new).limit(NUMBER_OF_ROTARY_PLATES).collect(toList());
  }

  public void readNewProduct(int plateNumber, Product product) {
    getPlate(plateNumber).insertProduct(product);
  }

  public void rotatePlates() {
	  rangeClosed(1, NUMBER_OF_ROTARY_PLATES).forEach(plateNumber -> {
		var plate = getPlate(plateNumber);
	    var today = SystemIntegration.getToday();
	    var product = plate.getCurrentProduct();

		var price = product.map(Product::getPrice).orElse(ZERO);		
		var status = product.map(p -> p.isExpiredAsOf(today) ?
				EXPIRED : OK)
				.orElse(OFF);
		
		plate.rotate();
		SystemIntegration.showProductPrice(plateNumber, price);
		SystemIntegration.showExpiryStatus(plateNumber, status);
	  });
  }

  public boolean tryOpen(int plateNumber) {
    var plate = getPlate(plateNumber);
    var today = SystemIntegration.getToday();
    
    var maybeValidProduct = plate.getCurrentProduct().filter(p -> !p.isExpiredAsOf(today));
    if (maybeValidProduct.isEmpty()) {
    	return false;
    }
    
    var product = maybeValidProduct.get();
    var price = product.getPrice();
    if (!register.validatePotentialPurchase(price)) {
    	return false;
    }
    
    register.purchase(price);
    plate.removeProduct();
    transactionLog
        .addTransaction(new Transaction(product, today));
    
    SystemIntegration.unlock(plateNumber);
    return true;
  }

  public BigDecimal getCurrentTotalProductValue() {
    return rotaryPlates
        .stream()
        .flatMap(RotaryPlate::getAllProducts)
        .map(Product::getPrice)
        .reduce(BigDecimal.ZERO, BigDecimal::add);
  }

  public int getNumberOfProductSoldSince(String name, LocalDate date) {
    return (int)
        transactionLog
            .stream()
            .filter(transaction -> transaction.getProductName().equalsIgnoreCase(name))
            .filter(transaction -> !transaction.getDate().isBefore(date))
            .count();
  }
  
  private RotaryPlate getPlate(int plateNumber) {
	  // Plates are numbered 1 through 7 but represented as a List<RotaryPlate> (0-based)
	  return rotaryPlates.get(plateNumber - 1);
  }
}
