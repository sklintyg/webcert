package se.inera.certificate.mc2wc.converter;

import org.apache.commons.lang.CharUtils;

public class FragaSvarUtils {

    public static boolean detectIfSamordningsNummer(String personNr) {
        char dateDigit = personNr.charAt(6);
        return (CharUtils.toIntValue(dateDigit) >= 6);
    }
}
