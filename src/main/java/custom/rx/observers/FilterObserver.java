package custom.rx.observers;

import custom.rx.interfaces.Observer;
import java.util.function.Predicate;

/** Оператор filter - проверяет элементы условием */
public class FilterObserver<T> extends BaseObserver<T, T> {
  private final Predicate<T> predicate;

  public FilterObserver(Observer<T> subscriber, Predicate<T> predicate) {
    super(subscriber);
    this.predicate = predicate;
  }

  @Override
  public void onNext(T item) {
    try {
      if (predicate.test(item)) {
        subscriber.onNext(item);
      }
    } catch (Exception e) {
      subscriber.onError(e);
    }
  }
}
