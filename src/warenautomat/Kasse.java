package warenautomat;

import exceptions.InvalidOperationException;
import java.math.BigDecimal;
import state.Register;
import state.TransactionLog;
import values.Denomination;
import values.RegisterUpdate;
import values.Transaction;

/**
 * Die Kasse verwaltet das eingenommene Geld sowie das Wechselgeld. <br>
 * Die Kasse hat fünf Münz-Säulen für: <br>
 * - 10 Rappen <br>
 * - 20 Rappen <br>
 * - 50 Rappen <br>
 * - 1 Franken <br>
 * - 2 Franken <br>
 */
public class Kasse {
	private final static int DENOMINATION_NOT_SUPPORTED = -200;
	private final Register register;
	private final TransactionLog transactionLog;
  /**
   * Standard-Konstruktor. <br>
   * Führt die nötigen Initialisierungen durch.
   */
  public Kasse() {
    this.register = new Register();
    this.transactionLog = new TransactionLog();
  }

  /**
   * Diese Methode wird aufgerufen nachdem das Personal beim Verwalten des
   * Wechselgeldbestand die Münzart und die Anzahl der Münzen über die
   * Tastatur eingegeben hat 
   * (siehe Use-Case "Wechselgeldbestand (Münzbestand) verwalten").
   * 
   * @param pMuenzenBetrag Der Betrag der Münzart in Franken.
   * @param pAnzahl Die Anzahl der Münzen. Bei der Entnahme von Münzen als
   *                entsprechender negativer Wert.
   * @return Anzahl der Münzen welche hinzugefügt resp. entnommen werden (bei
   *         Entnahme als negativer Wert). <br>
   *         Im Normalfall entspricht dieser Wert dem Übergabeparameter 
   *         <code>pAnzahl</code>. <br> 
   *         Er kann kleiner sein falls beim Hinzufügen in der Münzsäule zu 
   *         wenig Platz vorhanden ist oder wenn bei der Entnahme ein grössere 
   *         Anzahl angegeben wurde als tatsächlich in der Münzsäule vorhanden 
   *         ist. <br>
   *         Wenn ein nicht unterstützter Münzbetrag übergeben wurde: -200
   */
  public int verwalteMuenzbestand(double pMuenzenBetrag, int pAnzahl) {
	  var maybeDenomination = Denomination.fromDouble(pMuenzenBetrag);
	  if (maybeDenomination.isEmpty()) {
		  return DENOMINATION_NOT_SUPPORTED;
	  }
	  var registerUpdate = new RegisterUpdate(maybeDenomination.get(), pAnzahl);
	  return register.propose(registerUpdate).getNumberOfCoins();
  } 

  /**
   * Diese Methode wird aufgerufen nachdem das Personal beim Geldauffüllen den
   * Knopf "Bestätigen" gedrückt hat
   * (siehe Use-Case "Wechselgeldbestand (Münzbestand) verwalten"). <br>
   * Verbucht die Münzen gemäss dem vorangegangenen Aufruf der Methode 
   * <code>verwalteMuenzbestand()</code>.
   */
  public void verwalteMuenzbestandBestaetigung() {
    register.commitProposedUpdate();
  }
 
  /**
   * Diese Methode wird aufgerufen wenn ein Kunde eine Münze eingeworfen hat. <br>
   * Führt den eingenommenen Betrag entsprechend nach. <br>
   * Stellt den nach dem Einwerfen vorhandenen Betrag im Kassen-Display dar. <br>
   * Eingenommenes Geld steht sofort als Wechselgeld zur Verfügung. <br>
   * Die Münzen werden von der Hardware-Kasse auf Falschgeld, Fremdwährung und
   * nicht unterstützte Münzarten geprüft, d.h. diese Methode wird nur
   * aufgerufen wenn ein Münzeinwurf soweit erfolgreich war. <br>
   * Ist die Münzsäule voll (d.h. 100 Münzen waren vor dem Einwurf bereits darin
   * enthalten), so wird mittels
   * <code> SystemSoftware.auswerfenWechselGeld() </code> unmittelbar ein
   * entsprechender Münz-Auswurf ausgeführt. <br>
   * Hinweis: eine Hardware-Münzsäule hat jeweils effektiv Platz für 101 Münzen.
   * 
   * @param pMuenzenBetrag Der Betrag der neu eingeworfenen Münze in Franken.
   * @return <code> true </code>, wenn er Einwurf erfolgreich war. <br>
   *         <code> false </code>, wenn Münzsäule bereits voll war.
   */
  public boolean einnehmen(double pMuenzenBetrag) {
      Denomination denomination =
          Denomination.fromDouble(pMuenzenBetrag)
              .orElseThrow(() -> new InvalidOperationException("This coin is not supported."));

      return register.tryPutCoin(denomination);
    
  }

  /**
   * Bewirkt den Auswurf des Restbetrages.
   */
  public void gibWechselGeld() {
    register.returnCredit();
  }

  /**
   * Gibt den Gesamtbetrag der bisher verkauften Waren zurück. <br>
   * Analyse: Abgeleitetes Attribut.
   * 
   * @return Gesamtbetrag der bisher verkauften Waren.
   */
  public double gibBetragVerkaufteWaren() {
      BigDecimal sumOfAllIncome = transactionLog.getTransactions()
          .stream()
          .map(Transaction::getPriceAtDate)
          .reduce(BigDecimal.ZERO, BigDecimal::add);
      return sumOfAllIncome.doubleValue();
  }

    public Register getRegister() {
        return register;
    }

    public TransactionLog getTransactionLog() {
        return transactionLog;
    }
}
