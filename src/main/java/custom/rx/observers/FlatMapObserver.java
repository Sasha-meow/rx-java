package custom.rx.observers;

import custom.rx.Observable;
import custom.rx.interfaces.Observer;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;

/** Оператор flatMap - объединяет несколько потоков */
public class FlatMapObserver<T, R> implements Observer<T> {
  private final Observer<R> subscriber;
  private final Function<T, Observable<R>> mapper;
  AtomicInteger pending = new AtomicInteger(1); // Счётчик активных потоков
  AtomicBoolean isTerminated = new AtomicBoolean(false); // Флаг завершения

  public FlatMapObserver(Observer<R> subscriber, Function<T, Observable<R>> mapper) {
    this.subscriber = subscriber;
    this.mapper = mapper;
  }

  @Override
  public void onNext(T item) {
    try {
      Observable<R> inner = mapper.apply(item);
      pending.incrementAndGet();

      inner.subscribe(new InnerObserver());
    } catch (Exception e) {
      if (isTerminated.compareAndSet(false, true)) {
        subscriber.onError(e);
      }
    }
  }

  @Override
  public void onError(Throwable t) {
    if (isTerminated.compareAndSet(false, true)) {
      subscriber.onError(t);
    }
  }

  @Override
  public void onComplete() {
    if (pending.decrementAndGet() == 0 && !isTerminated.get()) {
      subscriber.onComplete();
    }
  }

  private class InnerObserver implements Observer<R> {
    @Override
    public void onNext(R r) {
      subscriber.onNext(r);
    }

    @Override
    public void onError(Throwable t) {
      if (isTerminated.compareAndSet(false, true)) {
        subscriber.onError(t);
      }
    }

    @Override
    public void onComplete() {
      if (pending.decrementAndGet() == 0 && !isTerminated.get()) {
        subscriber.onComplete();
      }
    }
  }
}
