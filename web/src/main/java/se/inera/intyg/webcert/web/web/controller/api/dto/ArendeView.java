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
package se.inera.intyg.webcert.web.web.controller.api.dto;

import com.google.common.collect.ImmutableList;
import java.time.LocalDate;
import java.time.LocalDateTime;
import lombok.Builder;
import lombok.Value;
import se.inera.intyg.webcert.persistence.arende.model.ArendeAmne;
import se.inera.intyg.webcert.persistence.model.Status;

@Value
@Builder
public class ArendeView {

    public enum ArendeType {
        FRAGA,
        SVAR,
        PAMINNELSE
    }
    
    ImmutableList<MedicinsktArendeView> kompletteringar;
    String internReferens;
    Status status;
    ArendeAmne amne;
    String meddelandeRubrik;
    LocalDate sistaDatumForSvar;
    Boolean vidarebefordrad;
    String frageStallare;
    ImmutableList<String> externaKontakter;
    String meddelande;
    String signeratAv;
    LocalDateTime svarSkickadDatum;
    String intygId;
    String enhetsnamn;
    String vardgivarnamn;
    LocalDateTime timestamp;
    ArendeType arendeType;
    String svarPaId;
    String paminnelseMeddelandeId;
    String vardaktorNamn;
}
