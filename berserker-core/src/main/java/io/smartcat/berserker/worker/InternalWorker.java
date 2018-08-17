package io.smartcat.berserker.worker;

import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.codahale.metrics.Histogram;
import com.codahale.metrics.Meter;
import com.codahale.metrics.MetricRegistry;

import io.smartcat.berserker.api.AlreadyClosedException;
import io.smartcat.berserker.api.Worker;
import io.smartcat.berserker.util.LinkedEvictingBlockingQueue;

/**
 * Default implementation of {@link InternalWorker} which uses queue and thread pool to schedule work for delegate
 * worker. When queue is full and new message is received, an old message will be dropped. <code>dropFromHead</code>
 * parameter determines whether message from head or from tail will e dropped.
 *
 * @param <T> Type of data this worker accepts.
 */
public class InternalWorker<T> implements Consumer<T>, AutoCloseable {

    private static final Logger LOGGER = LoggerFactory.getLogger(InternalWorker.class);
    private static final String DEFAULT_METRICS_PREFIX = "io.smartcat.berserker";
    private static final String DROPPED = "dropped";
    private static final String WAIT_TIME = "waitTime";
    private static final String SUCCESS_SERVICE_TIME = "successServiceTime";
    private static final String FAILURE_SERVICE_TIME = "failureServiceTime";
    private static final String TOTAL_SERVICE_TIME = "totalServiceTime";
    private static final String SUCCESS_RESPONSE_TIME = "successResponseTime";
    private static final String FAILURE_RESPONSE_TIME = "failureResponseTime";
    private static final String TOTAL_RESPONSE_TIME = "totalResponseTime";
    private static final String GENERATED_THROUGHPUT = "generatedThroughput";
    private static final String SUCCESS_PROCESSED_THROUGHPUT = "successProcessedThroughput";
    private static final String FAILURE_PROCESSED_THROUGHPUT = "failureProcessedThroughput";
    private static final String TOTAL_PROCESSED_THROUGHPUT = "totalProcessedThroughput";
    private static final String QUEUE_SIZE = "queueSize";

    private final LinkedEvictingBlockingQueue<WorkerMeta> queue;
    private final ThreadPoolExecutor threadPoolExecutor;
    private final MetricRegistry metricRegistry;
    private final Meter droppedMeter;
    private final Histogram waitTime;
    private final Histogram successServiceTime;
    private final Histogram failureServiceTime;
    private final Histogram totalServiceTime;
    private final Histogram successResponseTime;
    private final Histogram failureResponseTime;
    private final Histogram totalResponseTime;
    private final Meter generatedThroughput;
    private final Meter totalProcessedThroughput;
    private final Meter successProcessedThroughput;
    private final Meter failureProcessedThroughput;

    private boolean closed = false;

    /**
     * Constructs asynchronous worker with specified <code>delegate</code> worker, <code>queueCapacity</code>.
     * <code>dropFromHead</code> is set to true, <code>metricsPrefix</code> is set to
     * <code>io.smartcat.berserker</code>, <code>threadCount</code> is set to
     * <code>Runtime.getRuntime().availableProcessors()</code>, <code>threadFactory</code> is set to
     * {@link DefaultThreadFactory}.
     *
     * @param delegate Worker which is run in thread pool and to which work is delegated.
     * @param queueCapacity Capacity of the queue used as a message buffer, must be positive number.
     */
    public InternalWorker(Worker<T> delegate, int queueCapacity) {
        this(delegate, queueCapacity, true);
    }

    /**
     * Constructs asynchronous worker with specified <code>delegate</code> worker, <code>queueCapacity</code> and
     * <code>dropFromHead</code>. <code>metricsPrefix</code> is set to <code>io.smartcat.berserker</code>,
     * <code>threadCount</code> is set to <code>Runtime.getRuntime().availableProcessors()</code>,
     * <code>threadFactory</code> is set to {@link DefaultThreadFactory}.
     *
     * @param delegate Worker which is run in thread pool and to which work is delegated.
     * @param queueCapacity Capacity of the queue used as a message buffer, must be positive number.
     * @param dropFromHead If true, message from head of the queue will be dropped, if false, message from tail of the
     *            queue will be dropped.
     */
    public InternalWorker(Worker<T> delegate, int queueCapacity, boolean dropFromHead) {
        this(delegate, queueCapacity, dropFromHead, DEFAULT_METRICS_PREFIX);
    }

    /**
     * Constructs asynchronous worker with specified <code>delegate</code> worker, <code>queueCapacity</code>,
     * <code>dropFromHead</code> and <code>metricsPrefix</code>. <code>threadCount</code> is set to
     * <code>Runtime.getRuntime().availableProcessors()</code>, <code>threadFactory</code> is set to
     * {@link DefaultThreadFactory}.
     *
     * @param delegate Worker which is run in thread pool and to which work is delegated.
     * @param queueCapacity Capacity of the queue used as a message buffer, must be positive number.
     * @param dropFromHead If true, message from head of the queue will be dropped, if false, message from tail of the
     *            queue will be dropped.
     * @param metricsPrefix Prefix for metrics.
     */
    public InternalWorker(Worker<T> delegate, int queueCapacity, boolean dropFromHead, String metricsPrefix) {
        this(delegate, queueCapacity, dropFromHead, metricsPrefix, Runtime.getRuntime().availableProcessors());
    }

    /**
     * Constructs asynchronous worker with specified <code>delegate</code> worker, <code>queueCapacity</code>,
     * <code>dropFromHead</code>, <code>metricsPrefix</code> and <code>threadCount</code>. <code>threadFactory</code> is
     * set to {@link DefaultThreadFactory}.
     *
     * @param delegate Worker which is run in thread pool and to which work is delegated.
     * @param queueCapacity Capacity of the queue used as a message buffer, must be positive number.
     * @param dropFromHead If true, message from head of the queue will be dropped, if false, message from tail of the
     *            queue will be dropped.
     * @param metricsPrefix Prefix for metrics.
     * @param threadCount Number of thread to be used by thread pool, must be positive number.
     */
    public InternalWorker(Worker<T> delegate, int queueCapacity, boolean dropFromHead, String metricsPrefix,
                          int threadCount) {
        this(delegate, queueCapacity, dropFromHead, metricsPrefix, threadCount, new DefaultThreadFactory());
    }

    /**
     * Constructs asynchronous worker with specified <code>delegate</code> worker, <code>queueCapacity</code>,
     * <code>dropFromHead</code>, <code>metricsPrefix</code>, <code>threadCount</code> and <code>threadFactory</code>.
     *
     * @param delegate Worker which is run in thread pool and to which work is delegated.
     * @param queueCapacity Capacity of the queue used as a message buffer, must be positive number.
     * @param dropFromHead If true, message from head of the queue will be dropped, if false, message from tail of the
     *            queue will be dropped.
     * @param metricsPrefix Prefix for metrics.
     * @param threadCount Number of thread to be used by thread pool, must be positive number.
     * @param threadFactory ThreadFactory to be used in creating threads for thread pool.
     */
    public InternalWorker(Worker<T> delegate, int queueCapacity, boolean dropFromHead, String metricsPrefix,
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
        this.successServiceTime = metricRegistry.histogram(name(metricsPrefix, SUCCESS_SERVICE_TIME));
        this.failureServiceTime = metricRegistry.histogram(name(metricsPrefix, FAILURE_SERVICE_TIME));
        this.totalServiceTime = metricRegistry.histogram(name(metricsPrefix, TOTAL_SERVICE_TIME));
        this.successResponseTime = metricRegistry.histogram(name(metricsPrefix, SUCCESS_RESPONSE_TIME));
        this.failureResponseTime = metricRegistry.histogram(name(metricsPrefix, FAILURE_RESPONSE_TIME));
        this.totalResponseTime = metricRegistry.histogram(name(metricsPrefix, TOTAL_RESPONSE_TIME));
        this.generatedThroughput = metricRegistry.meter(name(metricsPrefix, GENERATED_THROUGHPUT));
        this.successProcessedThroughput = metricRegistry.meter(name(metricsPrefix, SUCCESS_PROCESSED_THROUGHPUT));
        this.failureProcessedThroughput = metricRegistry.meter(name(metricsPrefix, FAILURE_PROCESSED_THROUGHPUT));
        this.totalProcessedThroughput = metricRegistry.meter(name(metricsPrefix, TOTAL_PROCESSED_THROUGHPUT));
        metricRegistry.gauge(name(metricsPrefix, QUEUE_SIZE), () -> () -> queue.size());
    }

    /**
     * Accepts message of type {@code <T>}.
     *
     * @param message Message to be processed.
     */
    @Override
    public void accept(T message) {
        if (closed) {
            throw new AlreadyClosedException("Worker is already closed.");
        }
        WorkerMeta meta = new WorkerMeta(message);
        WorkerMeta dropped = queue.put(meta);
        if (dropped != null) {
            droppedMeter.mark();
        }
        generatedThroughput.mark();
    }

    @Override
    public void close() {
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
        String workerName = delegate.getClass().getName();
        while (result.getPoolSize() < threadCount) {
            result.submit(() -> {
                while (true) {
                    try {
                        WorkerMeta meta = queue.take();
                        meta.markAsAccepted();
                        delegate.accept(meta.getPayload(), () -> {
                            meta.markAsDone();
                            waitTime.update(meta.getWaitNanoTime());
                            successServiceTime.update(meta.getServiceNanoTime());
                            totalServiceTime.update(meta.getServiceNanoTime());
                            successResponseTime.update(meta.getResponseNanoTime());
                            totalResponseTime.update(meta.getResponseNanoTime());
                            successProcessedThroughput.mark();
                            totalProcessedThroughput.mark();
                        }, (throwable) -> {
                            meta.markAsDone();
                            waitTime.update(meta.getWaitNanoTime());
                            failureServiceTime.update(meta.getServiceNanoTime());
                            totalServiceTime.update(meta.getServiceNanoTime());
                            failureResponseTime.update(meta.getResponseNanoTime());
                            totalResponseTime.update(meta.getResponseNanoTime());
                            failureProcessedThroughput.mark();
                            totalProcessedThroughput.mark();
                            if (throwable != null) {
                                LOGGER.error("Error while accepting payload at worker: " + workerName + ". Error: ",
                                        throwable);
                            }
                        });
                    } catch (Exception e) {
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
     * Meta data on worker message processing.
     */
    private class WorkerMeta {

        private final T payload;
        private final long timeSubmittedInNanos;
        private long timeAcceptedInNanos;
        private long timeDoneInNanos;
        private boolean dropped = false;

        WorkerMeta(T payload) {
            this.payload = payload;
            this.timeSubmittedInNanos = now();
        }

        void markAsAccepted() {
            timeAcceptedInNanos = now();
        }

        void markAsDone() {
            timeDoneInNanos = now();
        }

        /**
         * Returns payload sent to worker.
         *
         * @return payload sent to worker.
         */
        T getPayload() {
            return payload;
        }

        /**
         * Returns time in nanoseconds when message was submitted to the worker thread.
         *
         * @return time in nanoseconds when message was submitted to the worker thread.
         */
        long getWaitNanoTime() {
            return timeAcceptedInNanos - timeSubmittedInNanos;
        }

        /**
         * Returns time in nanoseconds when message was accepted by the worker.
         *
         * @return time in nanoseconds when message was accepted by the worker.
         */
        long getServiceNanoTime() {
            return timeDoneInNanos - timeAcceptedInNanos;
        }

        /**
         * Returns time in nanoseconds when processing was done on message.
         *
         * @return time in nanoseconds when processing was done on message.
         */
        long getResponseNanoTime() {
            return timeDoneInNanos - timeSubmittedInNanos;
        }

        @Override
        public String toString() {
            return "WorkerMeta [payload=" + payload + ", timeSubmittedInNanos=" + timeSubmittedInNanos
                    + ", timeAcceptedInNanos=" + timeAcceptedInNanos + ", timeDoneInNanos=" + timeDoneInNanos
                    + ", dropped=" + dropped + "]";
        }

        private long now() {
            return System.nanoTime();
        }
    }
}
