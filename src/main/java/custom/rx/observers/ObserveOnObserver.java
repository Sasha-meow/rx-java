package custom.rx.observers;

import custom.rx.interfaces.Observer;
import custom.rx.interfaces.Scheduler;

/** Оператор observeOn - переключает поток выполнения */
public class ObserveOnObserver<T> extends BaseObserver<T, T> {
  private final Scheduler scheduler;

  public ObserveOnObserver(Observer<T> subscriber, Scheduler scheduler) {
    super(subscriber);
    this.scheduler = scheduler;
  }

  @Override
  public void onNext(T item) {
    scheduler.execute(() -> subscriber.onNext(item));
  }

  @Override
  public void onError(Throwable t) {
    scheduler.execute(() -> subscriber.onError(t));
  }

  @Override
  public void onComplete() {
    scheduler.execute(subscriber::onComplete);
  }
}
