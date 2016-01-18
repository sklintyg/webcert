/*
 * Copyright (C) 2016 Inera AB (http://www.inera.se)
 *
 * This file is part of sklintyg (https://github.com/sklintyg).
 *
 * sklintyg is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * sklintyg is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

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
