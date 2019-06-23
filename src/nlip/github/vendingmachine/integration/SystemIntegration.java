package nlip.github.vendingmachine.integration;

import java.math.BigDecimal;
import java.time.LocalDate;

import nlip.github.vendingmachine.values.Denomination;
import warenautomat.SystemSoftware;

public final class SystemIntegration {
	  
  private SystemIntegration() {}
  

  public static LocalDate getToday() {
    return SystemSoftware.gibAktuellesDatum();
  }


  public static void showProductPrice(int plateNumber, BigDecimal price) {
    SystemSoftware.zeigeWarenPreisAn(plateNumber, price.doubleValue());
  }


  public static void showExpiryStatus(int plateNumber, ExpiryStatus status) {
    SystemSoftware.zeigeVerfallsDatum(plateNumber, status.getSystemCode());
  }

  public static void showAmount(BigDecimal amount) {
    SystemSoftware.zeigeBetragAn(amount.doubleValue());
  }

 
  public static void showInsufficientFunds() {
    SystemSoftware.zeigeZuWenigGeldAn();
  }


  public static void showInsufficientChange() {
    SystemSoftware.zeigeZuWenigWechselGeldAn();
  }


  public static void unlock(int plateNumber) {
    SystemSoftware.entriegeln(plateNumber);
  }

  public static void releaseChange(Denomination denomination) {
    SystemSoftware.auswerfenWechselGeld(denomination.getValue().doubleValue());
  }
  
  public enum ExpiryStatus {
	  OFF(0),
	  OK(1),
	  EXPIRED(2);
	  
	  private final int systemCode;
	  
	  private ExpiryStatus(int systemCode) {
		  this.systemCode = systemCode;
	  }
	  
	  public int getSystemCode() {
		  return systemCode;
	  }
  }
}
