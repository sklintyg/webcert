/*
 * Copyright (C) 2024 Inera AB (http://www.inera.se)
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
package se.inera.intyg.webcert.web.integration.interactions.createdraftcertificate.v3;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import se.inera.intyg.common.support.modules.registry.IntygModuleRegistry;
import se.inera.intyg.infra.pu.integration.api.model.Person;
import se.inera.intyg.infra.pu.integration.api.model.PersonSvar;
import se.inera.intyg.infra.security.common.model.AuthoritiesConstants;
import se.inera.intyg.infra.security.common.model.Feature;
import se.inera.intyg.infra.security.common.model.IntygUser;
import se.inera.intyg.schemas.contract.Personnummer;
import se.inera.intyg.webcert.web.integration.interactions.createdraftcertificate.BaseCreateDraftCertificateTest;
import se.inera.intyg.webcert.web.service.patient.PatientDetailsResolver;
import se.inera.intyg.webcert.web.service.user.dto.WebCertUser;
import se.inera.intyg.webcert.web.service.utkast.dto.CreateNewDraftRequest;
import se.riv.clinicalprocess.healthcond.certificate.createdraftcertificateresponder.v3.Enhet;
import se.riv.clinicalprocess.healthcond.certificate.createdraftcertificateresponder.v3.HosPersonal;
import se.riv.clinicalprocess.healthcond.certificate.createdraftcertificateresponder.v3.Intyg;
import se.riv.clinicalprocess.healthcond.certificate.types.v3.HsaId;
import se.riv.clinicalprocess.healthcond.certificate.types.v3.PersonId;
import se.riv.clinicalprocess.healthcond.certificate.types.v3.TypAvIntyg;
import se.riv.clinicalprocess.healthcond.certificate.v3.Patient;
import se.riv.clinicalprocess.healthcond.certificate.v3.Svar;
import se.riv.clinicalprocess.healthcond.certificate.v33.Forifyllnad;
import se.riv.clinicalprocess.healthcond.certificate.v33.ObjectFactory;

@RunWith(MockitoJUnitRunner.class)
public class CreateNewDraftRequestBuilderTest extends BaseCreateDraftCertificateTest {

    public static final String PERSONNUMMER = "191212121212";
    public static final String FORNAMN = "Adam";
    public static final String FORNAMN_FROM_PU = "Erik";
    public static final String MELLANNAMN = "Cesarsson";
    public static final String MELLANNAMN_FROM_PU = "Sten";
    public static final String EFTERNAMN = "Eriksson";
    public static final String EFTERNAMN_FROM_PU = "Svensson";
    public static final String PATIENT_POSTADRESS = "postadress";
    public static final String PATIENT_POSTADRESS_FROM_PU = "postadress från pu";
    public static final String PATIENT_POSTNUMMER = "postnummer";
    public static final String PATIENT_POSTNUMMER_FROM_PU = "postnummer från pu";
    public static final String PATIENT_POSTORT = "postort";
    public static final String PATIENT_POSTORT_FROM_PU = "postort från pu";
    private static final String CERT_TYPE = "LUSE";
    private static final String INTYG_TYPE_VERSION = "1.0";

    private WebCertUser user;

    @Mock
    private IntygModuleRegistry moduleRegistry;

    @Mock
    private PatientDetailsResolver patientDetailsResolver;

    @InjectMocks
    private CreateNewDraftRequestBuilderImpl builder;

    private Person person;

    @Before
    public void setup() {
        user = buildWebCertUser();
        user.changeValdVardenhet(UNIT_HSAID);
        person = new Person(Personnummer.createPersonnummer(PERSONNUMMER).get(), false, false, FORNAMN, MELLANNAMN, EFTERNAMN,
            PATIENT_POSTADRESS, PATIENT_POSTNUMMER, PATIENT_POSTORT, false);
        final var personSvar = PersonSvar.found(person);

        when(patientDetailsResolver.getPersonFromPUService(any(Personnummer.class))).thenReturn(personSvar);
        when(moduleRegistry.getModuleIdFromExternalId(anyString()))
            .thenAnswer(invocation -> ((String) invocation.getArguments()[0]).toLowerCase());
    }

    @Test
    public void testBuildCreateNewDraftRequest() {

        CreateNewDraftRequest res = builder.buildCreateNewDraftRequest(createIntyg(), INTYG_TYPE_VERSION, user);

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
        CreateNewDraftRequest res = builder.buildCreateNewDraftRequest(createIntyg(), INTYG_TYPE_VERSION, user);

        assertNotNull(res);
        assertNotNull(res.getHosPerson());
        assertEquals(TITLE_CODE, res.getHosPerson().getBefattningar().get(0));
        assertEquals(ALLMAN_MEDICIN, res.getHosPerson().getSpecialiteter().get(0));
        assertEquals(INVARTES_MEDICIN, res.getHosPerson().getSpecialiteter().get(1));
    }

    @Test
    public void testBuildCreateNewDraftRequestWithForifyllnadFeatureEnabled() {

        final Intyg intyg = createIntyg();
        Forifyllnad forifyllnad = new ObjectFactory().createForifyllnad();
        Svar svar = new se.riv.clinicalprocess.healthcond.certificate.v3.ObjectFactory().createSvar();
        forifyllnad.getSvar().add(svar);
        intyg.setForifyllnad(forifyllnad);
        IntygUser prefillUser = buildWebCertUser();
        prefillUser.changeValdVardenhet(UNIT_HSAID);
        prefillUser.setFeatures(Stream.of(AuthoritiesConstants.FEATURE_ENABLE_CREATE_DRAFT_PREFILL)
            .collect(Collectors.toMap(Function.identity(), s -> {
                Feature feature = new Feature();
                feature.setName(s);
                feature.setGlobal(true);
                feature.setIntygstyper(Arrays.asList(CERT_TYPE.toLowerCase()));
                return feature;
            })));

        CreateNewDraftRequest res = builder.buildCreateNewDraftRequest(intyg, INTYG_TYPE_VERSION, prefillUser);

        assertNotNull(res);
        assertNotNull(res.getHosPerson());
        assertTrue(res.getForifyllnad().isPresent());
        assertEquals(forifyllnad, res.getForifyllnad().get());
    }

    @Test
    public void testBuildCreateNewDraftRequestWithForifyllnadFeatureDisabled() {

        final Intyg intyg = createIntyg();
        Forifyllnad forifyllnad = new ObjectFactory().createForifyllnad();
        Svar svar = new se.riv.clinicalprocess.healthcond.certificate.v3.ObjectFactory().createSvar();
        forifyllnad.getSvar().add(svar);
        intyg.setForifyllnad(forifyllnad);
        IntygUser prefillUser = buildWebCertUser();
        prefillUser.changeValdVardenhet(UNIT_HSAID);
        prefillUser.setFeatures(Stream.of(AuthoritiesConstants.FEATURE_ENABLE_CREATE_DRAFT_PREFILL)
            .collect(Collectors.toMap(Function.identity(), s -> {
                Feature feature = new Feature();
                feature.setName(s);
                feature.setGlobal(true);
                feature.setIntygstyper(Arrays.asList("annat"));
                return feature;
            })));

        CreateNewDraftRequest res = builder.buildCreateNewDraftRequest(intyg, INTYG_TYPE_VERSION, prefillUser);

        assertNotNull(res);
        assertNotNull(res.getHosPerson());
        assertFalse(res.getForifyllnad().isPresent());
    }

    @Test
    public void shouldUsePatientInformationFromPU() {
        person = new Person(Personnummer.createPersonnummer(PERSONNUMMER).get(), false, false,
            FORNAMN_FROM_PU, MELLANNAMN_FROM_PU, EFTERNAMN_FROM_PU, PATIENT_POSTADRESS_FROM_PU, PATIENT_POSTNUMMER_FROM_PU,
            PATIENT_POSTORT_FROM_PU, true);
        final var personSvar = PersonSvar.found(person);

        when(patientDetailsResolver.getPersonFromPUService(any(Personnummer.class))).thenReturn(personSvar);

        CreateNewDraftRequest res = builder.buildCreateNewDraftRequest(createIntyg(), INTYG_TYPE_VERSION, user);

        assertAll(
            () -> assertEquals(PERSONNUMMER, res.getPatient().getPersonId().getPersonnummer()),
            () -> assertEquals(FORNAMN_FROM_PU, res.getPatient().getFornamn()),
            () -> assertEquals(MELLANNAMN_FROM_PU, res.getPatient().getMellannamn()),
            () -> assertEquals(EFTERNAMN_FROM_PU, res.getPatient().getEfternamn()),
            () -> assertEquals(PATIENT_POSTADRESS_FROM_PU, res.getPatient().getPostadress()),
            () -> assertEquals(PATIENT_POSTNUMMER_FROM_PU, res.getPatient().getPostnummer()),
            () -> assertEquals(PATIENT_POSTORT_FROM_PU, res.getPatient().getPostort()),
            () -> assertEquals(true, res.getPatient().isTestIndicator())
        );
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