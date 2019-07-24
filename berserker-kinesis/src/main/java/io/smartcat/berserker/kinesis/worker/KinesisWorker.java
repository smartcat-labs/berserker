package io.smartcat.berserker.kinesis.worker;

import java.math.BigInteger;
import java.nio.ByteBuffer;
import java.util.Map;
import java.util.Properties;
import java.util.Random;

import com.amazonaws.auth.DefaultAWSCredentialsProviderChain;
import com.amazonaws.services.kinesis.producer.*;

import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import io.smartcat.berserker.api.Worker;

/**
 * Worker that publishes accepted message to AWS Kinesis.
 */
public class KinesisWorker implements Worker<Map<String, Object>>, AutoCloseable {

    private static final String TIMESTAMP = Long.toString(System.currentTimeMillis());
    private static final String MSG = "msg";

    private final Properties props;
    private final boolean async;
    private final String stream;

    private KinesisProducer producer;

    /**
     * Constructs Kinesis worker with specified properties.
     *
     * @param props Properties to be used by {@link KinesisProducer}. Contains common Kinesis producer
     * <a href="https://docs.aws.amazon.com/streams/latest/dev/kinesis-kpl-config.html">configuration properties</a>.
     * @param async Indicates whether messages should be sent asynchronously or synchronously.
     * @param stream Kinesis stream to which to send messages.
     */
    public KinesisWorker(Properties props, boolean async, String stream) {
        this.props = props;
        this.async = async;
        this.stream = stream;
        init();
    }

    /**
     * Accepts following arguments:
     * <ul>
     * <li><code><b>msg</b></code> - Content of the message. Mandatory.</li>
     * </ul>
     */
    @Override
    public void accept(Map<String, Object> message, Runnable commitSuccess, Runnable commitFailure) {
        String msg = (String) message.get(MSG);
        if (msg == null) {
            throw new RuntimeException("'msg' is mandatory.");
        }

        ByteBuffer data = ByteBuffer.wrap(msg.getBytes());
        UserRecord record = new UserRecord(stream, TIMESTAMP, randomExplicitHashKey(), data);

        FutureCallback<UserRecordResult> callback = new FutureCallback<UserRecordResult>() {
            @Override
            public void onFailure(Throwable t) {
                if (t instanceof UserRecordFailedException) {
                    commitFailure.run();
                }
            }

            @Override
            public void onSuccess(UserRecordResult result) {
                commitSuccess.run();
            }
        };

        ListenableFuture<UserRecordResult> responseFuture = producer.addUserRecord(record);
        Futures.addCallback(responseFuture, callback);

        if (!async) {
            try {
                responseFuture.get();
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
    }

    @Override
    public void close() {
        producer.destroy();
    }

    private void init() {
        KinesisProducerConfiguration config = KinesisProducerConfiguration.fromProperties(props);
        config.setCredentialsProvider(new DefaultAWSCredentialsProviderChain());

        producer = new KinesisProducer(config);
    }

    private String randomExplicitHashKey() {
        Random rand = new Random();
        return new BigInteger(128, rand).toString(10);
    }
}
