package se.inera.certificate.mc2wc.common.utils;

import org.joda.time.LocalDate;

public final class LocalDateAdapter {

    public static String unmarshal(LocalDate v) {
        return v.toString();
    }

    public static LocalDate marshal(String v) {
        return new LocalDate(v);
    }

}
