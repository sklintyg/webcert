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
package se.inera.intyg.webcert.web.integration.builder;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.runners.MockitoJUnitRunner;
import se.inera.intyg.infra.integration.hsa.model.Vardenhet;
import se.inera.intyg.webcert.web.integration.BaseCreateDraftCertificateTest;
import se.inera.intyg.webcert.web.service.user.dto.WebCertUser;
import se.inera.intyg.webcert.web.service.utkast.dto.CreateNewDraftRequest;
import se.riv.clinicalprocess.healthcond.certificate.createdraftcertificateresponder.v1.Enhet;
import se.riv.clinicalprocess.healthcond.certificate.createdraftcertificateresponder.v1.HosPersonal;
import se.riv.clinicalprocess.healthcond.certificate.createdraftcertificateresponder.v1.Patient;
import se.riv.clinicalprocess.healthcond.certificate.createdraftcertificateresponder.v1.Utlatande;
import se.riv.clinicalprocess.healthcond.certificate.types.v1.HsaId;
import se.riv.clinicalprocess.healthcond.certificate.types.v1.PersonId;
import se.riv.clinicalprocess.healthcond.certificate.types.v1.TypAvUtlatande;
import se.riv.infrastructure.directory.v1.CommissionType;
import se.riv.infrastructure.directory.v1.PaTitleType;
import se.riv.infrastructure.directory.v1.PersonInformationType;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

@RunWith(MockitoJUnitRunner.class)
public class CreateNewDraftRequestBuilderTest extends BaseCreateDraftCertificateTest {

    private static final String CERT_TYPE = "fk7263";
    private static final String USER_HSAID = "SE1234567890";
    private static final String UNIT_HSAID = "SE0987654321";
    private static final String CAREGIVER_HSAID = "SE0000112233";


    private WebCertUser user;

    @InjectMocks
    private CreateNewDraftRequestBuilderImpl builder;

    @Before
    public void setup() {
        user = buildWebCertUser();
    }

    @Test
    public void test() {

        Vardenhet hsaVardenhet = createHsaVardenhet();

        Utlatande utlatande = createUtlatande();

        //CommissionType miu = createMIU(USER_HSAID, UNIT_HSAID, LocalDateTime.now().plusYears(2));

        CreateNewDraftRequest res = builder.buildCreateNewDraftRequest(utlatande, user);

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

        CreateNewDraftRequest res = builder.buildCreateNewDraftRequest(createUtlatande(),
                user);

        assertNotNull(res);

        assertNotNull(res.getPatient().getFullstandigtNamn());
        assertEquals("Adam Bertil Cesarsson Davidsson Eriksson", res.getPatient().getFullstandigtNamn());
    }

    @Test
    public void testWithHsaBefattningAndSpecialityNames() {

        Vardenhet hsaVardenhet = createHsaVardenhet();

        Utlatande utlatande = createUtlatande();

        // CommissionType miu = createMIU(USER_HSAID, UNIT_HSAID, LocalDateTime.now().plusYears(2));

        CreateNewDraftRequest res = builder.buildCreateNewDraftRequest(utlatande, user);

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
