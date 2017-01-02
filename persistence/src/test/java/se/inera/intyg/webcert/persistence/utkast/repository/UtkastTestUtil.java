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
package se.inera.intyg.webcert.persistence.utkast.repository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

import se.inera.intyg.common.support.common.enumerations.RelationKod;
import se.inera.intyg.common.support.modules.support.api.dto.Personnummer;
import se.inera.intyg.webcert.persistence.utkast.model.*;

public final class UtkastTestUtil {

    private UtkastTestUtil() {
    }

    public static final String ENHET_1_ID = "ENHET_1_ID";
    public static final String ENHET_2_ID = "ENHET_2_ID";
    public static final String ENHET_3_ID = "ENHET_3_ID";

    public static final String HOS_PERSON1_ID = "SE123344332";
    public static final String HOS_PERSON1_NAMN = "Dr. Börje Dengroth";

    public static final String HOS_PERSON2_ID = "SE123493729";
    public static final String HOS_PERSON2_NAMN = "Dr. Henry Jekyl";

    public static final String HOS_PERSON3_ID = "SE123484628";
    public static final String HOS_PERSON3_NAMN = "Dr. Johan Steen";

    public static final Personnummer PERSON_NUMMER = new Personnummer("19121212-1212");
    public static final String PERSON_FORNAMN = "Tolvan";
    public static final String PERSON_MELLANNAMN = "Svensson";
    public static final String PERSON_EFTERNAMN = "Tolvansson";

    public static final String INTYGSTYP_FK7263 = "fk7263";

    public static final String MODEL = "This is the JSON model of this Intyg "
            + "with some interesting scandinavian characters like Å, Ä and ö added";

    public static Utkast buildUtkast(String enhetsId) {
        return buildUtkast(enhetsId, UtkastStatus.DRAFT_INCOMPLETE, INTYGSTYP_FK7263, HOS_PERSON1_ID, HOS_PERSON1_NAMN,
                PERSON_NUMMER, PERSON_FORNAMN, PERSON_MELLANNAMN, PERSON_EFTERNAMN, MODEL, null);
    }

    public static Utkast buildUtkast(String enhetsId, UtkastStatus status) {
        return buildUtkast(enhetsId, status, INTYGSTYP_FK7263, HOS_PERSON1_ID, HOS_PERSON1_NAMN, PERSON_NUMMER,
                PERSON_FORNAMN, PERSON_MELLANNAMN, PERSON_EFTERNAMN, MODEL, null);
    }

    public static Utkast buildUtkast(String intygsId, String enhetsId, UtkastStatus status) {
        return buildUtkast(intygsId, enhetsId, status, INTYGSTYP_FK7263, HOS_PERSON1_ID, HOS_PERSON1_NAMN, PERSON_NUMMER,
                PERSON_FORNAMN, PERSON_MELLANNAMN, PERSON_EFTERNAMN, MODEL, null);
    }

    public static Utkast buildUtkast(String enhetsId, String hoSPersonId, String hoSPersonNamn, UtkastStatus status,
            String sparadStr) {
        LocalDateTime sparad = LocalDate.parse(sparadStr).atStartOfDay();
        return buildUtkast(enhetsId, status, INTYGSTYP_FK7263, hoSPersonId, hoSPersonNamn, PERSON_NUMMER,
                PERSON_FORNAMN, PERSON_MELLANNAMN, PERSON_EFTERNAMN, MODEL, sparad);
    }

    public static Utkast buildUtkast(String enhetsId, String relationIntygsId, RelationKod relationKod) {
        return buildUtkast(UUID.randomUUID().toString(), enhetsId, UtkastStatus.DRAFT_INCOMPLETE, INTYGSTYP_FK7263, HOS_PERSON1_ID, HOS_PERSON1_NAMN,
                PERSON_NUMMER, PERSON_FORNAMN, PERSON_MELLANNAMN, PERSON_EFTERNAMN, MODEL, null, relationIntygsId, relationKod);
    }

    public static Utkast buildUtkast(String enhetsId, UtkastStatus status, String type, String hoSPersonId,
            String hoSPersonNamn, Personnummer personNummer, String personFornamn, String personMellannamn,
            String personEfternamn, String model, LocalDateTime senastSparadDatum) {
        return buildUtkast(UUID.randomUUID().toString(), enhetsId, status, type, hoSPersonId, hoSPersonNamn,
                personNummer, personFornamn, personMellannamn, personEfternamn, model, senastSparadDatum);
    }

    public static Utkast buildUtkast(String intygsId, String enhetsId, UtkastStatus status, String type,
            String hoSPersonId, String hoSPersonNamn, Personnummer personNummer, String personFornamn,
            String personMellannamn, String personEfternamn, String model, LocalDateTime senastSparadDatum) {
        return buildUtkast(intygsId, enhetsId, status, type, hoSPersonId, hoSPersonNamn, personNummer, personFornamn, personMellannamn,
                personEfternamn, model, senastSparadDatum, null, null);
    }

    public static Utkast buildUtkast(String intygsId, String enhetsId, UtkastStatus status, String type,
            String hoSPersonId, String hoSPersonNamn, Personnummer personNummer, String personFornamn,
            String personMellannamn, String personEfternamn, String model, LocalDateTime senastSparadDatum, String relationIntygsId,
            RelationKod relationKod) {
        Utkast intyg = new Utkast();
        intyg.setIntygsId(intygsId);
        intyg.setIntygsTyp(type);
        intyg.setEnhetsId(enhetsId);
        intyg.setPatientPersonnummer(personNummer);
        intyg.setPatientFornamn(personFornamn);
        intyg.setPatientMellannamn(personMellannamn);
        intyg.setPatientEfternamn(personEfternamn);
        VardpersonReferens vardpersonReferens = new VardpersonReferens();
        vardpersonReferens.setHsaId(hoSPersonId);
        vardpersonReferens.setNamn(hoSPersonNamn);
        intyg.setSenastSparadAv(vardpersonReferens);
        intyg.setSenastSparadDatum(LocalDateTime.now());
        intyg.setSkapadAv(vardpersonReferens);

        if (senastSparadDatum != null) {
            intyg.setSenastSparadDatum(senastSparadDatum);
        }

        intyg.setStatus(status);

        intyg.setModel(model);

        intyg.setRelationIntygsId(relationIntygsId);
        intyg.setRelationKod(relationKod);

        return intyg;
    }

    public static Signatur buildSignatur(String intygId, String signeradAv, LocalDateTime signeringsDatum) {
        return new Signatur(signeringsDatum, signeradAv, intygId, "<intygs-data>", "<intyg-hash>", "<signatur-data>");
    }

}
