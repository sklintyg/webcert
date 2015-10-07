package se.inera.auth.common;

import static org.junit.Assert.assertEquals;
import static se.inera.webcert.service.signatur.asn1.ASN1Type.OBJECT_IDENTIFIER;
import static se.inera.webcert.service.signatur.asn1.ASN1Type.PRINTABLE_STRING;
import static se.inera.webcert.service.signatur.asn1.ASN1Type.SEQUENCE;

import java.io.IOException;
import java.io.InputStream;

import org.junit.Test;
import org.springframework.core.io.ClassPathResource;

import se.inera.webcert.service.signatur.asn1.ASN1StreamParser;

/**
 * Created by eriklupander on 2015-09-01.
 */
public class ASN1Test {

    public static final String PERSON_ID = "197309069289";
    // 0x55, 0x04, 0x05 identifies X.520 DN SerialNumber
    private static final int SEQUENCE_LENGTH = 19; // 0x13
    private static final int OBJECT_ID_LENGTH = 3; // 0x03

    /* Builds up a sequence of bytes to match for in a stream */
    private static final int[] FIND = new int[] {
            SEQUENCE, SEQUENCE_LENGTH,
            OBJECT_IDENTIFIER, OBJECT_ID_LENGTH, 0x55, 0x04, 0x05,
            PRINTABLE_STRING, PERSON_ID.length()
    };

    @Test
    public void decodeASNSigData() throws IOException {
        InputStream is = new ClassPathResource("netid-sig.txt").getInputStream();
        String value = new ASN1StreamParser().parse(is, FIND, PERSON_ID.length());
        is.close();
        assertEquals(PERSON_ID, value);
    }
}
