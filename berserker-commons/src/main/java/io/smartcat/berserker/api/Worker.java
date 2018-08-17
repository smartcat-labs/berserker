package io.smartcat.berserker.api;

import java.util.function.Consumer;

/**
 * Worker interface providing API for task execution.
 *
 * @param <T> Type of data workerConfiguration accepts.
 */
public interface Worker<T> {

    /**
     * Accepts message of type {@code <T>} and processes it.
     *
     * @param message Message which will be processed.
     * @param commitSuccess Callback to be invoked when processing is successful.
     * @param commitFailure Callback to be invoked in case of a failure.
     */
    void accept(T message, Runnable commitSuccess, Consumer<Throwable> commitFailure);
}
