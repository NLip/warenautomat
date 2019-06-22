package exceptions;

// TODO: Docstring. Exception signals that an attempted operation cannot be performed at this
// moement
// Might be recoverable.
public class InvalidOperationException extends RuntimeException {
  public InvalidOperationException(String message) {
    super(message);
  }
}
