package fr.umlv.loom;

import java.util.Objects;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

@FunctionalInterface
public interface Task<T> extends Future<T> {
  T await();
  
  @Override
  default T get() {
    return await();
  }
  @Override
  default T get(long timeout, TimeUnit unit) {
    throw new UnsupportedOperationException();
  }
  @Override
  default boolean isDone() {
    throw new UnsupportedOperationException();
  }
  @Override
  default boolean cancel(boolean mayInterruptIfRunning) {
    throw new UnsupportedOperationException();
  }
  @Override
  default boolean isCancelled() {
    throw new UnsupportedOperationException();
  }
  
  public static <T> Task<T> async(Supplier<? extends T> supplier) {
    return new Task<>() {
      private final Fiber fiber = Fiber.execute(() -> result = Objects.requireNonNull(supplier.get()));
      private volatile T result;
      
      @Override
      public T await() {
        fiber.await();
        return result;
      }
      
      @Override
      public T get(long timeout, TimeUnit unit) {
        fiber.awaitNanos(unit.toNanos(timeout));
        T result = this.result;
        if (result != null) {
          return result;
        }
        throw new IllegalStateException("timeout");
      }
      
      public boolean isDone() {
        return result != null;
      }
    };
  }
}