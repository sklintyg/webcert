/*
 * Copyright (C) 2017 Inera AB (http://www.inera.se)
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
package se.inera.intyg.webcert.common.model;

import se.inera.intyg.infra.security.common.service.Feature;

public enum WebcertFeature implements Feature {

    HANTERA_FRAGOR("hanteraFragor"),
    HANTERA_INTYGSUTKAST("hanteraIntygsutkast"),
    FORNYA_INTYG("fornyaIntyg"),
    MAKULERA_INTYG("makuleraIntyg"),
    MAKULERA_INTYG_KRAVER_ANLEDNING("makuleraIntygKraverAnledning"),
    SKAPA_NYFRAGA("skapaNyFraga"),
    SKICKA_INTYG("skickaIntyg"),
    SIGNERA_SKICKA_DIREKT("signeraSkickaDirekt"),
    UTSKRIFT("utskrift"),
    ARBETSGIVARUTSKRIFT("arbetsgivarUtskrift"),
    JS_LOGGNING("jsLoggning"),
    JS_MINIFIED("jsMinified", "webcert.useMinifiedJavaScript"),
    SRS("srs"),
    UNIKT_INTYG("uniktIntyg"),
    UNIKT_INTYG_INOM_VG("uniktIntygInomVg"),
    HANTERA_INTYGSUTKAST_AVLIDEN("hanteraIntygsutkastAvliden"),
    TAK_KONTROLL("takKontroll"),
    TAK_KONTROLL_TRADKLATTRING("takKontrollTradklattring");

    private final String name;
    private final String envName;

    WebcertFeature(String name) {
        this.name = name;
        this.envName = null;
    }

    WebcertFeature(String name, String envName) {
        this.name = name;
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
