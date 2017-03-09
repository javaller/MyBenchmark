package org.sample;

import org.apache.ignite.internal.binary.BinaryUtils;
import org.apache.ignite.internal.binary.GridBinaryMarshaller;
import org.apache.ignite.internal.binary.streams.BinaryHeapOutputStream;
import org.apache.ignite.internal.binary.streams.BinaryOffheapOutputStream;
import org.apache.ignite.internal.binary.streams.BinaryOutputStream;

import java.io.*;

/**
 * Created by admin on 28.02.2017.
 */
public class BinaryUtilsNew extends BinaryUtils {

    public static int getStrLen(String val) {
        int strLen = val.length();
        int utfLen = 0;
        int c;

        // Determine length of resulting byte array.
        for (int cnt = 0; cnt < strLen; cnt++) {
            c = val.charAt(cnt);

            if (c >= 0x0001 && c <= 0x007F)
                utfLen++;
            else if (c > 0x07FF)
                utfLen += 3;
            else
                utfLen += 2;
        }

        return utfLen;
    }

    public static void strToUtf8BytesDirect(BinaryOutputStream outBinaryHeap, String val) {

        int strLen = val.length();
        int c, cnt;

        int position = 0;

        outBinaryHeap.unsafeEnsure(1 + 4);
        outBinaryHeap.unsafeWriteByte(GridBinaryMarshaller.STRING);
        outBinaryHeap.unsafeWriteInt(getStrLen(val));

        for (cnt = 0; cnt < strLen; cnt++) {
            c = val.charAt(cnt);

            if (c >= 0x0001 && c <= 0x007F)
                outBinaryHeap.writeByte((byte) c);
            else if (c > 0x07FF) {
                outBinaryHeap.writeByte((byte)(0xE0 | (c >> 12) & 0x0F));
                outBinaryHeap.writeByte((byte)(0x80 | (c >> 6) & 0x3F));
                outBinaryHeap.writeByte((byte)(0x80 | (c & 0x3F)));
            }
            else {
                outBinaryHeap.writeByte((byte)(0xC0 | ((c >> 6) & 0x1F)));
                outBinaryHeap.writeByte((byte)(0x80 | (c  & 0x3F)));
            }
        }
    }

    public static BinaryOutputStream strToBinaryOutputStream(String val) {

        int strLen = val.length();
        int utfLen = 0;
        int c, cnt;

        // Determine length of resulting byte array.
        for (cnt = 0; cnt < strLen; cnt++) {
            c = val.charAt(cnt);

            if (c >= 0x0001 && c <= 0x007F)
                utfLen++;
            else if (c > 0x07FF)
                utfLen += 3;
            else
                utfLen += 2;
        }

        BinaryOutputStream outBinaryHeap = new BinaryHeapOutputStream(utfLen);

        int position = 0;

        for (cnt = 0; cnt < strLen; cnt++) {
            c = val.charAt(cnt);

            if (c >= 0x0001 && c <= 0x007F)
                outBinaryHeap.writeByte((byte) c);
            else if (c > 0x07FF) {
                outBinaryHeap.writeByte((byte)(0xE0 | (c >> 12) & 0x0F));
                outBinaryHeap.writeByte((byte)(0x80 | (c >> 6) & 0x3F));
                outBinaryHeap.writeByte((byte)(0x80 | (c & 0x3F)));
            }
            else {
                outBinaryHeap.writeByte((byte)(0xC0 | ((c >> 6) & 0x1F)));
                outBinaryHeap.writeByte((byte)(0x80 | (c  & 0x3F)));
            }
        }

        return outBinaryHeap;
    }
}