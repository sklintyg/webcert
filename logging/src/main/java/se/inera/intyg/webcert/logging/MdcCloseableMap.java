/*
 * Copyright (C) 2024 Inera AB (http://www.inera.se)
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

package se.inera.intyg.webcert.logging;

import java.io.Closeable;
import java.util.Collections;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import org.slf4j.MDC;

public class MdcCloseableMap implements Closeable {
    private final Set<String> keys;
    private MdcCloseableMap(Map<String, String> entries) {
        this.keys = Collections.unmodifiableSet(entries.keySet());
        entries.forEach(MDC::put);
    }
    @Override
    public void close() {
        keys.forEach(MDC::remove);
    }
    public static Builder builder() {
        final var builder = new Builder();
        builder.put(MdcLogConstants.EVENT_CATEGORY, MdcLogConstants.EVENT_CATEGORY_PROCESS);
        return builder;
    }
    public static class Builder {
        private final Map<String, String> mdc = new ConcurrentHashMap<>();
        public Builder put(String key, String value) {
            mdc.put(key, value != null ? value : "null");
            return this;
        }
        public MdcCloseableMap build() {
            return new MdcCloseableMap(mdc);
        }
    }
}
