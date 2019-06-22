package extensions;

import java.util.function.IntConsumer;
import java.util.stream.IntStream;
import java.util.stream.Stream;

public final class StreamUtils {
  private StreamUtils() {}
  
  public static void repeat(int n, Runnable runnable) {
	  IntStream.range(1, n).forEach(__ -> runnable.run());
  }
}
