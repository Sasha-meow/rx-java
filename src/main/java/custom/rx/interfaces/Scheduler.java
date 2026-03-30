package custom.rx.interfaces;

import custom.rx.schedulers.ComputationScheduler;
import custom.rx.schedulers.IOScheduler;
import custom.rx.schedulers.SingleThreadScheduler;

/** Интерфейс планировщика потоков */
public interface Scheduler {
  /** Выполняет задачу в пуле потоков планировщика */
  void execute(Runnable task);

  /** Создаёт IO планировщик */
  static Scheduler io() {
    return new IOScheduler();
  }

  /** Создаёт Computation планировщик */
  static Scheduler computation() {
    return new ComputationScheduler();
  }

  /** Создаёт SingleThread планировщик */
  static Scheduler single() {
    return new SingleThreadScheduler();
  }
}
