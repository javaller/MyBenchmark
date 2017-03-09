package org.sample;

import org.apache.ignite.internal.binary.*;
import org.apache.ignite.internal.binary.streams.BinaryHeapOutputStream;
import org.apache.ignite.internal.binary.streams.BinaryOutputStream;
import org.jetbrains.annotations.Nullable;

import java.io.ByteArrayOutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

import static java.nio.charset.StandardCharsets.UTF_8;

/**
 * Created by admin on 22.02.2017.
 */

public class BinaryWriterExImplNew extends BinaryWriterExImpl {

    public BinaryWriterExImplNew(BinaryContext ctx) {
        super(ctx);
    }

    public BinaryWriterExImplNew(BinaryContext ctx, BinaryThreadLocalContext tlsCtx) {
        super(ctx, tlsCtx);
    }

    public BinaryWriterExImplNew(BinaryContext ctx, BinaryOutputStream out, BinaryWriterSchemaHolder schema, BinaryWriterHandles handles) {
        super(ctx, out, schema, handles);
    }

    /**
     * @param val String value.
     */
    public void doWriteStringDirect(@Nullable String val) {

        if (val == null)
            out.writeByte(GridBinaryMarshaller.NULL);
        else {
            if (BinaryUtils.USE_STR_SERIALIZATION_VER_2)
                BinaryUtilsNew.strToUtf8BytesDirect(out, val);
            else
                BinaryUtilsNew.strToUtf8BytesDirect(out, val);
        }
    }
}