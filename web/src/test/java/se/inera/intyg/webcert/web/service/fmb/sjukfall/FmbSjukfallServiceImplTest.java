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
package se.inera.intyg.webcert.web.service.fmb.sjukfall;

import com.google.common.collect.Lists;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.*;
import org.mockito.junit.MockitoJUnitRunner;
import se.inera.intyg.clinicalprocess.healthcond.rehabilitation.listactivesickleavesforcareunit.v1.ListActiveSickLeavesForCareUnitResponderInterface;
import se.inera.intyg.clinicalprocess.healthcond.rehabilitation.listactivesickleavesforcareunit.v1.ListActiveSickLeavesForCareUnitResponseType;
import se.inera.intyg.clinicalprocess.healthcond.rehabilitation.listactivesickleavesforcareunit.v1.ListActiveSickLeavesForCareUnitType;
import se.inera.intyg.clinicalprocess.healthcond.rehabilitation.listactivesickleavesforcareunit.v1.ResultCodeEnum;
import se.inera.intyg.infra.integration.hsatk.model.legacy.Mottagning;
import se.inera.intyg.infra.integration.hsatk.model.legacy.Vardenhet;
import se.inera.intyg.infra.security.common.model.*;
import se.inera.intyg.infra.sjukfall.dto.IntygParametrar;
import se.inera.intyg.infra.sjukfall.dto.SjukfallEnhet;
import se.inera.intyg.infra.sjukfall.services.SjukfallEngineService;
import se.inera.intyg.schemas.contract.Personnummer;
import se.inera.intyg.webcert.web.service.user.WebCertUserService;
import se.inera.intyg.webcert.web.service.user.dto.WebCertUser;
import se.inera.intyg.webcert.web.web.controller.api.dto.Period;
import se.riv.clinicalprocess.healthcond.certificate.types.v2.Befattning;
import se.riv.clinicalprocess.healthcond.certificate.types.v2.HsaId;
import se.riv.clinicalprocess.healthcond.certificate.types.v2.PersonId;
import se.riv.clinicalprocess.healthcond.rehabilitation.v1.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

import static java.time.temporal.ChronoUnit.DAYS;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.times;

@RunWith(MockitoJUnitRunner.class)
public class FmbSjukfallServiceImplTest {

    private static final LocalDate START_DATUM = LocalDate.of(2019, 12, 12);
    private static final LocalDate SLUT_DATUM = LocalDate.of(2019, 12, 20);
    private static final LocalDateTime SIGNERING_TIDPUNKT = LocalDateTime.of(2018, 11, 11, 11, 11);
    private static final String INTYG_ID = "intyg-id";
    private static final String ROOT = "root";
    private static final String HSA_ID = "hsa-id";
    private static final String LAKARE_KOD = "203090";
    private static final String LAKARE_SYSTEM = "system";
    private static final String LAKARE_SYSTEM_NAME = "system-name";
    private static final String LAKARE_SYSTEM_VERSION = "system-version";
    private static final String LAKARE_DISPLAY_NAME = "LAKARE";
    private static final String LAKARE_ORIGINAL_TEXT = "original-text";
    private static final String PERSON_NUMMER = "191212121212";
    private static final int NEDSATTNING = 100;
    private static final String VARDGIVAR_NAMN = "Vard Givar Namn";
    private static final String ENHETS_NAMN = "En Hets Namn";
    private static final String FULLSTANDIGT_NAMN = "Full Standigt Namn";
    private static final String DIAGNOS_KOD = "diagnos-kod";
    private static final boolean ENKELT_INTYG = false;
    protected static final String MOTTAGNING_VE1M1 = "ve1m1";
    protected static final String VARDENHET_VE1 = "ve1";

    @Mock
    private ListActiveSickLeavesForCareUnitResponderInterface sickLeavesForCareUnit;

    @Mock
    private SjukfallEngineService sjukfallEngineService;

    @Mock
    private WebCertUserService webCertUserService;

    @Captor
    private ArgumentCaptor<ListActiveSickLeavesForCareUnitType> requestCaptor;

    @InjectMocks
    private FmbSjukfallServiceImpl fmbSjukfallService;

    private List<Period> PERIODS = Collections.emptyList();

    @Test
    public void totalSjukskrivningstidForPatientAndCareUnit() {

        final WebCertUser user = createDefaultUser(AuthoritiesConstants.PRIVILEGE_SIGNERA_INTYG);
        final ListActiveSickLeavesForCareUnitResponseType response = createResponse();

        final SjukfallEnhet sjukfallEnhet = createSjukfallForEnhet(START_DATUM, SLUT_DATUM);

        doReturn(user)
            .when(webCertUserService)
            .getUser();

        doReturn(response)
            .when(sickLeavesForCareUnit)
            .listActiveSickLeavesForCareUnit(anyString(), any(ListActiveSickLeavesForCareUnitType.class));

        doReturn(Collections.singletonList(sjukfallEnhet))
            .when(sjukfallEngineService)
            .beraknaSjukfallForEnhet(anyList(), any(IntygParametrar.class));

        final int tid = fmbSjukfallService
            .totalSjukskrivningstidForPatientAndCareUnit(Personnummer.createPersonnummer(PERSON_NUMMER).get(), PERIODS);

        Mockito.verify(sickLeavesForCareUnit, times(1)).listActiveSickLeavesForCareUnit(anyString(), requestCaptor.capture());
        final ListActiveSickLeavesForCareUnitType actualRequest = requestCaptor.getValue();

        assertEquals(VARDENHET_VE1, actualRequest.getEnhetsId().getExtension());
        assertThat(tid).isEqualTo((int) DAYS.between(START_DATUM, SLUT_DATUM) + 1);

    }

    @Test
    public void totalSjukskrivningstidForPatientAndCareUnitWithoutPreviousSickLeaves() {

        final WebCertUser user = createDefaultUser(AuthoritiesConstants.PRIVILEGE_SIGNERA_INTYG);
        final ListActiveSickLeavesForCareUnitResponseType response = createEmptyResponse();

        final SjukfallEnhet sjukfallEnhet = createSjukfallForEnhet(START_DATUM, SLUT_DATUM);

        doReturn(user)
            .when(webCertUserService)
            .getUser();

        doReturn(response)
            .when(sickLeavesForCareUnit)
            .listActiveSickLeavesForCareUnit(anyString(), any(ListActiveSickLeavesForCareUnitType.class));

        doReturn(Collections.singletonList(sjukfallEnhet))
            .when(sjukfallEngineService)
            .beraknaSjukfallForEnhet(anyList(), any(IntygParametrar.class));

        final Period period = new Period();
        period.setFrom(START_DATUM);
        period.setTom(SLUT_DATUM);
        period.setNedsattning(100);

        final int tid = fmbSjukfallService
            .totalSjukskrivningstidForPatientAndCareUnit(Personnummer.createPersonnummer(PERSON_NUMMER).get(), Arrays.asList(period));

        Mockito.verify(sickLeavesForCareUnit, times(1)).listActiveSickLeavesForCareUnit(anyString(), requestCaptor.capture());
        final ListActiveSickLeavesForCareUnitType actualRequest = requestCaptor.getValue();

        assertEquals(VARDENHET_VE1, actualRequest.getEnhetsId().getExtension());
        assertThat(tid).isEqualTo((int) DAYS.between(START_DATUM, SLUT_DATUM) + 1);

    }

    @Test
    public void totalSjukskrivningstidForPatientAndCareUnitChainOfSjukfall() {

        final WebCertUser user = createDefaultUser(AuthoritiesConstants.PRIVILEGE_SIGNERA_INTYG);
        final ListActiveSickLeavesForCareUnitResponseType response = createResponse();

        final LocalDate start1 = LocalDate.of(2019, 1, 1);
        final LocalDate end1 = LocalDate.of(2019, 1, 5);
        final SjukfallEnhet sjukfallEnhet1 = createSjukfallForEnhet(start1, end1);

        final LocalDate start2 = LocalDate.of(2019, 1, 7);
        final LocalDate end2 = LocalDate.of(2019, 1, 10);
        final SjukfallEnhet sjukfallEnhet2 = createSjukfallForEnhet(start2, end2);

        final LocalDate start3 = LocalDate.of(2019, 1, 11);
        final LocalDate end3 = LocalDate.of(2019, 1, 15);
        final SjukfallEnhet sjukfallEnhet3 = createSjukfallForEnhet(start3, end3);

        doReturn(user)
            .when(webCertUserService)
            .getUser();

        doReturn(response)
            .when(sickLeavesForCareUnit)
            .listActiveSickLeavesForCareUnit(anyString(), any(ListActiveSickLeavesForCareUnitType.class));

        doReturn(Arrays.asList(sjukfallEnhet1, sjukfallEnhet2, sjukfallEnhet3))
            .when(sjukfallEngineService)
            .beraknaSjukfallForEnhet(anyList(), any(IntygParametrar.class));

        final int tid = fmbSjukfallService
            .totalSjukskrivningstidForPatientAndCareUnit(Personnummer.createPersonnummer(PERSON_NUMMER).get(), PERIODS);

        Mockito.verify(sickLeavesForCareUnit, times(1)).listActiveSickLeavesForCareUnit(anyString(), requestCaptor.capture());
        final ListActiveSickLeavesForCareUnitType actualRequest = requestCaptor.getValue();

        assertEquals(VARDENHET_VE1, actualRequest.getEnhetsId().getExtension());
        assertThat(tid).isEqualTo(14);

    }

    @Test
    public void totalSjukskrivningstidForPatientAndCareUnitMottagning() {

        final WebCertUser user = createDefaultUser(AuthoritiesConstants.PRIVILEGE_SIGNERA_INTYG);
        user.setValdVardenhet(user.getVardgivare().get(0).getVardenheter().get(0).getMottagningar().get(0));
        final ListActiveSickLeavesForCareUnitResponseType response = createResponse();

        final SjukfallEnhet sjukfallEnhet = createSjukfallForEnhet(START_DATUM, SLUT_DATUM);

        doReturn(user)
            .when(webCertUserService)
            .getUser();

        doReturn(response)
            .when(sickLeavesForCareUnit)
            .listActiveSickLeavesForCareUnit(anyString(), any(ListActiveSickLeavesForCareUnitType.class));

        doReturn(Collections.singletonList(sjukfallEnhet))
            .when(sjukfallEngineService)
            .beraknaSjukfallForEnhet(anyList(), any(IntygParametrar.class));

        final int tid = fmbSjukfallService
            .totalSjukskrivningstidForPatientAndCareUnit(Personnummer.createPersonnummer(PERSON_NUMMER).get(), PERIODS);

        Mockito.verify(sickLeavesForCareUnit, times(1)).listActiveSickLeavesForCareUnit(anyString(), requestCaptor.capture());
        final ListActiveSickLeavesForCareUnitType actualRequest = requestCaptor.getValue();

        assertEquals(VARDENHET_VE1, actualRequest.getEnhetsId().getExtension());
        assertThat(tid).isEqualTo((int) DAYS.between(START_DATUM, SLUT_DATUM) + 1);

    }

    private SjukfallEnhet createSjukfallForEnhet(LocalDate start, LocalDate slut) {
        SjukfallEnhet sjukfallEnhet = new SjukfallEnhet();
        sjukfallEnhet.setStart(start);
        sjukfallEnhet.setSlut(slut);
        sjukfallEnhet.setDagar((int) DAYS.between(start, slut) + 1);
        sjukfallEnhet.setAktivGrad(100);

        return sjukfallEnhet;
    }

    private ListActiveSickLeavesForCareUnitResponseType createEmptyResponse() {
        ListActiveSickLeavesForCareUnitResponseType response = new ListActiveSickLeavesForCareUnitResponseType();
        response.setIntygsLista(new IntygsLista());
        response.setResultCode(ResultCodeEnum.OK);
        response.setComment("kommentar");

        return response;
    }

    private ListActiveSickLeavesForCareUnitResponseType createResponse() {

        PersonId personId1 = new PersonId();
        personId1.setRoot(ROOT);
        personId1.setExtension(PERSON_NUMMER);

        Patient patient1 = new Patient();
        patient1.setPersonId(personId1);
        patient1.setFullstandigtNamn(FULLSTANDIGT_NAMN);

        HsaId hsaId1 = new HsaId();
        hsaId1.setRoot(ROOT);
        hsaId1.setExtension(HSA_ID);

        Befattning befattning1 = new Befattning();
        befattning1.setCode(LAKARE_KOD);
        befattning1.setCodeSystem(LAKARE_SYSTEM);
        befattning1.setCodeSystemName(LAKARE_SYSTEM_NAME);
        befattning1.setCodeSystemVersion(LAKARE_SYSTEM_VERSION);
        befattning1.setDisplayName(LAKARE_DISPLAY_NAME);
        befattning1.setOriginalText(LAKARE_ORIGINAL_TEXT);

        Befattningar befattningar1 = new Befattningar();
        befattningar1.getBefattning().add(befattning1);

        Vardgivare vardgivare1 = new Vardgivare();
        vardgivare1.setVardgivarId(hsaId1);
        vardgivare1.setVardgivarnamn(VARDGIVAR_NAMN);

        Enhet enhet1 = new Enhet();
        enhet1.setEnhetsId(hsaId1);
        enhet1.setEnhetsnamn(ENHETS_NAMN);
        enhet1.setVardgivare(vardgivare1);

        HosPersonal hosPersonal1 = new HosPersonal();
        hosPersonal1.setPersonalId(hsaId1);
        hosPersonal1.setFullstandigtNamn(FULLSTANDIGT_NAMN);
        hosPersonal1.setBefattningar(befattningar1);
        hosPersonal1.setEnhet(enhet1);

        Formaga formaga1 = new Formaga();
        formaga1.setStartdatum(START_DATUM);
        formaga1.setSlutdatum(SLUT_DATUM);
        formaga1.setNedsattning(NEDSATTNING);

        Arbetsformaga arbetsformaga1 = new Arbetsformaga();
        arbetsformaga1.getFormaga().add(formaga1);

        IntygsData intygsData1 = new IntygsData();
        intygsData1.setIntygsId(INTYG_ID);
        intygsData1.setPatient(patient1);
        intygsData1.setSkapadAv(hosPersonal1);
        intygsData1.setDiagnoskod(DIAGNOS_KOD);
        intygsData1.setArbetsformaga(arbetsformaga1);
        intygsData1.setEnkeltIntyg(ENKELT_INTYG);
        intygsData1.setSigneringsTidpunkt(SIGNERING_TIDPUNKT);

        IntygsLista intygsLista = new IntygsLista();
        intygsLista.getIntygsData().add(intygsData1);

        ListActiveSickLeavesForCareUnitResponseType response = new ListActiveSickLeavesForCareUnitResponseType();
        response.setVardgivare(vardgivare1);
        response.setIntygsLista(intygsLista);
        response.setResultCode(ResultCodeEnum.OK);
        response.setComment("kommentar");

        return response;
    }

    private WebCertUser createDefaultUser(final String privilegie) {
        Map<String, Feature> featureMap = new HashMap<>();

        Feature feature1 = new Feature();
        feature1.setName(AuthoritiesConstants.FEATURE_HANTERA_INTYGSUTKAST);
        feature1.setIntygstyper(Collections.singletonList("fk7263"));
        feature1.setGlobal(true);
        featureMap.put(feature1.getName(), feature1);

        Feature feature2 = new Feature();
        feature2.setName("base_feature");
        feature2.setIntygstyper(Collections.emptyList());
        feature2.setGlobal(true);
        featureMap.put(feature2.getName(), feature2);

        return createUser(AuthoritiesConstants.ROLE_LAKARE,
            createPrivilege(privilegie,
                Collections.emptyList(),
                Lists.newArrayList(
                    createRequestOrigin(UserOriginType.NORMAL.name(), Arrays.asList("fk7263")),
                    createRequestOrigin(UserOriginType.DJUPINTEGRATION.name(), Arrays.asList("ts-bas")))),
            featureMap,
            UserOriginType.NORMAL.name());
    }

    private WebCertUser createUser(String roleName, Privilege p, Map<String, Feature> features, String origin) {
        WebCertUser user = new WebCertUser();

        HashMap<String, Privilege> privilegeHashMap = new HashMap<>();
        privilegeHashMap.put(p.getName(), p);
        user.setAuthorities(privilegeHashMap);

        user.setOrigin(origin);
        user.setFeatures(features);

        HashMap<String, Role> roleHashMap = new HashMap<>();
        Role role = new Role();
        role.setName(roleName);
        roleHashMap.put(roleName, role);

        user.setRoles(roleHashMap);

        Vardenhet vardenhet = new Vardenhet();
        vardenhet.setId(VARDENHET_VE1);
        vardenhet.setVardgivareHsaId("vardenhet-id");

        Mottagning mottagning = new Mottagning();
        mottagning.setParentHsaId(vardenhet.getId());
        mottagning.setId(MOTTAGNING_VE1M1);
        mottagning.setNamn("Mottagning1 ve1");

        vardenhet.getMottagningar().add(mottagning);
        user.setValdVardenhet(vardenhet);

        se.inera.intyg.infra.integration.hsatk.model.legacy.Vardgivare vardgivare = new se.inera.intyg.infra.integration.hsatk.model.legacy.Vardgivare();
        vardgivare.setId("vg1");
        vardgivare.getVardenheter().add(vardenhet);
        user.setVardgivare(Arrays.asList(vardgivare));
        return user;
    }

    private RequestOrigin createRequestOrigin(String name, List<String> intygstyper) {
        RequestOrigin requestOrigin = new RequestOrigin();
        requestOrigin.setName(name);
        requestOrigin.setIntygstyper(intygstyper);
        return requestOrigin;
    }

    private Privilege createPrivilege(String name, List<String> intygsTyper, List<RequestOrigin> requestOrigins) {
        Privilege privilege = new Privilege();
        privilege.setName(name);
        privilege.setIntygstyper(intygsTyper);
        privilege.setRequestOrigins(requestOrigins);
        return privilege;
    }
}
