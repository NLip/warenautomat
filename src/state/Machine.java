package state;

import static java.util.stream.Collectors.toList;
import static java.util.stream.Stream.generate;

import java.lang.annotation.Repeatable;

import values.CustomerProblem;
import values.ProblemToDisplayRouter;
import values.Product;
import values.RegisterDisplay;
import values.Transaction;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import warenautomat.SystemSoftware;

/**
 * Der Automat besteht aus 7 Drehtellern welche wiederum je aus 16 Fächer bestehen. <br>
 * Der erste Drehteller und das jeweils erste Fach haben jeweils die Nummer 1 (nicht 0!). <br>
 * Im Weitern hat der Automat eine Kasse. Diese wird vom Automaten instanziert.
 */
public final class Machine {

  private static final int NUMBER_OF_ROTARY_PLATES = 7;
  private final List<RotaryPlate> rotaryPlates;
  private final Register register;
  private final RegisterDisplay registerDisplay;
  private final List<Transaction> transactions; // TODO: Shouldn't this be a TransactionLog instance?

  /**
   * Der Standard-Konstruktor. <br>
   * Führt die nötigen Initialisierungen durch (u.a. wird darin die Kasse instanziert).
   */
  public Machine(Register register) {
    this.register = register;
    this.rotaryPlates = generate(RotaryPlate::new).limit(NUMBER_OF_ROTARY_PLATES).collect(toList());
    this.registerDisplay = new RegisterDisplay();
  }

  /**
   * Füllt ein Fach mit Ware. <br>
   * Wenn das Service-Personal den Automaten füllt, wird mit einem Bar-Code-Leser zuerst die Ware
   * gescannt. <br>
   * Daraufhin wird die Schiebe-Tür geöffnet. <br>
   * Das Service-Personal legt die neue Ware ins Fach und schliesst das Fach. <br>
   * Die Hardware resp. System-Software ruft die Methode <code> Automat.neueWareVonBarcodeLeser()
   * </code> auf.
   *
   * @param plateNumber Der Drehteller bei welchem das Fach hinter der Schiebe-Türe gefüllt wird.
   *     <br>
   *     Nummerierung beginnt mit 1 (nicht 0)!
   * @param name Der Name der neuen Ware.
   * @param price Der Preis der neuen Ware.
   * @param bestBeforeDate Das Verfallsdatum der neuen Ware.
   */
  public void readNewProduct(int plateNumber, String name, double price, LocalDate bestBeforeDate) {
    Product newProduct = new Product(name, BigDecimal.valueOf(price), bestBeforeDate);
    rotaryPlates.get(plateNumber - 1).insertProduct(newProduct);
  }

  /** Gibt die Objekt-Referenz auf die <em> Kasse </em> zurück. */
  public Register getRegister() {
    return register;
  }

  /**
   * Wird von der System-Software jedesmal aufgerufen wenn der gelbe Dreh-Knopf gedrückt wird. <br>
   * Die Applikations-Software führt die Drehteller-Anzeigen nach (Warenpreis, Verfallsdatum). <br>
   * Das Ansteuern des Drehteller-Motors übernimmt die System-Software (muss nicht von der
   * Applikations-Software gesteuert werden.). <br>
   * Die System-Software stellt sicher, dass <em> drehen </em> nicht durchgeführt wird wenn ein Fach
   * offen ist.
   */
  public void rotatePlates() {
    rotaryPlates.forEach(RotaryPlate::rotatePlate);
  }

  /**
   * Beim Versuch eine Schiebetüre zu öffnen ruft die System-Software die Methode <code> oeffnen()
   * </code> der Klasse <em> Automat </em> mit der Drehteller-Nummer als Parameter auf. <br>
   * Es wird überprüft ob alles o.k. ist: <br>
   * - Fach nicht leer <br>
   * - Verfallsdatum noch nicht erreicht <br>
   * - genug Geld eingeworfen <br>
   * - genug Wechselgeld vorhanden <br>
   * Wenn nicht genug Geld eingeworfen wurde, wird dies mit <code>
   *  SystemSoftware.zeigeZuWenigGeldAn() </code> signalisiert. <br>
   * Wenn nicht genug Wechselgeld vorhanden ist wird dies mit <code>
   *  SystemSoftware.zeigeZuWenigWechselGeldAn() </code> signalisiert. <br>
   * Wenn o.k. wird entriegelt (<code> SystemSoftware.entriegeln() </code>) und positives Resultat
   * zurückgegeben, sonst negatives Resultat. <br>
   * Es wird von der System-Software sichergestellt, dass zu einem bestimmten Zeitpunkt nur eine
   * Schiebetüre offen sein kann.
   *
   * @param plateNumber Der Drehteller bei welchem versucht wird die Schiebe-Türe zu öffnen. <br>
   *     Nummerierung beginnt mit 1 (nicht 0)!
   * @return Wenn alles o.k. <code> true </code>, sonst <code> false </code>.
   */
  public boolean openRotaryPlate(int plateNumber) {
	  // TODO: Why not forward to customer mode?
    var plate = rotaryPlates.get(plateNumber - 1);

    var problem = proposeOpening(plate);
    if (problem.isPresent()) {
      registerDisplay.display(problem.get());
      return false;
    }

    BigDecimal price = plate.getProductOrThrow().getPrice();
    String productName = plate.getProductOrThrow().getName();
    register.purchase(price);
    //logTransaction(new Transaction(productName, price, SystemSoftware.gibAktuellesDatum()));
    //this doesn't belong here
    SystemSoftware.entriegeln(plateNumber);
    return true;
  }

  private Optional<CustomerProblem> proposeOpening(RotaryPlate plate) {
    return plate
        .proposePurchase()
        .or(() -> register.proposePurchase(plate.getProductOrThrow().getPrice()));
  }

  /**
   * Gibt den aktuellen Wert aller im Automaten enthaltenen Waren in Franken zurück. <br>
   * Analyse: <br>
   * Abgeleitetes Attribut. <br>
   *
   * @return Der totale Warenwert des Automaten.
   */
  public double computeTotalOfProducts() {
    return rotaryPlates
        .stream()
        .flatMap(RotaryPlate::getAllProducts)
        .map(Product::getPrice)
        .reduce(BigDecimal.ZERO, BigDecimal::add)
        .doubleValue();
  }

  /**
   * Gibt die Anzahl der verkauften Ware <em> pName </em> seit (>=) <em> pDatum </em> zurück.
   *
   * @param name Der Name der Ware nach welcher gesucht werden soll.
   * @param date Das Datum seit welchem gesucht werden soll.
   * @return Anzahl verkaufter Waren.
   */
  public int numberOfSalesPerProductSince(String name, LocalDate date) {
    return (int)
        transactions
            .stream()
            .filter(transaction -> transaction.getProductName().equalsIgnoreCase(name))
            .filter(transaction -> !transaction.getDate().isBefore(date))
            .count();
  }
}
