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
package se.inera.intyg.webcert.web.service.utkast;

import org.junit.Before;
import org.mockito.Mock;
import org.mockito.Spy;

import se.inera.intyg.common.support.model.common.internal.HoSPersonal;
import se.inera.intyg.common.support.model.common.internal.Patient;
import se.inera.intyg.common.support.model.common.internal.Vardenhet;
import se.inera.intyg.common.support.model.common.internal.Vardgivare;
import se.inera.intyg.common.support.modules.registry.IntygModuleRegistry;
import se.inera.intyg.schemas.contract.Personnummer;
import se.inera.intyg.webcert.persistence.utkast.repository.UtkastRepository;
import se.inera.intyg.webcert.web.service.intyg.IntygService;
import se.inera.intyg.webcert.web.service.user.WebCertUserService;
import se.inera.intyg.webcert.web.service.utkast.util.CreateIntygsIdStrategy;

import static org.mockito.Mockito.when;

public class AbstractBuilderTest {
    
    protected static final String INTYG_ID = "abc123";

    protected static final String INTYG_JSON = "A bit of text representing json";
    protected static final String INTYG_COPY_ID = "def456";

    protected static final Personnummer PATIENT_SSN = createPnr("19121212-1212");

    protected static final String PATIENT_FNAME = "Adam";
    protected static final String PATIENT_MNAME = "Bertil";
    protected static final String PATIENT_LNAME = "Caesarsson";

    protected static final String VARDENHET_ID = "SE00001234-5678";
    protected static final String VARDENHET_NAME = "Vårdenheten 1";

    protected static final String VARDGIVARE_ID = "SE00001234-1234";
    protected static final String VARDGIVARE_NAME = "Vårdgivaren 1";

    protected static final String HOSPERSON_ID = "SE12345678-0001";
    protected static final String HOSPERSON_NAME = "Dr Börje Dengroth";

    @Mock
    protected IntygService mockIntygService;

    @Mock
    protected UtkastRepository mockUtkastRepository;

    @Mock
    protected IntygModuleRegistry moduleRegistry;

    @Mock
    protected WebCertUserService webcertUserService;

    @Spy
    protected CreateIntygsIdStrategy mockIdStrategy = new CreateIntygsIdStrategy() {
        @Override
        public String createId() {
            return INTYG_COPY_ID;
        }
    };

    protected HoSPersonal hoSPerson;

    protected Patient patient;

    @Before
    public void setup() {
        hoSPerson = new HoSPersonal();
        hoSPerson.setPersonId(HOSPERSON_ID);
        hoSPerson.setFullstandigtNamn(HOSPERSON_NAME);

        Vardgivare vardgivare = new Vardgivare();
        vardgivare.setVardgivarid(VARDGIVARE_ID);
        vardgivare.setVardgivarnamn(VARDGIVARE_NAME);

        Vardenhet vardenhet = new Vardenhet();
        vardenhet.setEnhetsid(VARDENHET_ID);
        vardenhet.setEnhetsnamn(VARDENHET_NAME);
        vardenhet.setVardgivare(vardgivare);
        hoSPerson.setVardenhet(vardenhet);

        patient = new Patient();
        patient.setPersonId(PATIENT_SSN);
    }

    @Before
    public void expectCallToWebcertUserService() {
        when(webcertUserService.isAuthorizedForUnit(VARDGIVARE_ID, VARDENHET_ID, true)).thenReturn(true);
    }

    protected static Personnummer createPnr(String personId) {
        return Personnummer.createPersonnummer(personId)
                .orElseThrow(() -> new IllegalArgumentException("Could not parse passed personnummer: " + personId));
    }
}
