/*
 * Copyright (C) 2015 Inera AB (http://www.inera.se)
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

package se.inera.intyg.webcert.web.auth.authorities;

/**
 * Created by mango on 25/11/15.
 */
public final class AuthoritiesConstants {

    private AuthoritiesConstants() {
    }

    // Titles, a.k.a 'legitimerad yrkesgrupp', has a coding system governing these titles. See:
    // HSA Innehåll Legitimerad yrkesgrupp
    // http://www.inera.se/TJANSTER--PROJEKT/HSA/Dokument/HSA-kodverk/
    public static final String TITLE_LAKARE = "Läkare";
    public static final String TITLE_TANDLAKARE = "Tandläkare";

    // Title codes, a.k.a 'befattningskod', has a coding system governing these codes. See:
    // HSA Innehåll Befattning
    // http://www.inera.se/TJANSTER--PROJEKT/HSA/Dokument/HSA-kodverk/
    public static final String TITLECODE_AT_LAKARE = "204010";

    // Known roles (these roles are copied from authorities.yaml which is the master authorities configuration)
    public static final String ROLE_LAKARE = "LAKARE";
    public static final String ROLE_PRIVATLAKARE = "PRIVATLAKARE";
    public static final String ROLE_TANDLAKARE = "TANDLAKARE";
    public static final String ROLE_ADMIN = "VARDADMINISTRATOR";

    // Known privileges (these privileges are copied from authorities.yaml which is the master authorities
    // configuration)
    // Note: not all privileges are mapped, only the ones actually used in beckend
    public static final String PRIVILEGE_VISA_INTYG = "VISA_INTYG";
    public static final String PRIVILEGE_SIGNERA_INTYG = "SIGNERA_INTYG";
    public static final String PRIVILEGE_BESVARA_KOMPLETTERINGSFRAGA = "BESVARA_KOMPLETTERINGSFRAGA";
    public static final String PRIVILEGE_VIDAREBEFORDRA_FRAGASVAR = "VIDAREBEFORDRA_FRAGASVAR";
    public static final String PRIVILEGE_VIDAREBEFORDRA_UTKAST = "VIDAREBEFORDRA_UTKAST";
    public static final String PRIVILEGE_MAKULERA_INTYG = "MAKULERA_INTYG";
    public static final String PRIVILEGE_KOPIERA_INTYG = "KOPIERA_INTYG";

}
