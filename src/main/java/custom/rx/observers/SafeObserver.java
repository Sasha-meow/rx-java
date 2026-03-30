package custom.rx.observers;

import custom.rx.interfaces.Disposable;
import custom.rx.interfaces.Observer;
import java.util.concurrent.atomic.AtomicBoolean;

/** Безопасная обёртка с синхронизацией и поддержкой отмены */
public class SafeObserver<T> implements Observer<T>, Disposable {
  private final Observer<T> actual; // подписчик, которому перенаправляются события
  private final AtomicBoolean disposed = new AtomicBoolean(false); // флаг отмены подписки
  private final Object lock = new Object();

  public SafeObserver(Observer<T> actual) {
    this.actual = actual;
  }

  @Override
  public void onNext(T item) {
    synchronized (lock) {
      if (!isDisposed()) {
        actual.onNext(item);
      }
    }
  }

  @Override
  public void onError(Throwable t) {
    synchronized (lock) {
      if (!isDisposed()) {
        actual.onError(t);
        dispose();
      }
    }
  }

  @Override
  public void onComplete() {
    synchronized (lock) {
      if (!isDisposed()) {
        actual.onComplete();
        dispose();
      }
    }
  }

  @Override
  public boolean isDisposed() {
    return disposed.get();
  }

  @Override
  public void dispose() {
    disposed.set(true);
  }
}
