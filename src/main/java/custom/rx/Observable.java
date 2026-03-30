package custom.rx;

import custom.rx.interfaces.Disposable;
import custom.rx.interfaces.Observer;
import custom.rx.interfaces.Scheduler;
import custom.rx.observers.FilterObserver;
import custom.rx.observers.FlatMapObserver;
import custom.rx.observers.MapObserver;
import custom.rx.observers.ObserveOnObserver;
import custom.rx.observers.SafeObserver;
import java.util.Arrays;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;

/** Реактивный поток данных */
public class Observable<T> {
  private final OnSubscribe<T> onSubscribe;

  /** Интерфейс, описывающий логику подписки */
  public interface OnSubscribe<T> {
    void subscribe(Observer<T> observer) throws InterruptedException;
  }

  private Observable(OnSubscribe<T> onSubscribe) {
    this.onSubscribe = onSubscribe;
  }

  /** Создаёт Observable с произвольной логикой */
  public static <T> Observable<T> create(OnSubscribe<T> onSubscribe) {
    return new Observable<>(onSubscribe);
  }

  /** Создаёт Observable из нескольких элементов */
  @SafeVarargs
  public static <T> Observable<T> just(T... items) {
    return createFromIterable(Arrays.asList(items));
  }

  /** Создаёт Observable из коллекции */
  public static <T> Observable<T> fromIterable(Iterable<T> iterable) {
    return createFromIterable(iterable);
  }

  public Disposable subscribe(Observer<T> observer) {
    SafeObserver<T> safeObserver = new SafeObserver<>(observer);

    try {
      onSubscribe.subscribe(safeObserver);
    } catch (Exception e) {
      safeObserver.onError(e);
    }

    return safeObserver;
  }

  public Disposable subscribe(Consumer<T> onNext) {
    return subscribe(createObserver(onNext, Throwable::printStackTrace, () -> {}));
  }

  public Disposable subscribe(Consumer<T> onNext, Consumer<Throwable> onError) {
    return subscribe(createObserver(onNext, onError, () -> {}));
  }

  public Disposable subscribe(
      Consumer<T> onNext, Consumer<Throwable> onError, Runnable onComplete) {
    return subscribe(createObserver(onNext, onError, onComplete));
  }

  /** Преобразует каждый элемент по функции */
  public <R> Observable<R> map(Function<T, R> mapper) {
    return create(observer -> subscribe(new MapObserver<>(observer, mapper)));
  }

  /** Фильтрует элементы */
  public Observable<T> filter(Predicate<T> predicate) {
    return create(observer -> subscribe(new FilterObserver<>(observer, predicate)));
  }

  /** Задаёт поток для подписки */
  public Observable<T> subscribeOn(Scheduler scheduler) {
    return create(observer -> scheduler.execute(() -> Observable.this.subscribe(observer)));
  }

  /** Задаёт поток для обработки элементов */
  public Observable<T> observeOn(Scheduler scheduler) {
    return create(observer -> subscribe(new ObserveOnObserver<>(observer, scheduler)));
  }

  /** Преобразует элемент в Observable и объединяет результаты */
  public <R> Observable<R> flatMap(Function<T, Observable<R>> mapper) {
    return create(observer -> subscribe(new FlatMapObserver<>(observer, mapper)));
  }

  /** Вспомогательный метод - создаёт Observer из трёх обработчиков */
  private static <T> Observer<T> createObserver(
      Consumer<T> onNext, Consumer<Throwable> onError, Runnable onComplete) {
    return new Observer<T>() {
      @Override
      public void onNext(T item) {
        onNext.accept(item);
      }

      @Override
      public void onError(Throwable t) {
        onError.accept(t);
      }

      @Override
      public void onComplete() {
        onComplete.run();
      }
    };
  }

  /** Проверяет, отменена ли подписка у Observer */
  private static boolean isDisposed(Observer<?> observer) {
    return observer instanceof SafeObserver && ((SafeObserver<?>) observer).isDisposed();
  }

  /** Вспомогательный метод - создаёт Observable из Iterable */
  private static <T> Observable<T> createFromIterable(Iterable<T> iterable) {
    return create(
        observer -> {
          try {
            for (T item : iterable) {
              if (isDisposed(observer)) {
                break;
              }

              observer.onNext(item);
            }

            observer.onComplete();
          } catch (Exception e) {
            observer.onError(e);
          }
        });
  }
}
