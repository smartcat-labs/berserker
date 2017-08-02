package io.smartcat.berserker.worker;

import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.codahale.metrics.Histogram;
import com.codahale.metrics.Meter;
import com.codahale.metrics.MetricRegistry;

import io.smartcat.berserker.api.AlreadyClosedException;
import io.smartcat.berserker.api.Worker;
import io.smartcat.berserker.util.LinkedEvictingBlockingQueue;

/**
 * Asynchronous worker which uses queue and thread pool to schedule work for delegate worker. When queue is full and new
 * packet is received, an old packet will be dropped. <code>dropFromHead</code> parameter determines whether packet from
 * head or from tail will e dropped.
 *
 * @param <T> Type of data this worker accepts.
 */
public class AsyncWorker<T> implements Worker<T>, AutoCloseable {

    private static final Logger LOGGER = LoggerFactory.getLogger(AsyncWorker.class);
    private static final String DEFAULT_METRICS_PREFIX = "io.smartcat.berserker";
    private static final String DROPPED = "dropped";
    private static final String WAIT_TIME = "waitTime";
    private static final String SERVICE_TIME = "serviceTime";
    private static final String RESPONSE_TIME = "responseTime";
    private static final String GENERATED_THROUGHPUT = "generatedThroughput";
    private static final String PROCESSED_THROUGHPUT = "processedThroughput";
    private static final String QUEUE_SIZE = "queueSize";

    private final LinkedEvictingBlockingQueue<DefaultWorkerMeta> queue;
    private final ThreadPoolExecutor threadPoolExecutor;
    private final MetricRegistry metricRegistry;
    private final Meter droppedMeter;
    private final Histogram waitTime;
    private final Histogram serviceTime;
    private final Histogram responseTIme;
    private final Meter generatedThroughput;
    private final Meter processedThroughput;

    private boolean closed = false;

    /**
     * Constructs asynchronous worker with specified <code>delegate</code> worker, <code>queueCapacity</code>.
     * <code>dropFromHead</code> is set to true, <code>metricsPrefix</code> is set to
     * <code>io.smartcat.berserker</code>, <code>threadCount</code> is set to
     * <code>Runtime.getRuntime().availableProcessors()</code>, <code>threadFactory</code> is set to
     * {@link DefaultThreadFactory}.
     *
     * @param delegate Worker which is run in thread pool and to which work is delegated.
     * @param queueCapacity Capacity of the queue used as a packet buffer, must be positive number.
     */
    public AsyncWorker(Worker<T> delegate, int queueCapacity) {
        this(delegate, queueCapacity, true);
    }

    /**
     * Constructs asynchronous worker with specified <code>delegate</code> worker, <code>queueCapacity</code> and
     * <code>dropFromHead</code>. <code>metricsPrefix</code> is set to <code>io.smartcat.berserker</code>,
     * <code>threadCount</code> is set to <code>Runtime.getRuntime().availableProcessors()</code>,
     * <code>threadFactory</code> is set to {@link DefaultThreadFactory}.
     *
     * @param delegate Worker which is run in thread pool and to which work is delegated.
     * @param queueCapacity Capacity of the queue used as a packet buffer, must be positive number.
     * @param dropFromHead If true, packet from head of the queue will be dropped, if false, packet from tail of the
     *            queue will be dropped.
     */
    public AsyncWorker(Worker<T> delegate, int queueCapacity, boolean dropFromHead) {
        this(delegate, queueCapacity, dropFromHead, DEFAULT_METRICS_PREFIX);
    }

    /**
     * Constructs asynchronous worker with specified <code>delegate</code> worker, <code>queueCapacity</code>,
     * <code>dropFromHead</code> and <code>metricsPrefix</code>. <code>threadCount</code> is set to
     * <code>Runtime.getRuntime().availableProcessors()</code>, <code>threadFactory</code> is set to
     * {@link DefaultThreadFactory}.
     *
     * @param delegate Worker which is run in thread pool and to which work is delegated.
     * @param queueCapacity Capacity of the queue used as a packet buffer, must be positive number.
     * @param dropFromHead If true, packet from head of the queue will be dropped, if false, packet from tail of the
     *            queue will be dropped.
     * @param metricsPrefix Prefix for metrics.
     */
    public AsyncWorker(Worker<T> delegate, int queueCapacity, boolean dropFromHead, String metricsPrefix) {
        this(delegate, queueCapacity, dropFromHead, metricsPrefix, Runtime.getRuntime().availableProcessors());
    }

    /**
     * Constructs asynchronous worker with specified <code>delegate</code> worker, <code>queueCapacity</code>,
     * <code>dropFromHead</code>, <code>metricsPrefix</code> and <code>threadCount</code>. <code>threadFactory</code> is
     * set to {@link DefaultThreadFactory}.
     *
     * @param delegate Worker which is run in thread pool and to which work is delegated.
     * @param queueCapacity Capacity of the queue used as a packet buffer, must be positive number.
     * @param dropFromHead If true, packet from head of the queue will be dropped, if false, packet from tail of the
     *            queue will be dropped.
     * @param metricsPrefix Prefix for metrics.
     * @param threadCount Number of thread to be used by thread pool, must be positive number.
     */
    public AsyncWorker(Worker<T> delegate, int queueCapacity, boolean dropFromHead, String metricsPrefix,
            int threadCount) {
        this(delegate, queueCapacity, dropFromHead, metricsPrefix, threadCount, new DefaultThreadFactory());
    }

    /**
     * Constructs asynchronous worker with specified <code>delegate</code> worker, <code>queueCapacity</code>,
     * <code>dropFromHead</code>, <code>metricsPrefix</code>, <code>threadCount</code> and <code>threadFactory</code>.
     *
     * @param delegate Worker which is run in thread pool and to which work is delegated.
     * @param queueCapacity Capacity of the queue used as a packet buffer, must be positive number.
     * @param dropFromHead If true, packet from head of the queue will be dropped, if false, packet from tail of the
     *            queue will be dropped.
     * @param metricsPrefix Prefix for metrics.
     * @param threadCount Number of thread to be used by thread pool, must be positive number.
     * @param threadFactory ThreadFactory to be used in creating threads for thread pool.
     */
    public AsyncWorker(Worker<T> delegate, int queueCapacity, boolean dropFromHead, String metricsPrefix,
            int threadCount, ThreadFactory threadFactory) {
        if (delegate == null) {
            throw new IllegalArgumentException("Delegate cannot be null.");
        }
        if (queueCapacity <= 0) {
            throw new IllegalArgumentException("Queue capacity must be positive number.");
        }
        if (threadCount <= 0) {
            throw new IllegalArgumentException("Thread count must be positive number.");
        }
        if (threadFactory == null) {
            throw new IllegalArgumentException("Thread factory cannot be null.");
        }
        this.queue = new LinkedEvictingBlockingQueue<>(dropFromHead, queueCapacity);
        this.threadPoolExecutor = createAndInitThreadPoolExecutor(delegate, threadCount, threadFactory);
        this.metricRegistry = new MetricRegistry();
        this.droppedMeter = metricRegistry.meter(name(metricsPrefix, DROPPED));
        this.waitTime = metricRegistry.histogram(name(metricsPrefix, WAIT_TIME));
        this.serviceTime = metricRegistry.histogram(name(metricsPrefix, SERVICE_TIME));
        this.responseTIme = metricRegistry.histogram(name(metricsPrefix, RESPONSE_TIME));
        this.generatedThroughput = metricRegistry.meter(name(metricsPrefix, GENERATED_THROUGHPUT));
        this.processedThroughput = metricRegistry.meter(name(metricsPrefix, PROCESSED_THROUGHPUT));
        metricRegistry.gauge(name(metricsPrefix, QUEUE_SIZE), () -> () -> queue.size());
    }

    @Override
    public void accept(T t) {
        if (closed) {
            throw new AlreadyClosedException("Worker is already closed.");
        }
        DefaultWorkerMeta meta = new DefaultWorkerMeta(t);
        DefaultWorkerMeta dropped = queue.put(meta);
        if (dropped != null) {
            droppedMeter.mark();
        }
        generatedThroughput.mark();
    }

    @Override
    public void close() throws Exception {
        if (!closed) {
            threadPoolExecutor.shutdownNow();
            closed = true;
        }
    }

    /**
     * Returns metric registry this async worker is using.
     *
     * @return Metric registry this async worker is using.
     */
    public MetricRegistry getMetricRegistry() {
        return metricRegistry;
    }

    private ThreadPoolExecutor createAndInitThreadPoolExecutor(Worker<T> delegate, int threadCount,
            ThreadFactory threadFactory) {
        ThreadPoolExecutor result = new ThreadPoolExecutor(threadCount, threadCount, 10, TimeUnit.MILLISECONDS,
                new LinkedBlockingQueue<>(), threadFactory);
        while (result.getPoolSize() < threadCount) {
            result.submit(() -> {
                while (true) {
                    try {
                        DefaultWorkerMeta meta = queue.take();
                        meta.markAsAccepted();
                        delegate.accept(meta.getPayload());
                        meta.markAsDone();
                        waitTime.update(meta.getWaitNanoTime());
                        serviceTime.update(meta.getServiceNanoTime());
                        responseTIme.update(meta.getResposeNanoTime());
                        processedThroughput.mark();
                    } catch (Exception e) {
                        String workerName = delegate.getClass().getName();
                        LOGGER.error("Error while accepting payload at worker: " + workerName + ". Error: ", e);
                    }
                }
            });
        }
        return result;
    }

    private String name(String metricsPrefix, String name) {
        String prefix = metricsPrefix == null || metricsPrefix.isEmpty() ? DEFAULT_METRICS_PREFIX : metricsPrefix;
        return MetricRegistry.name(prefix, name);
    }

    /**
     * Thread factory that creates normal priority, daemon threads with 'async-worker-thread-&lt;number&gt;' names.
     */
    public static class DefaultThreadFactory implements ThreadFactory {

        private final AtomicInteger threadNumber = new AtomicInteger(1);

        @Override
        public Thread newThread(Runnable r) {
            Thread thread = new Thread(r);
            thread.setName("async-worker-thread-" + threadNumber.getAndIncrement());
            thread.setDaemon(true);
            thread.setPriority(Thread.NORM_PRIORITY);
            return thread;
        }
    }

    /**
     * Meta data on worker packet processing.
     *
     * @param <T> Type of payload this meta data contains.
     */
    public interface WorkerMeta<T> {

        /**
         * Returns payload sent to worker.
         *
         * @return payload sent to worker.
         */
        T getPayload();

        /**
         * Returns time in nanoseconds when packet was submitted to the worker thread.
         *
         * @return time in nanoseconds when packet was submitted to the worker thread.
         */
        long getWaitNanoTime();

        /**
         * Returns time in nanoseconds when packet was accepted by the worker.
         *
         * @return time in nanoseconds when packet was accepted by the worker.
         */
        long getServiceNanoTime();

        /**
         * Returns time in nanoseconds when processing was done on packet.
         *
         * @return time in nanoseconds when processing was done on packet.
         */
        long getResposeNanoTime();
    }

    /**
     * Default implementation of {@link WorkerMeta}.
     */
    private class DefaultWorkerMeta implements WorkerMeta<T> {

        private final T payload;
        private final long timeSubmittedInNanos;
        private long timeAcceptedInNanos;
        private long timeDoneInNanos;
        private boolean dropped = false;

        public DefaultWorkerMeta(T payload) {
            this.payload = payload;
            this.timeSubmittedInNanos = now();
        }

        public void markAsAccepted() {
            timeAcceptedInNanos = now();
        }

        public void markAsDone() {
            timeDoneInNanos = now();
        }

        @Override
        public T getPayload() {
            return payload;
        }

        @Override
        public long getWaitNanoTime() {
            return timeAcceptedInNanos - timeSubmittedInNanos;
        }

        @Override
        public long getServiceNanoTime() {
            return timeDoneInNanos - timeAcceptedInNanos;
        }

        @Override
        public long getResposeNanoTime() {
            return timeDoneInNanos - timeSubmittedInNanos;
        }

        @Override
        public String toString() {
            return "DefaultWorkerMeta [payload=" + payload + ", timeSubmittedInNanos=" + timeSubmittedInNanos
                    + ", timeAcceptedInNanos=" + timeAcceptedInNanos + ", timeDoneInNanos=" + timeDoneInNanos
                    + ", dropped=" + dropped + "]";
        }

        private long now() {
            return System.nanoTime();
        }
    }
}
