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

import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import se.inera.intyg.common.support.model.UtkastStatus;
import se.inera.intyg.schemas.contract.Personnummer;
import se.inera.intyg.webcert.persistence.utkast.model.Utkast;
import se.inera.intyg.webcert.persistence.utkast.repository.UtkastRepository;

@Service
public class TestDataServiceImpl implements TestDataService {

    private static final Logger LOG = LoggerFactory.getLogger(TestDataServiceImpl.class);

    @Autowired
    private UtkastRepository utkastRepository;

    @Override
    public void createIntyg(String jsonData) {

        Map<String, String> data = parseJsonData(jsonData);

        persistIntyg(data);
    }

    private Map<String, String> parseJsonData(String jsonData) {
        return null;
    }

    @Override
    public void deleteIntyg() {
        utkastRepository.deleteAll();
    }

    private void persistIntyg(Map<String, String> data) {
        Utkast utkast = new Utkast();
        utkast.setPatientPersonnummer(getPersonnummerFromString(data.get("PersonNummer")));
        utkast.setPatientFornamn(data.get("Fornamn"));
        utkast.setPatientMellannamn(data.get("Mellannamn"));
        utkast.setPatientEfternamn(data.get("Efternamn"));
        utkast.setTestIntyg(Boolean.getBoolean(data.get("TestIntyg")));

        utkast.setIntygsId(data.get("IntygId"));
        utkast.setIntygsTyp(data.get("IntygType"));
        utkast.setIntygTypeVersion(data.get("IntygTypeVersion"));

        utkast.setStatus(getUtkastStatusFromString(data.get("Status")));

        utkast.setModel(data.get("Model"));

//        Vardenhe vardenhet = request.getHosPerson().getVardenhet();
//
//        utkast.setEnhetsId(vardenhet.getEnhetsid());
//        utkast.setEnhetsNamn(vardenhet.getEnhetsnamn());
//
//        Object vardgivare = vardenhet.getVardgivare();
//
//        utkast.setVardgivarId(vardgivare.getVardgivarid());
//        utkast.setVardgivarNamn(vardgivare.getVardgivarnamn());
//
//        Object creator = "";
//
//        utkast.setSenastSparadAv(creator);
//        utkast.setSkapadAv(creator);
//        utkast.setSkapad();

        utkastRepository.save(utkast);
    }

    private UtkastStatus getUtkastStatusFromString(String status) {
        return null;
    }

    private Personnummer getPersonnummerFromString(String personNummer) {
        return null;
    }
}
