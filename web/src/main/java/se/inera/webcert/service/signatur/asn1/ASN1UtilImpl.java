package se.inera.webcert.service.signatur.asn1;

import static se.inera.webcert.service.signatur.asn1.ASN1Type.*;

import java.io.IOException;

import org.apache.commons.io.IOUtils;
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

    private static final Logger log = LoggerFactory.getLogger(ASN1UtilImpl.class);

    private static final int SEQUENCE_LENGTH = 19;   // 0x13
    private static final int OBJECT_ID_LENGTH = 3;   // 0x03
    private static final int PERSON_ID_LENGTH = 12;  // 0x0C

    // Defines a sequence of bytes that marks a X520 DN person serialNumber.
    private static final int[] X520_SERIAL_MARKER_BYTE_SEQ = new int[]{
            SEQUENCE, SEQUENCE_LENGTH,
            OBJECT_IDENTIFIER, OBJECT_ID_LENGTH, 0x55, 0x04, 0x05,
            PRINTABLE_STRING, PERSON_ID_LENGTH
    };

    /**
     * Tries to parse a personnummer (X520 DN serial) from the supplied base64-encoded signature data.
     *
     * @param asn1Signature
     *      Base64-encoded signature. Please note that the underlying parser will have to make sure each byte is
     *      unsigned before using it.
     * @return
     */
    public String parsePersonId(String asn1Signature) {
        try {
            return new ASN1StreamParser().parse(IOUtils.toInputStream(asn1Signature), X520_SERIAL_MARKER_BYTE_SEQ, PERSON_ID_LENGTH);
        } catch (IOException e) {
            log.error("Could not parse personId from NetID signature: " + e.getMessage());
            throw new IllegalStateException("Could not parse personId from NetID signature, will not sign utkast: " + e.getMessage());
        }
    }

}
