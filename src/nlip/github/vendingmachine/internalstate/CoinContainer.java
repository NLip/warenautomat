package nlip.github.vendingmachine.internalstate;

public final class CoinContainer {
  private static final int CAPACITY = 100;
  private static final int INITIAL_NUMBER_OF_COINS = 0;

  private int numberOfCoins;

  public CoinContainer() {
    this.numberOfCoins = INITIAL_NUMBER_OF_COINS;
  }

  public boolean isAtCapacity() {
    return numberOfCoins >= CAPACITY;
  }
  
  public boolean isEmpty() {
	  return numberOfCoins == 0;
  }

  public int getNumberOfCoins() {
    return numberOfCoins;
  }
  
  public int getAvailableSpace() {
	  return CAPACITY - numberOfCoins;
  }

  public void increment() {
    if (isAtCapacity()) {
      throw new IllegalStateException("Capacity is maxed out. Pushing not possible.");
    }
    numberOfCoins++;
  }

  public void decrement() {
    if (isEmpty()) {
      throw new IllegalStateException("Container is empty.");
    }
    numberOfCoins--;
  }
}
