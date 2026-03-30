package custom.rx.interfaces;

/** Наблюдатель - получает события из Observable */
public interface Observer<T> {
  /** Вызывается при получении нового элемента */
  void onNext(T item);

  /** Вызывается при возникновении ошибки */
  void onError(Throwable t);

  /** Вызывается при успешном завершении потока */
  void onComplete();
}
