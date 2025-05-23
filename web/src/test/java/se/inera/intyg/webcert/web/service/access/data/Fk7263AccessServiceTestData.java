/*
 * Copyright (C) 2025 Inera AB (http://www.inera.se)
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
package se.inera.intyg.webcert.web.service.access.data;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import se.inera.intyg.infra.security.common.model.AuthoritiesConstants;

public class Fk7263AccessServiceTestData implements AccessServiceTestData {

    private static List<String> FEATURES = Arrays.asList(
        AuthoritiesConstants.FEATURE_HANTERA_INTYGSUTKAST,
        AuthoritiesConstants.FEATURE_UTSKRIFT,
        AuthoritiesConstants.FEATURE_MAKULERA_INTYG,
        AuthoritiesConstants.FEATURE_SKICKA_INTYG,
        AuthoritiesConstants.FEATURE_SKAPA_NYFRAGA,
        AuthoritiesConstants.FEATURE_HANTERA_FRAGOR);
    private static List<String> PRIVILEGES = Arrays.asList(
        AuthoritiesConstants.PRIVILEGE_SKRIVA_INTYG,
        AuthoritiesConstants.PRIVILEGE_SIGNERA_INTYG,
        AuthoritiesConstants.PRIVILEGE_VIDAREBEFORDRA_UTKAST,
        AuthoritiesConstants.PRIVILEGE_NOTIFIERING_UTKAST,
        AuthoritiesConstants.PRIVILEGE_VISA_INTYG,
        AuthoritiesConstants.PRIVILEGE_HANTERA_SEKRETESSMARKERAD_PATIENT,
        AuthoritiesConstants.PRIVILEGE_MAKULERA_INTYG,
        AuthoritiesConstants.PRIVILEGE_BESVARA_KOMPLETTERINGSFRAGA,
        AuthoritiesConstants.PRIVILEGE_LASA_FRAGA,
        AuthoritiesConstants.PRIVILEGE_MARKERA_KOMPLETTERING_SOM_HANTERAD,
        AuthoritiesConstants.PRIVILEGE_MARKERA_FRAGA_SOM_HANTERAD);

    @Override
    public List<String> getFeatures() {
        return Collections.unmodifiableList(FEATURES);
    }

    @Override
    public List<String> getPrivileges() {
        return Collections.unmodifiableList(PRIVILEGES);
    }
}
