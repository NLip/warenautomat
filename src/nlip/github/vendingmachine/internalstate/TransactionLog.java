package nlip.github.vendingmachine.internalstate;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

import nlip.github.vendingmachine.values.Transaction;

public final class TransactionLog {
  private final List<Transaction> transactions;

  public TransactionLog() {
    this.transactions = new ArrayList<>();
  }

  public Stream<Transaction> stream() {
    return transactions.stream();
  }

  public void addTransaction(Transaction transaction) {
    transactions.add(transaction);
  }
}
