package nlip.github.vendingmachine;
import static java.math.BigDecimal.ONE;
import static java.math.BigDecimal.ZERO;
import static java.util.stream.Collectors.toList;
import static java.util.stream.IntStream.range;
import static java.util.stream.Stream.generate;
import static nlip.github.vendingmachine.integration.SystemIntegration.ExpiryStatus.EXPIRED;
import static nlip.github.vendingmachine.integration.SystemIntegration.ExpiryStatus.OFF;
import static nlip.github.vendingmachine.integration.SystemIntegration.ExpiryStatus.OK;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import nlip.github.vendingmachine.integration.SystemIntegration;
import nlip.github.vendingmachine.internalstate.RotaryPlate;
import nlip.github.vendingmachine.internalstate.TransactionLog;
import nlip.github.vendingmachine.values.Product;
import nlip.github.vendingmachine.values.Transaction;

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
    rotaryPlates.get(plateNumber).insertProduct(product);
	showCurrentProduct(plateNumber);
  }

  public void rotatePlates() {
	  range(0, NUMBER_OF_ROTARY_PLATES).forEach(plateNumber -> {
		var plate = rotaryPlates.get(plateNumber);
	    plate.rotate();
		showCurrentProduct(plateNumber);
	  });
  }

  public boolean tryOpen(int plateNumber) {
    var plate = rotaryPlates.get(plateNumber);
    var today = SystemIntegration.getToday();
    
    var maybeValidProduct = plate.getCurrentProduct().filter(p -> !p.isExpiredAsOf(today));
    if (maybeValidProduct.isEmpty()) {
    	return false;
    }
    
    var product = maybeValidProduct.get();
    var price = product.getPrice();
    if (!register.tryPurchase(price)) {
    	return false;
    }
    
    SystemIntegration.unlock(plateNumber);
    
    plate.removeProduct();
    transactionLog
        .addTransaction(new Transaction(product, today));
    showCurrentProduct(plateNumber);
    return true;
  }

  public BigDecimal getCurrentTotalProductValue() {
	var today = SystemIntegration.getToday();
	var expiredValue = BigDecimal.valueOf(0.2);
    return rotaryPlates
        .stream()
        .flatMap(RotaryPlate::getAllProducts)
        .map(product -> {
        	var value = product.isExpiredAsOf(today)? expiredValue : ONE;
        	return product.getPrice().multiply(value);
        })
        .reduce(ZERO, BigDecimal::add);
  }

  public int getNumberOfProductSoldSince(String name, LocalDate date) {
    return (int)
        transactionLog
            .stream()
            .filter(transaction -> transaction.getProductName().equals(name))
            .filter(transaction -> !transaction.getDate().isBefore(date))
            .count();
  }
  
  private void showCurrentProduct(int plateNumber) {
	  var today = SystemIntegration.getToday();
	  	var product = rotaryPlates.get(plateNumber).getCurrentProduct();
		var price = product.map(Product::getPrice).orElse(ZERO);		
		var status = product.map(p -> p.isExpiredAsOf(today) ?
				EXPIRED : OK)
				.orElse(OFF);
		
		SystemIntegration.showProductPrice(plateNumber, price);
		SystemIntegration.showExpiryStatus(plateNumber, status);
  }
}
