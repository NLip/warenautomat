package values;

import warenautomat.SystemSoftware;

public class RegisterDisplay {
  public void display(CustomerProblem problem) {
    switch (problem) {
      case INSUFFICIENT_BALANCE:
        SystemSoftware.zeigeZuWenigGeldAn();
        return;
      case INSUFFICIENT_CHANGE:
        SystemSoftware.zeigeZuWenigWechselGeldAn();
        return;
      default:
    }
  }
}
