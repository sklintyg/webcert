package se.inera.intyg.webcert.web.web.handlers;

import org.joda.time.LocalDateTime;
import org.joda.time.format.DateTimeFormatter;
import org.joda.time.format.ISODateTimeFormat;

import javax.ws.rs.ext.ParamConverter;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;

/**
 * Handler to handle JAX-RS parameters represented by Joda-Time types.
 */
public class LocalDateTimeHandler implements javax.ws.rs.ext.ParamConverterProvider {

    // Use the same parser as we do in CustomObjectMapper.
    static final DateTimeFormatter PARSER = ISODateTimeFormat.localDateOptionalTimeParser();

    @Override
    public <T> ParamConverter<T> getConverter(Class<T> aClass, Type type, Annotation[] annotations) {
        if (type.equals(LocalDateTime.class)) {
            return (ParamConverter) new LocalDateTimeParamConverter();
        } else {
            return null;
        }
    }

    private static class LocalDateTimeParamConverter implements ParamConverter<LocalDateTime> {
        @Override
        public LocalDateTime fromString(String value) {
            try {
                return PARSER.parseLocalDateTime(value);
            } catch (IllegalArgumentException e) {
                return PARSER.parseLocalDateTime(value);
            }
        }

        @Override
        public String toString(LocalDateTime value) {
            return value.toString();
        }

    }
}
