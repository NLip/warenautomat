package nlip.github.vendingmachine.integration;

import java.util.Optional;

import nlip.github.vendingmachine.values.Denomination;
import nlip.github.vendingmachine.values.Product;
import warenautomat.SystemSoftware;

public final class GuiIntegration {
	  
  private GuiIntegration() {}
  
  public static void renderCoins(Denomination denomination, Integer numberOfCoins) {
	  SystemSoftware.zeigeMuenzenInGui(denomination.getValue().doubleValue(), numberOfCoins);
  }
  
  public static void renderProduct(int plateNumber, Optional<Product> product) {
	    SystemSoftware.zeigeWareInGui(plateNumber + 1,
	    		product.map(Product::getName).orElse(null),
	    		product.map(Product::getBestBeforeDate).orElse(null));
  }
  
  public static void renderRotation() {
	  SystemSoftware.dreheWarenInGui();
  }
}
