package process;

import state.Register;
import state.RotaryPlate;
import values.CustomerProblem;
import java.util.List;
import java.util.Optional;

// TODO: This classes job is to trigger the right methods on rotary plate / register when buttons
// are pressed.
@Deprecated
public class CustomerMode {
  // TODO: Replace these with 'Machine' instance once that class is made
  private final List<RotaryPlate> rotaryPlates;
  private final Register register;

  public CustomerMode(List<RotaryPlate> rotaryPlates, Register register) {
    this.rotaryPlates = rotaryPlates;
    this.register = register;
  }

  public void open(int plateNumber) {
    var plate = rotaryPlates.get(plateNumber);

    var problem = proposeOpening(plate);
    if (problem.isPresent()) {
      display(problem.get());
      return;
    }

    register.purchase(plate.getProductOrThrow().getPrice());
    plate.unlock();
  }

  private void display(CustomerProblem customerProblem) {}

  public void close(int plateNumber) {
    var plate = rotaryPlates.get(plateNumber);
    plate.removeProduct();
    plate.lock();
  }

  private Optional<CustomerProblem> proposeOpening(RotaryPlate plate) {
    return plate
        .proposePurchase()
        .or(() -> register.proposePurchase(plate.getProductOrThrow().getPrice()));
  }
}
