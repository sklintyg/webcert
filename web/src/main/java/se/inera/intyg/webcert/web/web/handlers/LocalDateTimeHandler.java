/*
 * Copyright (C) 2019 Inera AB (http://www.inera.se)
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

import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import javax.ws.rs.ext.ParamConverter;

/**
 * Handler to handle JAX-RS parameters represented by Joda-Time types.
 */
public class LocalDateTimeHandler implements javax.ws.rs.ext.ParamConverterProvider {

    @Override
    public <T> ParamConverter<T> getConverter(Class<T> aClass, Type type, Annotation[] annotations) {
        if (type.equals(LocalDateTime.class)) {
            return (ParamConverter<T>) new LocalDateTimeParamConverter();
        } else {
            return null;
        }
    }

    private static class LocalDateTimeParamConverter implements ParamConverter<LocalDateTime> {
        @Override
        public LocalDateTime fromString(String value) {
            if (value.contains("T")) {
                return LocalDateTime.parse(value, DateTimeFormatter.ISO_DATE_TIME);
            } else {
                return LocalDate.parse(value, DateTimeFormatter.ISO_DATE).atStartOfDay();
            }
        }

        @Override
        public String toString(LocalDateTime value) {
            return value.format(DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSS"));
        }

    }
}
