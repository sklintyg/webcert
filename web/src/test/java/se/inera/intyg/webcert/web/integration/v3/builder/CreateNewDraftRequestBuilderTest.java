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
package se.inera.intyg.webcert.web.integration.v3.builder;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.when;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import se.inera.intyg.common.support.modules.registry.IntygModuleRegistry;
import se.inera.intyg.webcert.web.integration.BaseCreateDraftCertificateTest;
import se.inera.intyg.webcert.web.service.user.dto.WebCertUser;
import se.inera.intyg.webcert.web.service.utkast.dto.CreateNewDraftRequest;
import se.riv.clinicalprocess.healthcond.certificate.createdraftcertificateresponder.v3.Enhet;
import se.riv.clinicalprocess.healthcond.certificate.createdraftcertificateresponder.v3.HosPersonal;
import se.riv.clinicalprocess.healthcond.certificate.createdraftcertificateresponder.v3.Intyg;
import se.riv.clinicalprocess.healthcond.certificate.types.v3.HsaId;
import se.riv.clinicalprocess.healthcond.certificate.types.v3.PersonId;
import se.riv.clinicalprocess.healthcond.certificate.types.v3.TypAvIntyg;
import se.riv.clinicalprocess.healthcond.certificate.v3.Patient;

@RunWith(MockitoJUnitRunner.class)
public class CreateNewDraftRequestBuilderTest extends BaseCreateDraftCertificateTest {

    private static final String CERT_TYPE = "LUSE";

    public static final String PERSONNUMMER = "19121212-1212";
    public static final String FORNAMN = "Adam";
    public static final String MELLANNAMN = "Cesarsson";
    public static final String EFTERNAMN = "Eriksson";
    public static final String PATIENT_POSTADRESS = "postadress";
    public static final String PATIENT_POSTNUMMER = "postnummer";
    public static final String PATIENT_POSTORT = "postort";

    private WebCertUser user;

    @Mock
    private IntygModuleRegistry moduleRegistry;

    @InjectMocks
    private CreateNewDraftRequestBuilderImpl builder;

    @Before
    public void setup() {
        user = buildWebCertUser();
        user.changeValdVardenhet(UNIT_HSAID);

        when(moduleRegistry.getModuleIdFromExternalId(anyString()))
                .thenAnswer(invocation -> ((String) invocation.getArguments()[0]).toLowerCase());
    }

    @Test
    public void testBuildCreateNewDraftRequest() {

        CreateNewDraftRequest res = builder.buildCreateNewDraftRequest(createIntyg(), user);

        assertNotNull(res);
        assertEquals(CERT_TYPE.toLowerCase(), res.getIntygType());
        assertEquals(USER_HSAID, res.getHosPerson().getPersonId());
        assertNotNull(res.getHosPerson().getFullstandigtNamn());
        assertEquals(UNIT_HSAID, res.getHosPerson().getVardenhet().getEnhetsid());
        assertNotNull(res.getHosPerson().getVardenhet().getEnhetsnamn());
        assertNotNull(res.getHosPerson().getVardenhet().getArbetsplatsKod());
        assertNotNull(res.getHosPerson().getVardenhet().getTelefonnummer());
        assertNotNull(res.getHosPerson().getVardenhet().getPostadress());
        assertNotNull(res.getHosPerson().getVardenhet().getPostnummer());
        assertNotNull(res.getHosPerson().getVardenhet().getPostort());
        assertEquals(CAREGIVER_HSAID, res.getHosPerson().getVardenhet().getVardgivare().getVardgivarid());
        assertNotNull(res.getHosPerson().getVardenhet().getVardgivare().getVardgivarnamn());
        assertEquals(PERSONNUMMER, res.getPatient().getPersonId().getPersonnummer());
        assertEquals(FORNAMN, res.getPatient().getFornamn());
        assertEquals(MELLANNAMN, res.getPatient().getMellannamn());
        assertEquals(EFTERNAMN, res.getPatient().getEfternamn());
        assertEquals(PATIENT_POSTADRESS, res.getPatient().getPostadress());
        assertEquals(PATIENT_POSTNUMMER, res.getPatient().getPostnummer());
        assertEquals(PATIENT_POSTORT, res.getPatient().getPostort());
    }

    @Test
    public void testBuildCreateNewDraftRequestWithHsaBefattningAndSpecialityNames() {

        CreateNewDraftRequest res = builder.buildCreateNewDraftRequest(createIntyg(), user);

        assertNotNull(res);
        assertNotNull(res.getHosPerson());
        assertEquals(TITLE_CODE, res.getHosPerson().getBefattningar().get(0));
        assertEquals(ALLMAN_MEDICIN, res.getHosPerson().getSpecialiteter().get(0));
        assertEquals(INVARTES_MEDICIN, res.getHosPerson().getSpecialiteter().get(1));
    }

    private Intyg createIntyg() {
        Intyg intyg = new Intyg();

        TypAvIntyg intygTyp = new TypAvIntyg();
        intygTyp.setCode(CERT_TYPE);
        intyg.setTypAvIntyg(intygTyp);

        HosPersonal hosPerson = new HosPersonal();
        HsaId userHsaId = new HsaId();
        userHsaId.setExtension(USER_HSAID);
        userHsaId.setRoot("USERHSAID");
        hosPerson.setPersonalId(userHsaId);
        hosPerson.setFullstandigtNamn(FULLSTANDIGT_NAMN);

        Enhet hosEnhet = new Enhet();
        HsaId unitHsaId = new HsaId();
        unitHsaId.setExtension(UNIT_HSAID);
        unitHsaId.setRoot("UNITHSAID");
        hosEnhet.setEnhetsId(unitHsaId);
        hosPerson.setEnhet(hosEnhet);
        intyg.setSkapadAv(hosPerson);

        Patient patType = new Patient();
        PersonId personId = new PersonId();
        personId.setRoot("PERSNR");
        personId.setExtension(PERSONNUMMER);
        patType.setPersonId(personId);
        patType.setFornamn(FORNAMN);
        patType.setMellannamn(MELLANNAMN);
        patType.setEfternamn(EFTERNAMN);
        patType.setPostadress(PATIENT_POSTADRESS);
        patType.setPostnummer(PATIENT_POSTNUMMER);
        patType.setPostort(PATIENT_POSTORT);
        intyg.setPatient(patType);

        return intyg;
    }

}
