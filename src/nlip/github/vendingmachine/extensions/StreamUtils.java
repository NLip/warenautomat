package nlip.github.vendingmachine.extensions;

import java.util.stream.IntStream;

public final class StreamUtils {
  private StreamUtils() {}
  
  // TODO: Tell that this is equivalent to for(int i = 0; i <= n; ++i) runnable.run()
  public static void repeat(int n, Runnable runnable) {
	  IntStream.rangeClosed(1, n).forEach(__ -> runnable.run());
  }
}
