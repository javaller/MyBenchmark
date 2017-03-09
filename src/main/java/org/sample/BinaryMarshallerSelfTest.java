package org.sample;

import org.apache.ignite.IgniteCheckedException;
import org.apache.ignite.binary.BinaryIdMapper;
import org.apache.ignite.binary.BinaryNameMapper;
import org.apache.ignite.binary.BinarySerializer;
import org.apache.ignite.binary.BinaryTypeConfiguration;
import org.apache.ignite.configuration.BinaryConfiguration;
import org.apache.ignite.configuration.IgniteConfiguration;
import org.apache.ignite.internal.binary.*;
import org.apache.ignite.internal.util.IgniteUtils;
import org.apache.ignite.internal.util.typedef.internal.U;
import org.apache.ignite.logger.NullLogger;
import org.junit.Test;

import java.util.Collection;

import static org.apache.ignite.internal.binary.streams.BinaryMemoryAllocator.INSTANCE;
import static org.junit.Assert.assertEquals;

/**
 * Created by admin on 21.02.2017.
 */
public class BinaryMarshallerSelfTest  {

    protected static boolean compactFooter() {
        return true;
    }


    protected static BinaryMarshaller binaryMarshaller() throws IgniteCheckedException {
        return binaryMarshaller(null, null, null, null, null);
    }
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

    @Test
    public  void testDirect() throws Exception {

        // Checking the writer directly.
        assertEquals(false, INSTANCE.isAcquired());

        BinaryMarshaller marsh = binaryMarshaller();

        try (BinaryWriterExImplNew writer = new BinaryWriterExImplNew(binaryContext(marsh))) {
            writer.doWriteStringDirect("TestTestTest");
//            writer.array();
            assertEquals(true, INSTANCE.isAcquired());
        }

        // Checking the binary marshaller.
        assertEquals(false, INSTANCE.isAcquired());
    }

    @Test
    public  void testInDirect() throws Exception {

        // Checking the writer directly.
        assertEquals(false, INSTANCE.isAcquired());

        BinaryMarshaller marsh = binaryMarshaller();

        System.out.println("ТестТест");
        try (BinaryWriterExImplNew writer = new BinaryWriterExImplNew(binaryContext(marsh))) {
            writer.doWriteString("TestTestTest");
//            writer.array();
            assertEquals(true, INSTANCE.isAcquired());
        }

        // Checking the binary marshaller.
        assertEquals(false, INSTANCE.isAcquired());
    }
}