package se.inera.intyg.webcert.web.service.signatur.asn1;

/**
 * Convenience helper for checking if a given bit is set on a byte, or unsetting a bit at a given position.
 *
 * Used by {@link ASN1StreamParser} to parse the length octet.
 *
 * Created by eriklupander on 2015-10-15.
 */
public final class BitHelper {

    private BitHelper() {
    }

    public static boolean isSet(byte value, int bit) {
        return (value & (1 << bit)) != 0;
    }

    public static byte unset(byte value, int pos) {
        return (byte) (value & ~(1 << pos));
    }

}
