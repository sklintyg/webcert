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

package se.inera.intyg.webcert.web.web.util.resourcelinks.dto;

/**
 * Type of links that are returned through WebCerts REST API for relevant resources.
 *
 * These types can be resolved on the client side to assess if the user has correct access.
 */
public enum ActionLinkType {
    /**
     * Create new draft.
     */
    SKAPA_UTKAST,

    /**
     * Delete draft.
     */
    TA_BORT_UTKAST,

    /**
     * Print draft.
     */
    SKRIV_UT_UTKAST,

    /**
     * Invaliate locked draft.
     */
    MAKULERA_UTKAST,

    /**
     * Use the draft when creating new as a copy.
     */
    KOPIERA_UTKAST,

    /**
     * Renew certificate.
     */
    FORNYA_INTYG,

    /**
     * Invalidate certificate.
     */
    MAKULERA_INTYG,

    /**
     * Print certificate.
     */
    SKRIV_UT_INTYG,

    /**
     * Replace certificate.
     */
    ERSATT_INTYG,

    /**
     * Send certificate.
     */
    SKICKA_INTYG,

    /**
     * Read certificate.
     */
    LASA_INTYG,

    /**
     * Create new administrative question related to a certificate.
     */
    SKAPA_FRAGA,

    /**
     * Answer a administrative question related to a certificate.
     */
    BESVARA_FRAGA,

    /**
     * Answer a complement question related to a certificate.
     */
    BESVARA_KOMPLETTERING,

    /**
     * Read questions related to a certificate.
     */
    LASA_FRAGA,

    /**
     * Forward questions related to a certificate.
     */
    VIDAREBEFODRA_FRAGA
}
