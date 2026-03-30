package custom.rx;

import custom.rx.interfaces.Disposable;
import custom.rx.interfaces.Scheduler;
import java.util.Arrays;
import java.util.List;

public class Main {
  public static void main(String[] args) throws InterruptedException {
    System.out.println("Демонстрация работы кастомной RxJava");

    demoBasicComponents();
    demoMapAndFilter();
    demoSchedulers();
    demoDisposable();
  }

  private static void demoBasicComponents() {
    System.out.println("1. Базовые компоненты создания");

    System.out.println("-> create()");
    Observable.create(
            observer -> {
              observer.onNext(10);
              observer.onNext(20);
              observer.onNext(30);
              observer.onComplete();
            })
        .subscribe(
            item -> System.out.println("  Получено: " + item),
            Throwable::printStackTrace,
            () -> System.out.println("  Поток завершён"));
    System.out.println();

    System.out.println("-> just()");
    Observable.just(1, 2, 3).subscribe(item -> System.out.println("  Число: " + item));
    System.out.println();

    System.out.println("-> fromIterable()");
    List<String> names = Arrays.asList("A", "B", "C");
    Observable.fromIterable(names).subscribe(name -> System.out.println("  Буква: " + name));
    System.out.println();
  }

  private static void demoMapAndFilter() {
    System.out.println("2. Базовые операторы");

    System.out.println("-> map()");
    Observable.just(1, 2, 3).map(x -> "Число: " + x).subscribe(System.out::println);
    System.out.println();

    System.out.println("-> filter()");
    Observable.just(1, 2, 3, 4, 5, 6, 7, 8, 9, 10)
        .filter(x -> x % 2 == 0)
        .subscribe(x -> System.out.println("  Чётное: " + x));
    System.out.println();

    System.out.println("-> map() + filter()");
    Observable.just(1, 2, 3, 4, 5, 6)
        .filter(x -> x % 2 == 0)
        .map(x -> x * 10)
        .subscribe(x -> System.out.println("  Результат: " + x));
    System.out.println();

    System.out.println("-> flatMap()");
    Observable.just("1,2,3", "4,5,6", "7,8,9")
        .flatMap(
            s ->
                Observable.fromIterable(
                    Arrays.stream(s.split(",")).map(Integer::parseInt).toList()))
        .subscribe(x -> System.out.print(x + " "));
    System.out.println();
  }

  private static void demoSchedulers() throws InterruptedException {
    System.out.println("3. Управление потоками");

    System.out.println("-> subscribeOn (подписка в IO потоке)");
    Observable.<String>create(
            observer -> {
              System.out.println("  Генерация в потоке: " + Thread.currentThread().getName());
              observer.onNext("Привет");
              observer.onComplete();
            })
        .subscribeOn(Scheduler.io())
        .subscribe(
            item -> System.out.println("  Получено: " + item),
            Throwable::printStackTrace,
            () -> System.out.println("  Завершено"));

    Thread.sleep(500);

    System.out.println("-> observeOn (обработка в Single потоке)");
    Observable.just(1, 2, 3)
        .observeOn(Scheduler.single())
        .map(
            x -> {
              System.out.println(
                  "  Обработка " + x + " в потоке: " + Thread.currentThread().getName());
              return x * 10;
            })
        .subscribe(x -> System.out.println("  Результат: " + x));

    Thread.sleep(500);
    System.out.println();

    System.out.println("-> subscribeOn + observeOn");
    Observable.<Integer>create(
            observer -> {
              System.out.println("  Генерация в потоке: " + Thread.currentThread().getName());
              observer.onNext(100);
              observer.onNext(200);
              observer.onNext(300);
              observer.onComplete();
            })
        .subscribeOn(Scheduler.io())
        .observeOn(Scheduler.computation())
        .map(
            x -> {
              System.out.println("  Обработка в потоке: " + Thread.currentThread().getName());
              return x / 10;
            })
        .subscribe(
            item -> System.out.println("  Результат: " + item),
            Throwable::printStackTrace,
            () -> System.out.println("  Завершено"));

    Thread.sleep(1000);
    System.out.println();
  }

  private static void demoDisposable() throws InterruptedException {
    System.out.println("4. Отмена подписки (dispose)");

    Disposable disposable =
        Observable.<Integer>create(
                observer -> {
                  for (int i = 0; i < 10; i++) {
                    observer.onNext(i);
                    Thread.sleep(200);
                  }
                  observer.onComplete();
                })
            .subscribe(item -> System.out.println(" " + item), Throwable::printStackTrace);

    Thread.sleep(500);
    System.out.println("Отмена подписки...");
    disposable.dispose();
    System.out.println("Подписка отменена.");

    Thread.sleep(500);
    System.out.println();
  }
}
