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

package se.inera.intyg.webcert.web.integration.builder;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;

import java.time.LocalDateTime;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import se.inera.intyg.common.integration.hsa.model.Vardenhet;
import se.inera.intyg.common.integration.hsa.services.HsaOrganizationsService;
import se.inera.intyg.common.integration.hsa.services.HsaPersonService;
import se.inera.intyg.webcert.web.service.utkast.dto.CreateNewDraftRequest;
import se.riv.clinicalprocess.healthcond.certificate.createdraftcertificateresponder.v1.*;
import se.riv.clinicalprocess.healthcond.certificate.types.v1.*;
import se.riv.infrastructure.directory.v1.*;

@RunWith(MockitoJUnitRunner.class)
public class CreateNewDraftRequestBuilderTest {

    private static final String CERT_TYPE = "fk7263";
    private static final String USER_HSAID = "SE1234567890";
    private static final String UNIT_HSAID = "SE0987654321";
    private static final String CAREGIVER_HSAID = "SE0000112233";
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
    public void test() {

        Vardenhet hsaVardenhet = createHsaVardenhet();
        when(orgServiceMock.getVardenhet(anyString())).thenReturn(hsaVardenhet);

        Utlatande utlatande = createUtlatande();

        CommissionType miu = createMIU(USER_HSAID, UNIT_HSAID, LocalDateTime.now().plusYears(2));

        CreateNewDraftRequest res = builder.buildCreateNewDraftRequest(utlatande, miu);

        assertNotNull(res);

        assertEquals(CERT_TYPE, res.getIntygType());

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

        assertEquals("19121212-1212", res.getPatient().getPersonId().getPersonnummer());
        assertEquals("Adam Bertil", res.getPatient().getFornamn());
        assertEquals("Cesarsson Davidsson", res.getPatient().getMellannamn());
        assertEquals("Eriksson", res.getPatient().getEfternamn());

    }

    @Test
    public void testBuildSetsPatientFullName() {
        when(orgServiceMock.getVardenhet(anyString())).thenReturn(createHsaVardenhet());
        when(hsaPersonService.getHsaPersonInfo(anyString())).thenReturn(createHsaPerson());

        CreateNewDraftRequest res = builder.buildCreateNewDraftRequest(createUtlatande(),
                createMIU(USER_HSAID, UNIT_HSAID, LocalDateTime.now().plusYears(2)));

        assertNotNull(res);

        assertNotNull(res.getPatient().getFullstandigtNamn());
        assertEquals("Adam Bertil Cesarsson Davidsson Eriksson", res.getPatient().getFullstandigtNamn());
    }

    @Test
    public void testWithHsaBefattningAndSpecialityCodes() {

        Vardenhet hsaVardenhet = createHsaVardenhet();
        when(orgServiceMock.getVardenhet(anyString())).thenReturn(hsaVardenhet);
        when(hsaPersonService.getHsaPersonInfo(anyString())).thenReturn(createHsaPerson());

        Utlatande utlatande = createUtlatande();

        CommissionType miu = createMIU(USER_HSAID, UNIT_HSAID, LocalDateTime.now().plusYears(2));

        CreateNewDraftRequest res = builder.buildCreateNewDraftRequest(utlatande, miu);

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
        pit.getSpecialityCode().add(INVARTES_MEDICIN);
        pit.getSpecialityCode().add(ALLMAN_MEDICIN);
        pitList.add(pit);
        return pitList;
    }

    private Utlatande createUtlatande() {

        Utlatande utlatande = new Utlatande();

        // Type
        TypAvUtlatande utlTyp = new TypAvUtlatande();
        utlTyp.setCode(CERT_TYPE);
        utlatande.setTypAvUtlatande(utlTyp);

        // HoSPerson
        HsaId userHsaId = new HsaId();
        userHsaId.setExtension(USER_HSAID);
        userHsaId.setRoot("USERHSAID");

        HosPersonal hosPerson = new HosPersonal();

        hosPerson.setPersonalId(userHsaId);
        hosPerson.setFullstandigtNamn(FULLSTANDIGT_NAMN);



        Enhet hosEnhet = new Enhet();
        HsaId unitHsaId = new HsaId();
        unitHsaId.setExtension(UNIT_HSAID);
        unitHsaId.setRoot("UNITHSAID");
        hosEnhet.setEnhetsId(unitHsaId);
        hosPerson.setEnhet(hosEnhet);

        utlatande.setSkapadAv(hosPerson);

        // Patient
        PersonId personId = new PersonId();
        personId.setRoot("PERSNR");
        personId.setExtension("19121212-1212");

        Patient patType = new Patient();
        patType.setPersonId(personId);
        patType.getFornamn().add("Adam");
        patType.getFornamn().add("Bertil");
        patType.getMellannamn().add("Cesarsson");
        patType.getMellannamn().add("Davidsson");
        patType.setEfternamn("Eriksson");
        utlatande.setPatient(patType);

        return utlatande;
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
