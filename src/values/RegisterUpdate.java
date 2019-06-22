package values;

public final class RegisterUpdate {
  private final Denomination denomination;
  private final int numberOfCoins;
  
  public RegisterUpdate(Denomination denomination, int numberOfCoins) {
	  this.denomination = denomination;
	  this.numberOfCoins = numberOfCoins;
  }
  
  public int getNumberOfCoins() {
	  return numberOfCoins;
  }
  
  public Denomination getDenomination() {
	  return denomination;
  }
}
