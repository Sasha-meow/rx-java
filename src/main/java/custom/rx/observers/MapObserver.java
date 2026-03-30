package custom.rx.observers;

import custom.rx.interfaces.Observer;
import java.util.function.Function;

/** Оператор map - применяет функцию к каждому элементу */
public class MapObserver<T, R> extends BaseObserver<T, R> {
  private final Function<T, R> mapper;

  public MapObserver(Observer<R> subscriber, Function<T, R> mapper) {
    super(subscriber);
    this.mapper = mapper;
  }

  @Override
  public void onNext(T item) {
    try {
      subscriber.onNext(mapper.apply(item));
    } catch (Exception e) {
      subscriber.onError(e);
    }
  }
}
