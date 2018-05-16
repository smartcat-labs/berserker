package io.smartcat.berserker.worker;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicInteger;

import org.junit.Assert;
import org.junit.Test;

import io.smartcat.berserker.LoadGenerator;
import io.smartcat.berserker.api.DataSource;
import io.smartcat.berserker.api.RateGenerator;
import io.smartcat.berserker.api.Worker;
import io.smartcat.berserker.rategenerator.ConstantRateGenerator;

public class InternalWorkerTest {

    /**
     * InternalWorker has 4 threads and queue of 4 elements. Each thread is blocked until load generator depletes data
     * source. Data source has 10 values. At that moment, 4 threads have taken first four values and there is four more
     * values in queue to be processed. If load generator does not block, it means that InternalWorker is non-blocking.
     */
    @Test(timeout = 3000)
    public void should_not_block_thread_when_delegate_of_asyncWorker_is_blocking() throws Exception {
        // GIVEN
        RateGenerator rg = new ConstantRateGenerator(10);
        Object lock = new Object();
        AtomicInteger delegateInvokeCount = new AtomicInteger();
        Worker<Integer> delegate = (x) -> {
            synchronized (lock) {
                delegateInvokeCount.incrementAndGet();
            }
        };
        DataSource<Integer> ds = new DataSource<Integer>() {

            private int i = 0;

            @Override
            public boolean hasNext(long time) {
                return i < 10;
            }

            @Override
            public Integer getNext(long time) {
                return i++;
            }
        };
        InternalWorker<Integer> w = new InternalWorker<>(delegate, 4, true, null, 4);
        LoadGenerator<Integer> lg = new LoadGenerator<>(ds, rg, w);

        // WHEN
        synchronized (lock) {
            lg.run();
        }
        w.close();

        // THEN
        // load generator was not blocked meaning that InternalWorker is non-blocking.
    }

    /**
     * InternalWorker has 3 threads and queue of 3 elements. Each thread is blocked until load generator depletes data
     * source. After threads have taken first 3 values and queue has taken next 3 values. Queue is full with values 4,
     * 5, 6. Since values are dropped from head and added to tail. After 3 droppings and 3 additions, queue has 7, 8,
     * and 9.
     */
    @Test(timeout = 3000)
    public void should_drop_packet_from_head_of_the_queue_when_queue_is_full_and_dropFromHead_is_true()
            throws Exception {
        // GIVEN
        RateGenerator rg = new ConstantRateGenerator(10);
        CountDownLatch countDownLatch = new CountDownLatch(6);
        CountDownLatch dsCountDownLatch = new CountDownLatch(3);
        Object lock = new Object();
        AtomicInteger delegateInvokeCount = new AtomicInteger();
        Worker<Integer> delegate = (x) -> {
            dsCountDownLatch.countDown();
            synchronized (lock) {
                delegateInvokeCount.addAndGet(x);
                countDownLatch.countDown();
            }
        };
        DataSource<Integer> ds = new DataSource<Integer>() {

            private int i = 1;

            @Override
            public boolean hasNext(long time) {
                return i < 10;
            }

            @Override
            public Integer getNext(long time) {
                if (i == 4) {
                    try {
                        dsCountDownLatch.await();
                    } catch (InterruptedException e) {
                    }
                }
                return i++;
            }
        };
        InternalWorker<Integer> w = new InternalWorker<>(delegate, 3, true, null, 3);
        LoadGenerator<Integer> lg = new LoadGenerator<>(ds, rg, w);

        // WHEN
        synchronized (lock) {
            lg.run();
        }
        countDownLatch.await();
        w.close();

        // THEN
        // Values that are processed should be: 1, 2, 3, 7, 8, 9. Total of 30.
        Assert.assertEquals(30, delegateInvokeCount.get());
    }

    /**
     * InternalWorker has 3 threads and queue of 3 elements. Each thread is blocked until load generator depletes data
     * source. After threads have taken first 3 values and queue has taken next 3 values. Queue is full with values 4,
     * 5, 6. Since values are dropped from tail and added to tail. After 3 droppings and 3 additions, queue has 4, 5,
     * and 9.
     */
    @Test(timeout = 3000)
    public void should_drop_packet_from_head_of_the_queue_when_queue_is_full_and_dropFromHead_is_false()
            throws Exception {
        // GIVEN
        RateGenerator rg = new ConstantRateGenerator(10);
        CountDownLatch countDownLatch = new CountDownLatch(6);
        CountDownLatch dsCountDownLatch = new CountDownLatch(3);
        Object lock = new Object();
        AtomicInteger delegateInvokeCount = new AtomicInteger();
        Worker<Integer> delegate = (x) -> {
            dsCountDownLatch.countDown();
            synchronized (lock) {
                delegateInvokeCount.addAndGet(x);
                countDownLatch.countDown();
            }
        };
        DataSource<Integer> ds = new DataSource<Integer>() {

            private int i = 1;

            @Override
            public boolean hasNext(long time) {
                return i < 10;
            }

            @Override
            public Integer getNext(long time) {
                if (i == 4) {
                    try {
                        dsCountDownLatch.await();
                    } catch (InterruptedException e) {
                    }
                }
                return i++;
            }
        };
        InternalWorker<Integer> w = new InternalWorker<>(delegate, 3, false, null, 3);
        LoadGenerator<Integer> lg = new LoadGenerator<>(ds, rg, w);

        // WHEN
        synchronized (lock) {
            lg.run();
        }
        countDownLatch.await();
        w.close();

        // THEN
        // Values that are processed should be: 1, 2, 3, 4, 5, 9. Total of 24.
        Assert.assertEquals(24, delegateInvokeCount.get());
    }
}
