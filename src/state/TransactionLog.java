package state;

import values.Transaction;
import java.util.ArrayList;
import java.util.List;

public final class TransactionLog {
  private List<Transaction> transactions;

  public TransactionLog() {
    this.transactions = new ArrayList<>();
  }

  public List<Transaction> getTransactions() {
    return new ArrayList<>(transactions);
  }

  public void addTransaction(Transaction transaction) {
    transactions.add(transaction);
  }
}
