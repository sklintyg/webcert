package se.inera.intyg.webcert.web.service.signatur.asn1;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import java.io.IOException;
import java.io.InputStream;

import org.junit.Test;
import org.springframework.core.io.ClassPathResource;

/**
 * Created by eriklupander on 2015-10-15.
 */
public class ASN1UtilTest {

    static final String PERSON_ID = "197309069289";
    static final String HSA_ID = "TSTNMT2321000156-1028";

    private final ASN1UtilImpl asn1Util = new ASN1UtilImpl();

    @Test
    public void decodePersonIdFromASN1SigData() throws IOException {
        InputStream is = new ClassPathResource("netid-sig.txt").getInputStream();
        String value = asn1Util.parsePersonId(is);
        is.close();
        assertEquals(PERSON_ID, value);
    }

    @Test
    public void decodeHsaIdFromASN1SigData() throws IOException {
        InputStream is = new ClassPathResource("netid-siths-sig.txt").getInputStream();
        String value = asn1Util.parseHsaId(is);
        is.close();
        assertEquals(HSA_ID, value);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testExceptionThrownOnClosedStreamPassedToParsePersonId() throws IOException {
        InputStream is = new ClassPathResource("netid-sig.txt").getInputStream();
        is.close();
        asn1Util.parsePersonId(is);
        fail("You are not allowed to be here.");
    }

    @Test(expected = IllegalArgumentException.class)
    public void testExceptionThrownOnClosedStreamPassedToParseHsaId() throws IOException {
        InputStream is = new ClassPathResource("netid-siths-sig.txt").getInputStream();
        is.close();
        asn1Util.parseHsaId(is);
        fail("You are not allowed to be here.");
    }
}
