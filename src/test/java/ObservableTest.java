import static org.junit.jupiter.api.Assertions.*;

import custom.rx.Observable;
import custom.rx.interfaces.Disposable;
import custom.rx.interfaces.Scheduler;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

@DisplayName("Observable Тесты")
class ObservableTest {
  private final String TEST_ERROR = "Текст тестовой ошибки";
  private static final int TIMEOUT_SECONDS = 3;
  private static final int SHORT_SLEEP_MS = 50;
  private static final int MEDIUM_SLEEP_MS = 120;

  @Nested
  @DisplayName("Базовые компоненты")
  class BasicComponentsTests {
    @Test
    @DisplayName("create() должен создавать Observable с переданной логикой")
    void testCreate() {
      List<Integer> result = new ArrayList<>();

      Observable<Integer> observable =
          Observable.create(
              observer -> {
                observer.onNext(1);
                observer.onNext(2);
                observer.onComplete();
              });

      observable.subscribe(result::add);

      assertEquals(Arrays.asList(1, 2), result);
    }

    @Test
    @DisplayName("just() должен создавать поток из переданных элементов")
    void testJust() {
      List<Integer> result = new ArrayList<>();

      Observable.just(1, 2, 3).subscribe(result::add);

      assertEquals(Arrays.asList(1, 2, 3), result);
    }

    @Test
    @DisplayName("fromIterable() должен создавать поток из коллекции")
    void testFromIterable() {
      List<String> result = new ArrayList<>();
      List<String> input = Arrays.asList("A", "B", "C");

      Observable.fromIterable(input).subscribe(result::add);

      assertEquals(input, result);
    }

    @Test
    @DisplayName("subscribe() с Consumer должен работать без ошибок")
    void testSubscribeWithConsumer() {
      List<Integer> result = new ArrayList<>();

      Observable.just(1, 2, 3).subscribe(result::add);

      assertEquals(Arrays.asList(1, 2, 3), result);
    }
  }

  @Nested
  @DisplayName("Операторы")
  class OperatorsTests {
    @Test
    @DisplayName("map() должен преобразовывать элементы по переданному правилу")
    void testMap() {
      List<String> result = new ArrayList<>();

      Observable.just(1, 2, 3).map(x -> "Число: " + x).subscribe(result::add);

      assertEquals(Arrays.asList("Число: 1", "Число: 2", "Число: 3"), result);
    }

    @Test
    @DisplayName("filter() должен фильтровать элементы по переданному правилу")
    void testFilter() {
      List<Integer> result = new ArrayList<>();

      Observable.just(1, 2, 3, 4, 5, 6).filter(x -> x % 2 == 0).subscribe(result::add);

      assertEquals(Arrays.asList(2, 4, 6), result);
    }

    @Test
    @DisplayName("map() и filter() могут быть скомбинированы и должны работать без ошибок")
    void testMapAndFilterCombined() {
      List<String> result = new ArrayList<>();

      Observable.just(1, 2, 3, 4, 5)
          .filter(x -> x % 2 == 0)
          .map(x -> x * 10)
          .map(String::valueOf)
          .subscribe(result::add);

      assertEquals(Arrays.asList("20", "40"), result);
    }

    @Test
    @DisplayName("flatMap() должен преобразовывать элементы в потоки и объединять результаты")
    void testFlatMap() {
      List<Integer> result = new ArrayList<>();

      Observable.just("1,2,3", "4,5,6")
          .flatMap(
              s ->
                  Observable.fromIterable(
                      Arrays.stream(s.split(",")).map(Integer::parseInt).toList()))
          .subscribe(result::add);

      assertEquals(Arrays.asList(1, 2, 3, 4, 5, 6), result);
    }

    @Test
    @DisplayName("flatMap() должен корректно завершаться после всех внутренних потоков")
    void testFlatMapCompletion() {
      AtomicBoolean completed = new AtomicBoolean(false);

      Observable.just(1, 2, 3)
          .flatMap(x -> Observable.just(x, x * 10))
          .subscribe(item -> {}, Throwable::printStackTrace, () -> completed.set(true));

      assertTrue(completed.get());
    }
  }

  @Nested
  @DisplayName("Управление потоками")
  class SchedulerTests {
    @Test
    @DisplayName("subscribeOn() должен выполнять подписку в указанном потоке")
    void testSubscribeOn() throws InterruptedException {
      AtomicReference<String> threadName = new AtomicReference<>();
      CountDownLatch latch = new CountDownLatch(1);

      Observable.create(
              observer -> {
                threadName.set(Thread.currentThread().getName());
                observer.onComplete();
                latch.countDown();
              })
          .subscribeOn(Scheduler.io())
          .subscribe(item -> {});

      assertTrue(latch.await(1, TimeUnit.SECONDS));
      assertTrue(threadName.get().contains("pool-"));
    }

    @Test
    @DisplayName("observeOn() должен обрабатывать элементы в указанном потоке")
    void testObserveOn() throws InterruptedException {
      List<String> threadNames = new ArrayList<>();
      CountDownLatch latch = new CountDownLatch(3);

      Observable.just(1, 2, 3)
          .observeOn(Scheduler.single())
          .map(
              x -> {
                synchronized (threadNames) {
                  threadNames.add(Thread.currentThread().getName());
                }
                latch.countDown();
                return x;
              })
          .subscribe(item -> {});

      assertTrue(latch.await(TIMEOUT_SECONDS, TimeUnit.SECONDS));
      assertEquals(3, threadNames.size());
    }

    @Test
    @DisplayName("subscribeOn() и observeOn() вместе должны работать корректно")
    void testSubscribeOnAndObserveOn() throws InterruptedException {
      AtomicReference<String> subscribeOnThread = new AtomicReference<>();
      AtomicReference<String> observeOnThread = new AtomicReference<>();
      CountDownLatch latch = new CountDownLatch(1);

      Observable.create(
              observer -> {
                subscribeOnThread.set(Thread.currentThread().getName());
                observer.onNext(1);
                observer.onComplete();
              })
          .subscribeOn(Scheduler.io())
          .observeOn(Scheduler.single())
          .map(
              x -> {
                observeOnThread.set(Thread.currentThread().getName());
                return x;
              })
          .subscribe(item -> {}, Throwable::printStackTrace, latch::countDown);

      assertTrue(latch.await(TIMEOUT_SECONDS, TimeUnit.SECONDS));
      assertNotEquals(subscribeOnThread.get(), observeOnThread.get());
    }
  }

  @Nested
  @DisplayName("Отмена подписки")
  class DisposableTests {
    @Test
    @DisplayName("dispose() должен отменять подписку")
    void testDispose() throws InterruptedException {
      List<Integer> result = new ArrayList<>();

      Disposable disposable =
          Observable.<Integer>create(
                  observer -> {
                    for (int i = 0; i < 10; i++) {
                      observer.onNext(i);
                      try {
                        Thread.sleep(SHORT_SLEEP_MS);
                      } catch (InterruptedException e) {
                        break;
                      }
                    }
                    observer.onComplete();
                  })
              .subscribe(result::add);

      Thread.sleep(MEDIUM_SLEEP_MS);
      disposable.dispose();
      Thread.sleep(MEDIUM_SLEEP_MS);

      assertTrue(result.size() >= 2);
      assertEquals(0, result.get(0));
      assertEquals(1, result.get(1));
    }

    @Test
    @DisplayName("isDisposed() должен возвращать правильное состояние")
    void testIsDisposed() {
      Disposable disposable =
          Observable.<Integer>create(
                  observer -> {
                    observer.onNext(1);
                  })
              .subscribe(item -> {});

      assertFalse(disposable.isDisposed());
      disposable.dispose();
      assertTrue(disposable.isDisposed());
    }
  }

  @Nested
  @DisplayName("Обработка ошибок")
  class ErrorHandlingTests {
    @Test
    @DisplayName("Ошибка должна корректно передаваться в onError")
    void testErrorPropagation() {
      AtomicReference<Throwable> errorRef = new AtomicReference<>();
      RuntimeException testError = new RuntimeException(TEST_ERROR);

      Observable.<Integer>create(
              observer -> {
                observer.onError(testError);
              })
          .subscribe(item -> {}, errorRef::set);

      assertSame(testError, errorRef.get());
    }

    @Test
    @DisplayName("map() должен корректно обрабатывать исключения")
    void testMapWithException() {
      List<Integer> result = new ArrayList<>();
      AtomicReference<Throwable> errorRef = new AtomicReference<>();

      Observable.just(1, 2, 3)
          .map(
              x -> {
                if (x == 2) {
                  throw new RuntimeException(TEST_ERROR);
                }

                return x * 10;
              })
          .subscribe(result::add, errorRef::set);

      assertEquals(Arrays.asList(10), result);
      assertNotNull(errorRef.get());
      assertEquals(TEST_ERROR, errorRef.get().getMessage());
    }

    @Test
    @DisplayName("filter() должен корректно обрабатывать исключения")
    void testFilterWithException() {
      List<Integer> result = new ArrayList<>();
      AtomicReference<Throwable> errorRef = new AtomicReference<>();

      Observable.just(1, 2, 3)
          .filter(
              x -> {
                if (x == 2) {
                  throw new RuntimeException(TEST_ERROR);
                }
                return x > 0;
              })
          .subscribe(result::add, errorRef::set);

      assertEquals(Arrays.asList(1), result);
      assertNotNull(errorRef.get());
      assertEquals(TEST_ERROR, errorRef.get().getMessage());
    }

    @Test
    @DisplayName("flatMap() должен обрабатывать ошибки во внутренних потоках")
    void testFlatMapWithError() {
      List<Integer> result = new ArrayList<>();
      AtomicReference<Throwable> errorRef = new AtomicReference<>();

      Observable.just(1, 2, 3)
          .flatMap(
              x ->
                  Observable.<Integer>create(
                      observer -> {
                        if (x == 2) {
                          observer.onError(new RuntimeException(TEST_ERROR));
                        } else {
                          observer.onNext(x);
                          observer.onComplete();
                        }
                      }))
          .subscribe(result::add, errorRef::set);

      assertEquals(Arrays.asList(1), result);
      assertNotNull(errorRef.get());
      assertEquals(TEST_ERROR, errorRef.get().getMessage());
    }
  }
}
