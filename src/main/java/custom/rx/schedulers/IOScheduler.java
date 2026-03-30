package custom.rx.schedulers;

import custom.rx.interfaces.Scheduler;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/** Планировщик, использующий кэшированный пул потоков для операций ввода-вывода */
public class IOScheduler implements Scheduler {
  private final ExecutorService executor = Executors.newCachedThreadPool();

  @Override
  public void execute(Runnable task) {
    executor.submit(task);
  }
}
