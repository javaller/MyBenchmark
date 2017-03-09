package org.sample;



import org.apache.ignite.Ignite;
import org.apache.ignite.IgniteLogger;
import org.apache.ignite.cluster.ClusterGroup;
import org.apache.ignite.internal.util.typedef.P2;
import org.openjdk.jmh.annotations.*;

import java.io.*;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Created by SBT-Opolskiy-VA on 07.03.2017.
 */


@Warmup(iterations = 5, time = 1, timeUnit = TimeUnit.SECONDS)
@Measurement(iterations = 10, time = 1, timeUnit = TimeUnit.SECONDS)
@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.NANOSECONDS)
@Fork(5)
@State(Scope.Benchmark)
public class LatchPerformanceTest /*extends GridCommonAbstractTest*/ implements Serializable {

    /** */
    private static final String MSG_1 = "MSG-1";

    /** */
    private static final String MSG_2 = "MSG-2";

    /** */
    private static final String MSG_3 = "MSG-3";

    /** */
    protected static CountDownLatch rcvLatch;

    /** */
    protected Ignite ignite2;

    /** */
    private static final String S_TOPIC_1 = "TOPIC-1";

    /** */
    private static final String S_TOPIC_2 = "TOPIC-2";

    /** */
    protected transient IgniteLogger log;

    /** */
    protected Ignite ignite1;

    private static class TestMessage implements Externalizable {
        /** */
        private Object body;

        /** */
        private long delayMs;

        /**
         * No-arg constructor for {@link Externalizable}.
         */
        public TestMessage() {
            // No-op.
        }

        /**
         * @param body Message body.
         */
        TestMessage(Object body) {
            this.body = body;
        }

        /**
         * @param body Message body.
         * @param delayMs Message send delay in milliseconds.
         */
        TestMessage(Object body, long delayMs) {
            this.body = body;
            this.delayMs = delayMs;
        }

        /** {@inheritDoc} */
        @Override public String toString() {
            return "TestMessage [body=" + body + "]";
        }

        /** {@inheritDoc} */
        @Override public int hashCode() {
            return body.hashCode();
        }

        /** {@inheritDoc} */
        @Override public boolean equals(Object obj) {
            return obj instanceof TestMessage && body.equals(((TestMessage)obj).body);
        }

        /** {@inheritDoc} */
        @Override public void writeExternal(ObjectOutput out) throws IOException {
            if (delayMs > 0) {
                try {
                    Thread.sleep(delayMs);
                }
                catch (InterruptedException ignored) {
                    Thread.currentThread().interrupt();
                }
            }

            out.writeObject(body);
        }

        /** {@inheritDoc} */
        @Override public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
            body = in.readObject();
        }
    }

    @Benchmark
    public void testRemoteListenOrderedMessages() throws Exception {
        List<TestMessage> msgs = Arrays.asList(
                new TestMessage(MSG_1),
                new TestMessage(MSG_2, 3000),
                new TestMessage(MSG_3));

        final Collection<Object> rcvMsgs = new ConcurrentLinkedDeque<>();

        final AtomicBoolean error = new AtomicBoolean(false); //to make it modifiable

        rcvLatch = new CountDownLatch(3);

        ignite2.message().remoteListen(S_TOPIC_1, new P2<UUID, Object>() {
            @Override public boolean apply(UUID nodeId, Object msg) {
                try {
                    log.info("Received new message [msg=" + msg + ", senderNodeId=" + nodeId + ']');

                    if (!nodeId.equals(ignite1.cluster().localNode().id())) {
                        log.error("Unexpected sender node: " + nodeId);

                        error.set(true);

                        return false;
                    }

                    rcvMsgs.add(msg);

                    return true;
                }
                finally {
                    rcvLatch.countDown();
                }
            }
        });

        ClusterGroup prj2 = ignite1.cluster().forRemotes(); // Includes node from grid2.

/*
        for (TestMessage msg : msgs)
            message(prj2).sendOrdered(S_TOPIC_1, msg, 15000);
*/

    }

//    public static void main(String[] args) throws RunnerException {
//        Options opt = new OptionsBuilder()
//                .include(ExampleTest.class.getSimpleName())
//                .forks(1)
//                .build();
//
//        new Runner(opt).run();
//    }
}