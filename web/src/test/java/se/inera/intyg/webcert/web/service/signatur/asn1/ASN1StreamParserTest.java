package se.inera.intyg.webcert.web.service.signatur.asn1;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import java.io.IOException;
import java.io.InputStream;

import org.junit.Test;
import org.springframework.core.io.ClassPathResource;

/**
 * Tests a 255+ byte long parse from a NetID / ASN1 sig.
 *
 * This test together with {@link ASN1UtilTest} should give sufficient coverage.
 *
 * Created by eriklupander on 2015-09-01.
 */
public class ASN1StreamParserTest {

    /* For test purposes, used since the length of the SHA1 enc is > 255 */
    private static final int[] FIND_SHA_1_ENC = new int[]{
            0x06, 0x09, 0x2A, 0x86, 0x48, 0x86, 0xF7, 0x0D, 0x01, 0x01, 0x05, 0x05, 0x00, 0x03
    };
    public static final int EXPECTED_LEN = 513;

    @Test
    public void decodeValueWithLengthBitSet() throws IOException {
        InputStream is = new ClassPathResource("netid-siths-sig.txt").getInputStream();
        byte[] value = new ASN1StreamParser().parseDynamicLength(is, FIND_SHA_1_ENC);
        assertNotNull(value);
        assertEquals(EXPECTED_LEN, value.length);
    }

    @Test
    public void decodingOfUnknownFixedLengthValueReturnsNull() throws IOException {
        InputStream is = new ClassPathResource("netid-siths-sig.txt").getInputStream();
        byte[] value = new ASN1StreamParser().parse(is, new int[]{0x04, 0x03, 0x2B, 0x36, 0x38, 0x56}, 6);
        assertNull(value);
    }

    @Test
    public void decodingOfUnknownDynamicValueReturnsNull() throws IOException {
        InputStream is = new ClassPathResource("netid-siths-sig.txt").getInputStream();
        byte[] value = new ASN1StreamParser().parseDynamicLength(is, new int[]{0x04, 0x03, 0x2B, 0x36, 0x38, 0x56});
        assertNull(value);
    }
}
