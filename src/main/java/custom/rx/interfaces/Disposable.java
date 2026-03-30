package custom.rx.interfaces;

/** Интерфейс отмены подписки */
public interface Disposable {
  /** Отменяет подписку */
  void dispose();

  /** Проверяет, отменена ли подписка */
  boolean isDisposed();
}
