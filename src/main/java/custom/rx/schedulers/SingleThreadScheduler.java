package custom.rx.schedulers;

import custom.rx.interfaces.Scheduler;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/** Планировщик, использующий один поток для последовательного выполнения операций */
public class SingleThreadScheduler implements Scheduler {
  private final ExecutorService executor = Executors.newSingleThreadExecutor();

  @Override
  public void execute(Runnable task) {
    executor.submit(task);
  }
}
