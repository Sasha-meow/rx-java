package custom.rx.schedulers;

import custom.rx.interfaces.Scheduler;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/** Планировщик, использующий пул потоков фиксированного размера для вычислительных операций */
public class ComputationScheduler implements Scheduler {
  private final ExecutorService executor =
      Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());

  @Override
  public void execute(Runnable task) {
    executor.submit(task);
  }
}
