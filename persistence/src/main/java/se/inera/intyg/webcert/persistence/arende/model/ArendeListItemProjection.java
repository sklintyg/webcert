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
package se.inera.intyg.webcert.persistence.arende.model;

import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Getter;
import se.inera.intyg.webcert.persistence.model.Status;

/**
 * Lightweight projection for Arende list queries.
 * Only contains fields needed for list display, avoiding EAGER fetch of collections.
 */
@Getter
@AllArgsConstructor
public class ArendeListItemProjection {

    private final String meddelandeId;
    private final String intygId;
    private final String intygTyp;
    private final String signeratAv;
    private final String signeratAvName;
    private final Status status;
    private final String patientPersonId;
    private final LocalDateTime senasteHandelse;
    private final Boolean vidarebefordrad;
    private final String skickatAv;
    private final ArendeAmne amne;
    private final String enhetName;
    private final String vardgivareName;
}