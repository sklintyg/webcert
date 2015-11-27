package se.inera.intyg.webcert.web.service.signatur.asn1;

import static se.inera.intyg.webcert.web.service.signatur.asn1.ASN1Type.SEQUENCE;
import static se.inera.intyg.webcert.web.service.signatur.asn1.ASN1Type.OBJECT_IDENTIFIER;
import static se.inera.intyg.webcert.web.service.signatur.asn1.ASN1Type.PRINTABLE_STRING;

import java.io.IOException;
import java.io.InputStream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * Provides a mechanism to parse the value of a x.520 DN serial from
 * a ASN.1 container. Typical use is to extract information from a
 * NetID signature.
 *
 * Created by eriklupander on 2015-09-04.
 */
@Component
public class ASN1UtilImpl implements ASN1Util {

    private static final Logger LOG = LoggerFactory.getLogger(ASN1UtilImpl.class);

    private static final int SEQUENCE_LENGTH = 19;   // 0x13
    private static final int OBJECT_ID_LENGTH = 3;   // 0x03
    private static final int PERSON_ID_LENGTH = 12;  // 0x0C

    // Defines a sequence of bytes that marks a X520 DN person serialNumber.
    private static final int[] X520_SERIAL_MARKER_BYTE_SEQ = new int[]{
            SEQUENCE, SEQUENCE_LENGTH,
            OBJECT_IDENTIFIER, OBJECT_ID_LENGTH, 0x55, 0x04, 0x05,
            PRINTABLE_STRING, PERSON_ID_LENGTH
    };

    private static final int[] X520_SERIAL_MARKER_BYTE_SEQ_NO_LENGTH = new int[] {
            OBJECT_IDENTIFIER, OBJECT_ID_LENGTH, 0x55, 0x04, 0x05,
            PRINTABLE_STRING
    };

    /**
     * Tries to parse a personnummer (X520 DN serial) from the supplied base64-encoded signature data.
     *
     * @param asn1Signature
     *      Base64-encoded signature. Please note that the underlying parser will have to make sure each byte is
     *      unsigned before using it.
     * @return
     *      serialNumber (e.g. personnummer) extracted from the raw signature
     * @throws
     *      IllegalArgumentException if no serialNumber (e.g. personnummer) could be parsed from the signature.
     */
    @Override
    public String parsePersonId(InputStream asn1Signature) {
        try {
            byte[] value = new ASN1StreamParser().parse(asn1Signature, X520_SERIAL_MARKER_BYTE_SEQ, PERSON_ID_LENGTH);
            return returnAsString(value);
        } catch (IOException e) {
            LOG.error("Could not parse personId from NetID signature: " + e.getMessage());
            throw new IllegalArgumentException("Could not parse personId from NetID signature, will not sign utkast: " + e.getMessage());
        }
    }

    private String returnAsString(byte[] value) {
        if (value != null) {
            return new String(value);
        } else {
            return null;
        }
    }

    /**
     * Tries to parse a hsaId from the supplied base64-encoded signature data.
     *
     * Should be present in the X520 DN serial field, but since length of the hsaId is indeterminate, we must use
     * the ASN.1 length bits to determine how many bytes to read after the marker.
     */
    @Override
    public String parseHsaId(InputStream asn1Signature) {
        try {
            byte[] value = new ASN1StreamParser().parseDynamicLength(asn1Signature, X520_SERIAL_MARKER_BYTE_SEQ_NO_LENGTH);
            return returnAsString(value);

        } catch (IOException e) {
            LOG.error("Could not parse hsaId from NetID signature: " + e.getMessage());
            throw new IllegalArgumentException("Could not parse hsaId from NetID signature, will not sign utkast: " + e.getMessage());
        }
    }

}
