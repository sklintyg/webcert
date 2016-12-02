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

package se.inera.intyg.webcert.web.service.feature;

import se.inera.intyg.infra.security.common.service.Feature;

public enum WebcertFeature implements Feature {

    HANTERA_FRAGOR("hanteraFragor"),
    HANTERA_INTYGSUTKAST("hanteraIntygsutkast"),
    KOPIERA_INTYG("kopieraIntyg"),
    MAKULERA_INTYG("makuleraIntyg"),
    SKAPA_NYFRAGA("skapaNyFraga"),
    SKICKA_INTYG("skickaIntyg"),
    UTSKRIFT("utskrift"),
    ARBETSGIVARUTSKRIFT("arbetsgivarUtskrift"),
    JS_LOGGNING("jsLoggning"),
    JS_MINIFIED("jsMinified", "webcert.useMinifiedJavaScript");

    private final String name;
    private String envName;

    WebcertFeature(String name) {
        this.name = name;
    }

    WebcertFeature(String name, String envName) {
        this(name);
        this.envName = envName;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public String getEnvName() {
        return envName;
    }
}
