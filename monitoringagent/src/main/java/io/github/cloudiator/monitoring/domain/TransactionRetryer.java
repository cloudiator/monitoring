package io.github.cloudiator.monitoring.domain;

import com.github.rholder.retry.RetryException;
import com.github.rholder.retry.Retryer;
import com.github.rholder.retry.RetryerBuilder;
import com.github.rholder.retry.StopStrategies;
import com.github.rholder.retry.WaitStrategies;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

public class TransactionRetryer {

  private final static int TIME = 5;
  private final static int ATMS = 5;


  private TransactionRetryer() {
    throw new AssertionError("Do not instantiate");
  }


  public static <T> T retry(Integer mintime, Integer maxtime, Integer attempts,
      Callable<T> callable) {
    Retryer<T> retryer = RetryerBuilder.<T>newBuilder().retryIfRuntimeException()
        .withWaitStrategy(
            WaitStrategies
                .randomWait(mintime, TimeUnit.MILLISECONDS, maxtime, TimeUnit.MILLISECONDS))
        .withStopStrategy(
            StopStrategies.stopAfterAttempt(attempts)).build();

    try {
      return retryer.call(callable);
    } catch (ExecutionException e) {
      throw new IllegalStateException("Execution failed with cause : " + e.getCause().getMessage(),
          e.getCause());
    } catch (RetryException e) {
      throw new IllegalStateException("Retrying finally failed.", e);
    }
  }

}
