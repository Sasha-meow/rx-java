package custom.rx.observers;

import custom.rx.interfaces.Observer;

/** Базовый класс, пробрасывает onError/onComplete дальше */
public abstract class BaseObserver<T, R> implements Observer<T> {
  protected final Observer<R> subscriber; // Следующий подписчик

  public BaseObserver(Observer<R> subscriber) {
    this.subscriber = subscriber;
  }

  @Override
  public void onError(Throwable t) {
    subscriber.onError(t);
  }

  @Override
  public void onComplete() {
    subscriber.onComplete();
  }
}
