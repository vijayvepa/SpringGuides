package com.example.webfluxexample;

import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * BackgroundSteps provides a declarative mechanism for specifying
 * a series of test events to be sequentially executed on a background thread.
 * <ul> <li>Create a BackgroundSteps builder by calling BackgroundSteps.create().</li>
 * <li>Add steps to the builder.
 *   <ol>
 *     <ul>wait(duration) - wait for a duration before proceeding to the next step</ul>
 *     <ul>then(runnable) - execute a runnable block</ul>
 *   </ol></li>
 *  <li>Call run() to start the background thread running.</li>
 * </ul>
 * The primary use-case for this is to publish kafka messages asynchronously to a
 * test that is using a Flux to consume the Kafka topic.
 *
 */
public class BackgroundSteps {

  private static final Logger log = LoggerFactory.getLogger(BackgroundSteps.class);
  /**
   * Shared step definition
   */
  private interface Step {
    void doStep() throws Exception;
  }

  /**
   * The main builder class/
   */
  public static class StepBuilder {

    final List<Step> steps = new ArrayList<>();
    Consumer<Exception> onError = null;

    /**
     * A step that waits for a defined duration.
     *
     * @param duration dur
     * @return step builder
     */
    public StepBuilder wait(final Duration duration) {
      this.steps.add(new WaitStep(duration));
      return this;
    }

    /**
     * A step that waits for the system to be ready, before we send values.
     *
     * @return step builder
     */
    public StepBuilder waitTillReady() {
      // We don't currently have a good way of telling the system is ready,
      // so we just delay.  But if we ever do then this would be the place to
      // use it.
      this.steps.add(new WaitStep(Duration.of(10, ChronoUnit.SECONDS)));
      return this;
    }

    /**
     * A step that runs custom code.
     *
     * @param t runnable
     * @return step builder
     */
    public StepBuilder then(final Runnable t) {
      this.steps.add(new ExecStep(t));
      return this;
    }

    /**
     * Register an exception handler with the runner.
     *
     * @param consumer handles the exception
     * @return step builder
     */
    public StepBuilder onError(final Consumer<Exception> consumer) {
      this.onError = consumer;
      return this;
    }


    /**
     * Execute the steps on a backrgound thread.
     */
    public void run() {

      final Thread t = new Thread(() -> {
        try {
          for (final Step step : this.steps) {
            step.doStep();
          }
        } catch (final Exception exception) {
          log.error("Background Step failure", exception);
          if (this.onError != null) {
            this.onError.accept(exception);
          } else {
            throw new RuntimeException(exception);
          }
        }
      });
      t.start();
    }
  }

  private static class WaitStep implements Step {

    private final Duration duration;

    public WaitStep(final Duration duration) {
      this.duration = duration;
    }

    @Override
    public void doStep() throws Exception {
      log.debug("Waiting for {} seconds", this.duration.getSeconds());
      Thread.sleep(this.duration.toMillis());
      log.debug("Finished waiting for {} seconds", this.duration.getSeconds());
    }
  }

  private static class ExecStep implements Step {

    private final Runnable runnable;

    public ExecStep(final Runnable runnable) {
      this.runnable = runnable;
    }

    @Override
    public void doStep() throws Exception {
      log.debug("Running step ------------------");
      this.runnable.run();
    }
  }

  /**
   * Start the process of creating background steps.
   *
   * @return steps builder.
   */
  public static StepBuilder create() {
    return new StepBuilder();
  }


}
