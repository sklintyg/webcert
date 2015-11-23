package se.inera.intyg.webcert.web.service.signatur.asn1;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.LinkedList;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.io.IOUtils;

/**
 * Semi-generic byte stream parser.
 *
 * Usually used to extract data from a base64-encoded ASN.1 netId signature by
 * matching a supplied sequence of bytes with the content of the stream.
 *
 * If a match is found, the up-front known number of bytes is read and returned as "result".
 *
 * For unknown-length values, an alternate implementation will have to be developed that uses the
 * ASN.1 triplet mechanism to determine data length.
 *
 * Created by eriklupander on 2015-09-01.
 */
public class ASN1StreamParser {

    private static final int FF = 0xFF;
    private static final int BIT8 = 7;

    /**
     * Tries to extract a value from the supplied base64-encoded InputStream by trying to match
     * a sequence of bytes in the stream with the supplied marker sequence of bytes.
     *
     * @param is
     *            A BASE64-encoded byte stream, typically an ASN.1 signature from NetID
     * @param marker
     *            A known sequence of bytes denoting where the wanted value starts in the stream.
     * @param dataLength
     *            The number of bytes to read following the marker, when found.
     * @return
     *         String representation of the found value or null if the marker wasn't present in the byte stream.
     * @throws IOException
     */
    public byte[] parse(InputStream is, int[] marker, int dataLength) throws IOException {
        byte[] bytes = IOUtils.toByteArray(is);
        byte[] decoded = Base64.decodeBase64(bytes);

        LimitedQueue<Integer> buffer = new LimitedQueue<>(marker.length);

        try (ByteArrayInputStream bais = new ByteArrayInputStream(decoded)) {
            while (bais.available() > 0) {
                int b = unsignByte(bais);
                buffer.add(b);

                if ((buffer.size() == marker.length) && match(buffer, marker)) {
                    // Extract val
                    return readValue(dataLength, bais);
                }
            }
        }
        return null;
    }

    private byte[] readValue(int dataLength, ByteArrayInputStream bais) {
        byte[] value = new byte[dataLength];
        for (int a = 0; a < dataLength; a++) {
            value[a] = (byte) unsignByte(bais);
        }
        return value;
    }

    /**
     * When we do not know the length beforehand, use this one. For a primer on the ASN.1 format, see
     * http://luca.ntop.org/Teaching/Appunti/asn1.html
     *
     * @param is
     *      InputStream of bytes to parse, should be Base64 encoded.
     * @param marker
     *      Sequence of bytes to match in the supplied stream.
     * @return
     *      The value directly following the end of the marker+length octets as an array of bytes.
     *
     */
    public byte[] parseDynamicLength(InputStream is, int[] marker) throws IOException {
        byte[] bytes = IOUtils.toByteArray(is);
        byte[] decoded = Base64.decodeBase64(bytes);

        LimitedQueue<Integer> buffer = new LimitedQueue<>(marker.length);

        try (ByteArrayInputStream bais = new ByteArrayInputStream(decoded)) {
            while (bais.available() > 0) {
                int b = unsignByte(bais);
                buffer.add(b);

                if ((buffer.size() == marker.length) && match(buffer, marker)) {

                    int lengthOctet = unsignByte(bais);
                    int contentLength = determineContentLength(lengthOctet, bais);
                    return readValue(contentLength, bais);
                }
            }
        }
        return null;
    }

    /**
     * <li>Short form. One octet. Bit 8 has value "0" and bits 7-1 give the length.</li>
     *
     * <li>Long form. Two to 127 octets. Bit 8 of first octet has value "1" and bits 7-1 give the number of additional length octets.
     * Second and following octets give the length, base 256, most significant digit first.</li>
     */
    private int determineContentLength(int lengthOctet, ByteArrayInputStream bais) {
        if (!BitHelper.isSet((byte) lengthOctet, BIT8)) {
            return lengthOctet;
        } else {
            // e.g. number of bytes that makes up the length scalar.
            byte numLengthOctets = BitHelper.unset((byte) lengthOctet, BIT8);

            int total = unsignByte(bais) * (FF + 1);
            for (int a = 1; a < numLengthOctets; a++) {
                int i = unsignByte(bais);

                total += i;
            }
            return total;
        }
    }


    /**
     * The ASN.1 data stream from NetID is supplied as Base64-encoded bytes which Java treats as signed integers
     * Perform the [byte] & 0xFF trick to transform the unsigned byte value into an int.
     */
    private int unsignByte(ByteArrayInputStream bais) {
        final int ff = FF;
        return bais.read() & ff;
    }

    /**
     * Iterate over the current sequence of bytes in the buffer. If a non-matching byte
     * is encountered, the iteration terminates and the method returns false.
     *
     * All bytes in the buffer must exactly match the supplied marker sequence.
     */
    private boolean match(LimitedQueue<Integer> buffer, int[] marker) {
        for (int a = 0; a < marker.length; a++) {
            if (!buffer.get(a).equals(marker[a])) {
                return false;
            }
        }
        return true;
    }



    /**
     * Internal subclass of LinkedList popping out elements on FIFO-basis when the defined capacity (i.e. limit) is
     * reached.
     *
     * @param <E>
     */
    private final class LimitedQueue<E> extends LinkedList<E> {

        private static final long serialVersionUID = 507838301817733410L;

        private final int limit;

        private LimitedQueue(int limit) {
            this.limit = limit;
        }

        @Override
        public boolean add(E o) {
            super.add(o);
            while (size() > limit) {
                super.remove();
            }
            return true;
        }
    }
}
