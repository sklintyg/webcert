package se.inera.certificate.mc2wc.common.utils;

import org.joda.time.LocalDateTime;

public final class LocalDateTimeAdapter {

    public static String unmarshal(LocalDateTime v) {
        return v.toString();
    }

    public static LocalDateTime marshal(String v) {
        return new LocalDateTime(v);
    }
}
