package se.inera.intyg.webcert.web.service.signatur.asn1;

/**
 * Defines a limited set of ASN.1 byte identifiers.
 *
 * Created by eriklupander on 2015-09-01.
 */
public final class ASN1Type {

    public static final byte SET = 0x31;
    public static final byte SEQUENCE = 0x30;
    public static final byte OBJECT_IDENTIFIER = 0x06;
    public static final byte UTF8_STRING = 0x0C;
    public static final byte PRINTABLE_STRING = 0x13;

    private ASN1Type() {
    }

}
