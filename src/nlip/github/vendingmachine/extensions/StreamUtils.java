package nlip.github.vendingmachine.extensions;

import java.util.stream.IntStream;

public final class StreamUtils {
  private StreamUtils() {}
  
  public static void repeat(int n, Runnable runnable) {
	  IntStream.range(1, n).forEach(__ -> runnable.run());
  }
}
