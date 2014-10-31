package se.inera.webcert.web.handlers;

import org.apache.cxf.jaxrs.ext.ParameterHandler;
import org.joda.time.LocalDateTime;
import org.joda.time.format.DateTimeFormatter;
import org.joda.time.format.ISODateTimeFormat;

/**
 * Handler to handle JAX-RS parameters represented by Joda-Time types.
 */
public class LocalDateTimeHandler implements ParameterHandler<LocalDateTime> {

    // Use the same parser as we do in CustomObjectMapper.
    static final DateTimeFormatter PARSER = ISODateTimeFormat.localDateOptionalTimeParser();

    @Override
    public LocalDateTime fromString(String str) {
        return PARSER.parseLocalDateTime(str);
    }
}
