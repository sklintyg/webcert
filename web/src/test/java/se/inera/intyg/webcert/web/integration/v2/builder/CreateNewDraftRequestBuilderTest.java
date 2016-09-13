/*
 * Copyright (C) 2016 Inera AB (http://www.inera.se)
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

package se.inera.intyg.webcert.web.integration.v2.builder;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import se.inera.intyg.common.integration.hsa.model.Vardenhet;
import se.inera.intyg.common.integration.hsa.services.HsaOrganizationsService;
import se.inera.intyg.common.integration.hsa.services.HsaPersonService;
import se.inera.intyg.webcert.web.service.utkast.dto.CreateNewDraftRequest;
import se.riv.clinicalprocess.healthcond.certificate.createdraftcertificateresponder.v2.*;
import se.riv.clinicalprocess.healthcond.certificate.types.v2.*;
import se.riv.clinicalprocess.healthcond.certificate.v2.Patient;
import se.riv.infrastructure.directory.v1.*;

@RunWith(MockitoJUnitRunner.class)
public class CreateNewDraftRequestBuilderTest {

    private static final String CERT_TYPE = "LUSE";
    private static final String USER_HSAID = "SE1234567890";
    private static final String UNIT_HSAID = "SE0987654321";
    private static final String CAREGIVER_HSAID = "SE0000112233";
    public static final String PERSONNUMMER = "19121212-1212";
    public static final String FORNAMN = "Adam";
    public static final String MELLANNAMN = "Cesarsson";
    public static final String EFTERNAMN = "Eriksson";
    public static final String PATIENT_POSTADRESS = "postadress";
    public static final String PATIENT_POSTNUMMER = "postnummer";
    public static final String PATIENT_POSTORT = "postort";
    public static final String FULLSTANDIGT_NAMN = "Abel Baker";
    public static final String INVARTES_MEDICIN = "Invärtes medicin";
    public static final String TITLE_CODE = "203010";
    public static final String TITLE_NAME = "Läkare";
    public static final String ALLMAN_MEDICIN = "Allmänmedicin";

    @Mock
    private HsaOrganizationsService orgServiceMock;

    @Mock
    private HsaPersonService hsaPersonService;

    @InjectMocks
    private CreateNewDraftRequestBuilderImpl builder;

    @Test
    public void testBuildCreateNewDraftRequest() {
        when(orgServiceMock.getVardenhet(anyString())).thenReturn(createHsaVardenhet());

        CommissionType miu = createMIU(USER_HSAID, UNIT_HSAID, LocalDateTime.now().plusYears(2));

        CreateNewDraftRequest res = builder.buildCreateNewDraftRequest(createIntyg(), miu);

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

    @Ignore
    public void testBuildCreateNewDraftRequestWithHsaBefattningAndSpecialityNames() {
        when(orgServiceMock.getVardenhet(anyString())).thenReturn(createHsaVardenhet());
        when(hsaPersonService.getHsaPersonInfo(anyString())).thenReturn(createHsaPerson());

        CommissionType miu = createMIU(USER_HSAID, UNIT_HSAID, LocalDateTime.now().plusYears(2));

        CreateNewDraftRequest res = builder.buildCreateNewDraftRequest(createIntyg(), miu);

        assertNotNull(res);
        assertNotNull(res.getHosPerson());
        assertEquals(TITLE_CODE, res.getHosPerson().getBefattningar().get(0));
        assertEquals(ALLMAN_MEDICIN, res.getHosPerson().getSpecialiteter().get(0));
        assertEquals(INVARTES_MEDICIN, res.getHosPerson().getSpecialiteter().get(1));
    }

    private Vardenhet createHsaVardenhet() {
        Vardenhet hsaVardenhet = new Vardenhet();
        hsaVardenhet.setId(UNIT_HSAID);
        hsaVardenhet.setNamn("Vardenheten");
        hsaVardenhet.setArbetsplatskod("0000001");
        hsaVardenhet.setPostadress("Postaddr");
        hsaVardenhet.setPostnummer("12345");
        hsaVardenhet.setPostort("Staden");
        hsaVardenhet.setTelefonnummer("0123-456789");

        return hsaVardenhet;
    }

    private List<PersonInformationType> createHsaPerson() {
        List<PersonInformationType> pitList = new ArrayList<>();
        PersonInformationType pit = new PersonInformationType();
        pit.setPersonHsaId(USER_HSAID);
        pit.setGivenName(FULLSTANDIGT_NAMN);
        PaTitleType befattning = new PaTitleType();
        befattning.setPaTitleCode(TITLE_CODE);
        befattning.setPaTitleName(TITLE_NAME);
        pit.getPaTitle().add(befattning);
        pit.getSpecialityName().add(INVARTES_MEDICIN);
        pit.getSpecialityName().add(ALLMAN_MEDICIN);
        pitList.add(pit);
        return pitList;
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

    private CommissionType createMIU(String personHsaId, String unitHsaId,
            LocalDateTime miuEndDate) {
        CommissionType miu = new CommissionType();
        miu.setHealthCareProviderHsaId(CAREGIVER_HSAID);
        miu.setHealthCareProviderName("Landstinget");
        miu.setHealthCareUnitName("Sjukhuset");
        miu.setHealthCareUnitHsaId(unitHsaId);
        miu.setHealthCareUnitEndDate(miuEndDate);
        miu.setCommissionHsaId(personHsaId);
        return miu;
    }
}
