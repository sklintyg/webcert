/*
 * Copyright (C) 2020 Inera AB (http://www.inera.se)
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

package se.inera.intyg.webcert.web.service.testdata;

import com.fasterxml.jackson.databind.JsonNode;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import se.inera.intyg.common.support.common.enumerations.RelationKod;
import se.inera.intyg.common.support.model.UtkastStatus;
import se.inera.intyg.schemas.contract.Personnummer;
import se.inera.intyg.webcert.persistence.utkast.model.Signatur;
import se.inera.intyg.webcert.persistence.utkast.model.Utkast;
import se.inera.intyg.webcert.persistence.utkast.model.VardpersonReferens;
import se.inera.intyg.webcert.persistence.utkast.repository.UtkastRepository;

@Transactional
@Service
public class TestDataServiceImpl implements TestDataService {

    private static final Logger LOG = LoggerFactory.getLogger(TestDataServiceImpl.class);

    @Autowired
    private UtkastRepository utkastRepository;

    @Override
    public void createIntyg(JsonNode jsonData) {

        persistIntyg(jsonData);
    }

    @Override
    public void deleteIntyg() {
        utkastRepository.deleteAll();
    }

    private void persistIntyg(JsonNode data) {
        Utkast utkast = new Utkast();
        JsonNode model = safeGet(data, "model");
        JsonNode grundData = safeGet(model, "grundData");
        JsonNode skapadAv = safeGet(grundData, "skapadAv");
        JsonNode vardenhet = safeGet(skapadAv, "vardenhet");
        JsonNode vardgivare = safeGet(vardenhet, "vardgivare");
        JsonNode patient = safeGet(grundData, "patient");

        utkast.setPatientPersonnummer(getPersonnummerFromString(safeTextGet(patient, "personId")));
        utkast.setPatientFornamn(safeTextGet(patient, "fornamn"));
        utkast.setPatientMellannamn(safeTextGet(patient, "mellannamn"));
        utkast.setPatientEfternamn(safeTextGet(patient, "efternamn"));

        utkast.setTestIntyg(safeBoolGet(grundData, "testIntyg"));

        utkast.setIntygsId(safeTextGet(model, "id"));
        utkast.setIntygsTyp(safeTextGet(model, "typ"));
        utkast.setIntygTypeVersion(safeTextGet(model, "textVersion"));

        utkast.setStatus(getUtkastStatusFromString(safeTextGet(data, "status")));

        utkast.setEnhetsId(safeTextGet(vardenhet, "enhetsid"));
        utkast.setEnhetsNamn(safeTextGet(vardenhet, "enhetsnamn"));

        utkast.setVardgivarId(safeTextGet(vardgivare, "vardgivarid"));
        utkast.setVardgivarNamn(safeTextGet(vardgivare, "vardgivarnamn"));

        VardpersonReferens senastSparad = new VardpersonReferens(safeTextGet(data, "senast_sparad_av_hsaid"),
            safeTextGet(data, "senast_sparad_av_namn"));
        utkast.setSenastSparadAv(senastSparad);
        utkast.setSenastSparadDatum(safeDateGet(data, "senast_sparad_datum"));

        VardpersonReferens creator = new VardpersonReferens(safeTextGet(skapadAv, "personId"),
            safeTextGet(skapadAv, "fullstandigtNamn"));
        utkast.setSkapadAv(creator);

        utkast.setSkapad(safeDateGet(data, "skapad"));

        utkast.setAterkalladDatum(safeDateGet(data, "aterkallad_datum"));
        utkast.setKlartForSigneringDatum(safeDateGet(data, "klart_for_signering_datum"));
        utkast.setRelationIntygsId(safeTextGet(data, "relation_intyg_id"));
        utkast.setRelationKod(getRelationKodFromString(safeTextGet(data, "relation_kod")));
        utkast.setVidarebefordrad(safeBoolGet(data, "vidarebefodrad"));
        utkast.setVersion(safeLongGet(data, "version"));
        utkast.setSkickadTillMottagare(safeTextGet(data, "skickad_till_mottagare"));
        utkast.setSkickadTillMottagareDatum(safeDateGet(data, "skickad_till_mottagare_datum"));

        utkast.setModel(model.toString());

        //utkast.setSignatur(getSignatur(safeGet(data, "signatur")));

        utkastRepository.save(utkast);
    }


    private JsonNode safeGet(JsonNode data, String key) {
        if (data == null) {
            return null;
        }

        return data.get(key);
    }

    private String safeTextGet(JsonNode data, String key) {
        if (data == null) {
            return null;
        }

        JsonNode tmp = data.get(key);
        if (tmp == null) {
            return null;
        }

        return tmp.textValue();
    }

    private Boolean safeBoolGet(JsonNode data, String key) {
        if (data == null) {
            return null;
        }

        JsonNode tmp = data.get(key);
        if (tmp == null) {
            return null;
        }

        return tmp.booleanValue();
    }

    private Long safeLongGet(JsonNode data, String key) {
        if (data == null) {
            return null;
        }

        JsonNode tmp = data.get(key);
        if (tmp == null) {
            return null;
        }

        return tmp.longValue();
    }

    private LocalDateTime safeDateGet(JsonNode data, String key) {
        if (data == null) {
            return null;
        }

        JsonNode tmp = data.get(key);
        if (tmp == null) {
            return null;
        }

        String dateString = tmp.textValue();
        if (dateString.isEmpty()) {
            return null;
        }

        return LocalDateTime.parse(dateString, DateTimeFormatter.ISO_LOCAL_DATE_TIME);
    }

    private UtkastStatus getUtkastStatusFromString(String status) {
        if (status == null || status.isEmpty()) {
            return null;
        }
        return UtkastStatus.fromValue(status);
    }

    private Personnummer getPersonnummerFromString(String personNummer) {
        if (personNummer == null || personNummer.isEmpty()) {
            return null;
        }
        return Personnummer.createPersonnummer(personNummer).orElse(null);
    }

    private RelationKod getRelationKodFromString(String relationKod) {
        if (relationKod == null || relationKod.isEmpty()) {
            return null;
        }
        return RelationKod.fromValue(relationKod);
    }

    private Signatur getSignatur(JsonNode signatur) {
        return new Signatur();
    }
}
