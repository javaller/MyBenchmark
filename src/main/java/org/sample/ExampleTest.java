package org.sample;

import org.apache.ignite.IgniteCheckedException;
import org.apache.ignite.binary.BinaryIdMapper;
import org.apache.ignite.binary.BinaryNameMapper;
import org.apache.ignite.binary.BinarySerializer;
import org.apache.ignite.binary.BinaryTypeConfiguration;
import org.apache.ignite.configuration.BinaryConfiguration;
import org.apache.ignite.configuration.IgniteConfiguration;
import org.apache.ignite.internal.binary.BinaryCachingMetadataHandler;
import org.apache.ignite.internal.binary.BinaryContext;
import org.apache.ignite.internal.binary.BinaryMarshaller;
import org.apache.ignite.internal.binary.GridBinaryMarshaller;
import org.apache.ignite.internal.util.IgniteUtils;
import org.apache.ignite.internal.util.typedef.internal.U;
import org.apache.ignite.logger.NullLogger;
import org.openjdk.jmh.annotations.*;

import java.util.Collection;
import java.util.concurrent.TimeUnit;

/**
 * Created by admin on 21.02.2017.
 */
@Warmup(iterations = 5, time = 1, timeUnit = TimeUnit.SECONDS)
@Measurement(iterations = 10, time = 1, timeUnit = TimeUnit.SECONDS)
@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.NANOSECONDS)
@Fork(5)
@State(Scope.Benchmark)
public class ExampleTest {

    private BinaryWriterExImplNew writer;

    protected static boolean compactFooter() {
        return true;
    }

    protected static BinaryMarshaller binaryMarshaller() throws IgniteCheckedException {
        return binaryMarshaller(null, null, null, null, null);
    }

    @Param({"Test", "TestTest", "TestTestTestTestTestTest"})
    private String message;

    private BinaryMarshaller marsh;


    /**
     * @return Binary marshaller.
     */
    protected static BinaryMarshaller binaryMarshaller(
            BinaryNameMapper nameMapper,
            BinaryIdMapper mapper,
            BinarySerializer serializer,
            Collection<BinaryTypeConfiguration> cfgs,
            Collection<String> excludedClasses
    ) throws IgniteCheckedException {
        IgniteConfiguration iCfg = new IgniteConfiguration();

        BinaryConfiguration bCfg = new BinaryConfiguration();

        bCfg.setNameMapper(nameMapper);
        bCfg.setIdMapper(mapper);
        bCfg.setSerializer(serializer);
        bCfg.setCompactFooter(compactFooter());

        bCfg.setTypeConfigurations(cfgs);

        iCfg.setBinaryConfiguration(bCfg);

        BinaryContext ctx = new BinaryContext(BinaryCachingMetadataHandler.create(), iCfg, new NullLogger());

        BinaryMarshaller marsh = new BinaryMarshaller();

        marsh.setContext(new MarshallerContextTestImpl(null, excludedClasses));

        IgniteUtils.invoke(BinaryMarshaller.class, marsh, "setBinaryContext", ctx, iCfg);

        return marsh;
    }

    /**
     * @param marsh Marshaller.
     * @return Binary context.
     */
    protected static BinaryContext binaryContext(BinaryMarshaller marsh) {
        GridBinaryMarshaller impl = U.field(marsh, "impl");
        return impl.context();
    }

    @Setup (Level.Iteration)
    public void prepare () throws IgniteCheckedException {
        marsh = binaryMarshaller();
        writer = new BinaryWriterExImplNew(binaryContext(marsh));
    }

    @Benchmark
    public void binaryHeapOutputStreamDirect() throws IgniteCheckedException {
            writer.doWriteStringDirect(message);
    }

    @Benchmark
    public void binaryHeapOutputStreamInDirect() throws IgniteCheckedException {
            writer.doWriteString(message);
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