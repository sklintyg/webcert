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
package se.inera.intyg.webcert.web.service.patient;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anySet;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import se.inera.intyg.common.db.support.DbModuleEntryPoint;
import se.inera.intyg.common.db.v1.model.internal.DbUtlatandeV1;
import se.inera.intyg.common.db.v1.rest.DbModuleApiV1;
import se.inera.intyg.common.doi.v1.rest.DoiModuleApiV1;
import se.inera.intyg.common.luae_fs.v1.rest.LuaefsModuleApiV1;
import se.inera.intyg.common.support.model.common.internal.GrundData;
import se.inera.intyg.common.support.model.common.internal.Patient;
import se.inera.intyg.common.support.model.common.internal.Utlatande;
import se.inera.intyg.common.support.modules.registry.IntygModuleRegistry;
import se.inera.intyg.common.support.modules.registry.ModuleNotFoundException;
import se.inera.intyg.common.support.modules.support.ModuleEntryPoint;
import se.inera.intyg.common.support.modules.support.api.ModuleApi;
import se.inera.intyg.common.support.modules.support.api.exception.ModuleException;
import se.inera.intyg.common.ts_bas.v6.rest.TsBasModuleApiV6;
import se.inera.intyg.common.ts_diabetes.v2.rest.TsDiabetesModuleApiV2;
import se.inera.intyg.common.ts_diabetes.v3.rest.TsDiabetesModuleApiV3;
import se.inera.intyg.infra.integration.hsa.model.Vardenhet;
import se.inera.intyg.infra.integration.hsa.model.Vardgivare;
import se.inera.intyg.infra.integration.pu.model.Person;
import se.inera.intyg.infra.integration.pu.model.PersonSvar;
import se.inera.intyg.infra.integration.pu.services.PUService;
import se.inera.intyg.infra.security.common.model.UserOriginType;
import se.inera.intyg.schemas.contract.Personnummer;
import se.inera.intyg.webcert.persistence.utkast.model.Utkast;
import se.inera.intyg.webcert.persistence.utkast.repository.UtkastRepository;
import se.inera.intyg.webcert.web.service.user.WebCertUserService;
import se.inera.intyg.webcert.web.service.user.dto.WebCertUser;
import se.inera.intyg.webcert.web.web.controller.integration.dto.IntegrationParameters;

/**
 * Created by eriklupander on 2017-08-14.
 */
@RunWith(MockitoJUnitRunner.class)
public class PatientDetailsResolverTest {

    private static final String TS_BAS_VERSION = "6.0";

    private static final Personnummer PNR = Personnummer.createPersonnummer("191212121212").get();

    private static final String FNAMN = "Tolvan";
    private static final String MNAMN = "Sexan";
    private static final String LNAMN = "Tolvansson";
    private static final String POST_ADDR = "Tolvgatan 12";
    private static final String POST_NR = "12121";
    private static final String POST_ORT = "Tolvanstad";
    private static final String INTEGR_FNAMN = "Lasse";
    private static final String INTEGR_MNAMN = "Mellansson";
    private static final String INTEGR_LNAMN = "Efternamnsson";
    private static final String INTEGR_POST_ADDR = "Integrationsv. 77";
    private static final String INTEGR_POST_NR = "99999";
    private static final String INTEGR_POST_ORT = "Intemåla";

    private static final boolean PU_AVLIDEN = false;
    private static final boolean INTEGR_AVLIDEN = true;
    private static final String DB_FNAMN = "Fille";
    private static final String DB_MNAMN = "Mellis";
    private static final String DB_ENAMN = "Enrisbuskesson";
    private static final String DB_POST_ADDR = "Mortisv. -1";
    private static final String DB_POST_NR = "666 66";
    private static final String DB_POST_ORT = "Döderhult";

    private static final String DB_INTYG_VERSION = "1.0";
    private static final String ANY_VERSION_1 = "1.0";
    private static final String TS_DIABETES_VERSION_2 = "2.6";
    private static final String TS_DIABETES_VERSION_3 = "3.0";

    @Mock
    private PUService puService;

    @Mock
    private WebCertUserService webCertUserService;

    @Mock
    private UtkastRepository utkastRepository;

    @Mock
    private IntygModuleRegistry moduleRegistry;

    @Mock
    private ModuleEntryPoint moduleEntryPoint;

    @InjectMocks
    private PatientDetailsResolverImpl testee = new PatientDetailsResolverImpl();

    @Mock
    private WebCertUser integratedWebCertUser;

    @Mock
    private WebCertUser freeWebCertUser;

    @Before
    public void init() throws Exception {
        when(webCertUserService.hasAuthenticationContext()).thenReturn(true);
        when(integratedWebCertUser.getParameters()).thenReturn(buildIntegrationParameters());
        when(integratedWebCertUser.getOrigin()).thenReturn(UserOriginType.DJUPINTEGRATION.name());

        when(freeWebCertUser.getOrigin()).thenReturn(UserOriginType.NORMAL.name());
        when(moduleRegistry.moduleExists(anyString())).thenReturn(true);

        when(moduleRegistry.getModuleApi("luae_fs", ANY_VERSION_1)).thenReturn(new LuaefsModuleApiV1());
        when(moduleRegistry.getModuleApi("ts-bas", TS_BAS_VERSION)).thenReturn(new TsBasModuleApiV6());
        when(moduleRegistry.getModuleApi("ts-diabetes", TS_DIABETES_VERSION_2)).thenReturn(new TsDiabetesModuleApiV2());
        when(moduleRegistry.getModuleApi("ts-diabetes", TS_DIABETES_VERSION_3)).thenReturn(new TsDiabetesModuleApiV3());
        when(moduleRegistry.getModuleApi("db", ANY_VERSION_1)).thenReturn(new DbModuleApiV1());
        when(moduleRegistry.getModuleApi("doi", ANY_VERSION_1)).thenReturn(new DoiModuleApiV1());
    }

    private IntegrationParameters buildIntegrationParameters() {
        IntegrationParameters params = new IntegrationParameters("ref", "hospname", "20121212-1212", INTEGR_FNAMN, INTEGR_MNAMN,
            INTEGR_LNAMN,
            INTEGR_POST_ADDR, INTEGR_POST_NR, INTEGR_POST_ORT, false, INTEGR_AVLIDEN, false, true);
        return params;
    }

    private IntegrationParameters buildIntegrationParametersWithNullAddress() {
        IntegrationParameters params = new IntegrationParameters("ref", "hospname", "20121212-1212", INTEGR_FNAMN, INTEGR_MNAMN,
            INTEGR_LNAMN,
            null, null, null, false, INTEGR_AVLIDEN, false, true);
        return params;
    }

    @Test
    public void testIsPatientAddressChangedNewPatientNull() {
        Patient oldPatient = new Patient();
        Patient newPatient = null;

        boolean changed = testee.isPatientAddressChanged(oldPatient, newPatient);

        assertTrue(changed);
    }

    @Test
    public void testIsPatientAddressChangedNotChanged() {
        Patient oldPatient = new Patient();
        oldPatient.setPostadress("GBG");
        oldPatient.setPostnummer("123");

        Patient newPatient = new Patient();
        newPatient.setPostadress("GBG");
        newPatient.setPostnummer("123");

        boolean changed = testee.isPatientAddressChanged(oldPatient, newPatient);

        assertFalse(changed);
    }

    @Test
    public void testIsPatientNamedChangedNewPatientNull() {
        Patient oldPatient = new Patient();
        Patient newPatient = null;

        boolean changed = testee.isPatientNamedChanged(oldPatient, newPatient);

        assertTrue(changed);
    }

    @Test
    public void testIsPatientNamedChangedNotChanged() {
        Patient oldPatient = new Patient();
        oldPatient.setFornamn("Test");
        oldPatient.setEfternamn("Son");

        Patient newPatient = new Patient();
        newPatient.setFornamn("Test");
        newPatient.setEfternamn("Son");

        boolean changed = testee.isPatientNamedChanged(oldPatient, newPatient);

        assertFalse(changed);
    }

    // - START FK-intyg - //

    /**
     * Standardfallet för FK-intyg är namn + sekr + avliden från PU, alltid nullad address.
     */
    @Test
    public void testFKIntygIntegrationWithPuOk() throws ModuleNotFoundException {
        when(puService.getPerson(any(Personnummer.class))).thenReturn(buildPersonSvar());
        when(webCertUserService.getUser()).thenReturn(integratedWebCertUser);

        Patient patient = testee.resolvePatient(PNR, "luae_fs", "1.0");
        assertEquals(PNR, patient.getPersonId());
        assertEquals(FNAMN, patient.getFornamn());
        assertEquals(MNAMN, patient.getMellannamn());
        assertEquals(LNAMN, patient.getEfternamn());
        assertNull(patient.getPostadress());
        assertNull(patient.getPostnummer());
        assertNull(patient.getPostort());
        assertEquals(PU_AVLIDEN, patient.isAvliden());
        assertEquals(false, patient.isSekretessmarkering());
    }

    /**
     * För FK + integration + UTAN PU vill vi ha null
     */
    @Test
    public void testFKIntygIntegrationWithPuUnavailable() throws ModuleNotFoundException {
        when(puService.getPerson(any(Personnummer.class))).thenReturn(buildErrorPersonSvar());
        when(webCertUserService.getUser()).thenReturn(integratedWebCertUser);

        Patient patient = testee.resolvePatient(PNR, "luae_fs", "1.0");
        assertEquals(null, patient);
    }

    /**
     * FK - fristående, fungerande PU.
     */
    @Test
    public void testFKIntygFristaendeWithPuOk() throws ModuleNotFoundException {
        when(puService.getPerson(any(Personnummer.class))).thenReturn(buildPersonSvar());
        when(webCertUserService.getUser()).thenReturn(freeWebCertUser);

        Patient patient = testee.resolvePatient(PNR, "luae_fs", "1.0");
        assertEquals(PNR, patient.getPersonId());
        assertEquals(FNAMN, patient.getFornamn());
        assertEquals(MNAMN, patient.getMellannamn());
        assertEquals(LNAMN, patient.getEfternamn());
        assertNull(patient.getPostadress());
        assertNull(patient.getPostnummer());
        assertNull(patient.getPostort());
        assertEquals(PU_AVLIDEN, patient.isAvliden());
        assertEquals(false, patient.isSekretessmarkering());
    }

    /**
     * FK - fristående, EJ PU.
     */
    @Test
    public void testFKIntygFristaendeWithPuUnavailable() throws ModuleNotFoundException {
        when(puService.getPerson(any(Personnummer.class))).thenReturn(buildErrorPersonSvar());
        when(webCertUserService.getUser()).thenReturn(freeWebCertUser);

        Patient patient = testee.resolvePatient(PNR, "luae_fs", "1.0");
        assertNull(patient);
    }

    // - START TS-intyg - //

    /**
     * TS - integration - PU: Namn + meta från PU, adress från INTEGR
     */
    @Test
    public void testTSIntygIntegrationWithPuOk() throws ModuleNotFoundException {
        when(puService.getPerson(any(Personnummer.class))).thenReturn(buildPersonSvar());
        when(webCertUserService.getUser()).thenReturn(integratedWebCertUser);

        Patient patient = testee.resolvePatient(PNR, "ts-bas", TS_BAS_VERSION);
        assertEquals(PNR, patient.getPersonId());
        assertEquals(FNAMN, patient.getFornamn());
        assertEquals(MNAMN, patient.getMellannamn());
        assertEquals(LNAMN, patient.getEfternamn());
        assertEquals(INTEGR_POST_ADDR, patient.getPostadress());
        assertEquals(INTEGR_POST_NR, patient.getPostnummer());
        assertEquals(INTEGR_POST_ORT, patient.getPostort());
        assertEquals(PU_AVLIDEN, patient.isAvliden());
        assertEquals(false, patient.isSekretessmarkering());
    }

    /**
     * TS - integration - PU: Namn + meta från PU, adress från PU
     */
    @Test
    public void testTSIntygIntegrationWithPuOkButAddressMissingFromIntegration() throws ModuleNotFoundException {
        when(puService.getPerson(any(Personnummer.class))).thenReturn(buildPersonSvar());
        when(webCertUserService.getUser()).thenReturn(integratedWebCertUser);
        when(integratedWebCertUser.getParameters()).thenReturn(buildIntegrationParametersWithNullAddress());

        Patient patient = testee.resolvePatient(PNR, "ts-bas", TS_BAS_VERSION);
        assertEquals(PNR, patient.getPersonId());
        assertEquals(FNAMN, patient.getFornamn());
        assertEquals(MNAMN, patient.getMellannamn());
        assertEquals(LNAMN, patient.getEfternamn());
        assertEquals(POST_ADDR, patient.getPostadress());
        assertEquals(POST_NR, patient.getPostnummer());
        assertEquals(POST_ORT, patient.getPostort());
        assertEquals(PU_AVLIDEN, patient.isAvliden());
        assertEquals(false, patient.isSekretessmarkering());
    }

    /**
     * TS + fristående + PU == Allt från PU
     */
    @Test
    public void testTSIntygFristaendeWithPuOk() throws ModuleNotFoundException {
        when(puService.getPerson(any(Personnummer.class))).thenReturn(buildPersonSvar());
        when(webCertUserService.getUser()).thenReturn(freeWebCertUser);

        Patient patient = testee.resolvePatient(PNR, "ts-bas", TS_BAS_VERSION);
        assertEquals(PNR, patient.getPersonId());
        assertEquals(FNAMN, patient.getFornamn());
        assertEquals(MNAMN, patient.getMellannamn());
        assertEquals(LNAMN, patient.getEfternamn());
        assertEquals(POST_ADDR, patient.getPostadress());
        assertEquals(POST_NR, patient.getPostnummer());
        assertEquals(POST_ORT, patient.getPostort());
        assertEquals(PU_AVLIDEN, patient.isAvliden());
        assertEquals(false, patient.isSekretessmarkering());
    }

    /**
     * TS diabetes 2 + fristående + PU == Allt från PU
     */
    @Test
    public void testTSDiabetes2IntygFristaendeWithPuOk() throws ModuleNotFoundException {
        when(puService.getPerson(any(Personnummer.class))).thenReturn(buildPersonSvar());
        when(webCertUserService.getUser()).thenReturn(freeWebCertUser);

        Patient patient = testee.resolvePatient(PNR, "ts-diabetes", TS_DIABETES_VERSION_2);
        assertEquals(PNR, patient.getPersonId());
        assertEquals(FNAMN, patient.getFornamn());
        assertEquals(MNAMN, patient.getMellannamn());
        assertEquals(LNAMN, patient.getEfternamn());
        assertEquals(POST_ADDR, patient.getPostadress());
        assertEquals(POST_NR, patient.getPostnummer());
        assertEquals(POST_ORT, patient.getPostort());
        assertEquals(PU_AVLIDEN, patient.isAvliden());
        assertEquals(false, patient.isSekretessmarkering());
    }

    /**
     * TS diabetes 3 + fristående + PU == Allt från PU
     */
    @Test
    public void testTSDiabetes3IntygFristaendeWithPuOk() throws ModuleNotFoundException {
        when(puService.getPerson(any(Personnummer.class))).thenReturn(buildPersonSvar());
        when(webCertUserService.getUser()).thenReturn(freeWebCertUser);

        Patient patient = testee.resolvePatient(PNR, "ts-diabetes", TS_DIABETES_VERSION_3);
        assertEquals(PNR, patient.getPersonId());
        assertEquals(FNAMN, patient.getFornamn());
        assertEquals(MNAMN, patient.getMellannamn());
        assertEquals(LNAMN, patient.getEfternamn());
        assertNull(patient.getPostadress());
        assertNull(patient.getPostnummer());
        assertNull(patient.getPostort());
        assertEquals(PU_AVLIDEN, patient.isAvliden());
        assertEquals(false, patient.isSekretessmarkering());
    }

    /**
     * TS + fristående + EJ PU == null
     */
    @Test
    public void testTSIntygFristaendeWithPuUnavailable() throws ModuleNotFoundException {
        when(puService.getPerson(any(Personnummer.class))).thenReturn(buildErrorPersonSvar());
        when(webCertUserService.getUser()).thenReturn(freeWebCertUser);

        Patient patient = testee.resolvePatient(PNR, "ts-bas", TS_BAS_VERSION);
        assertNull(patient);
    }

    // - START Dödsbevis - //
    // (DB har nästan exakt samma regler som TS)

    /**
     * Dödsbevis - integration - PU: Namn + meta från PU == allt hämtas från PU
     */
    @Test
    public void testSOSDBIntygIntegrationWithPuOkShouldIgnoreIntegrationParameters() throws ModuleNotFoundException {
        when(puService.getPerson(any(Personnummer.class))).thenReturn(buildPersonSvar());
        when(webCertUserService.getUser()).thenReturn(integratedWebCertUser);

        Patient patient = testee.resolvePatient(PNR, "db", ANY_VERSION_1);
        assertEquals(PNR, patient.getPersonId());
        assertEquals(FNAMN, patient.getFornamn());
        assertEquals(MNAMN, patient.getMellannamn());
        assertEquals(LNAMN, patient.getEfternamn());
        assertEquals(POST_ADDR, patient.getPostadress());
        assertEquals(POST_NR, patient.getPostnummer());
        assertEquals(POST_ORT, patient.getPostort());
        assertEquals(PU_AVLIDEN, patient.isAvliden());
        assertEquals(false, patient.isSekretessmarkering());
    }

    /**
     * Dödsbevis + integration + EJ PU, inget kan hämtas
     */
    @Test
    public void testSOSDBIntygIntegrationWithPuUnavailable() throws ModuleNotFoundException {
        when(puService.getPerson(any(Personnummer.class))).thenReturn(buildErrorPersonSvar());
        when(webCertUserService.getUser()).thenReturn(integratedWebCertUser);

        Patient patient = testee.resolvePatient(PNR, "db", ANY_VERSION_1);
        assertNull(patient);
    }

    /**
     * Dödsbevis + fristående + PU == Allt från PU
     */
    @Test
    public void testSOSDBIntygFristaendeWithPuOk() throws ModuleNotFoundException {
        when(puService.getPerson(any(Personnummer.class))).thenReturn(buildPersonSvar());
        when(webCertUserService.getUser()).thenReturn(freeWebCertUser);

        Patient patient = testee.resolvePatient(PNR, "db", ANY_VERSION_1);
        assertEquals(PNR, patient.getPersonId());
        assertEquals(FNAMN, patient.getFornamn());
        assertEquals(MNAMN, patient.getMellannamn());
        assertEquals(LNAMN, patient.getEfternamn());
        assertEquals(POST_ADDR, patient.getPostadress());
        assertEquals(POST_NR, patient.getPostnummer());
        assertEquals(POST_ORT, patient.getPostort());
        assertEquals(PU_AVLIDEN, patient.isAvliden());
        assertEquals(false, patient.isSekretessmarkering());
    }

    /**
     * Dödsbevis + fristående + EJ PU == null
     */
    @Test
    public void testSOSDBIntygFristaendeWithPuUnavailable() throws ModuleNotFoundException {
        when(puService.getPerson(any(Personnummer.class))).thenReturn(buildErrorPersonSvar());
        when(webCertUserService.getUser()).thenReturn(freeWebCertUser);

        Patient patient = testee.resolvePatient(PNR, "db", ANY_VERSION_1);
        assertNull(patient);
    }

    // - Dödsorsaksintyg - //

    /**
     * DOI - Integration. DB finns, PU finns. Namn och adress från DB-intyget, avliden/sekr från PU.
     */
    @Test
    public void testSosDoiIntygIntegrationWithExistingDBIntygAndPuOk() throws ModuleNotFoundException, IOException, ModuleException {
        when(puService.getPerson(any(Personnummer.class))).thenReturn(buildPersonSvar());
        when(webCertUserService.getUser()).thenReturn(integratedWebCertUser);
        when(integratedWebCertUser.getValdVardgivare()).thenReturn(new Vardgivare("vg-1", "vardgivare-1"));

        List<Utkast> drafts = buildSosDBDrafts();
        when(utkastRepository.findDraftsByPatientAndVardgivareAndStatus(anyString(), anyString(), anyList(),
            anySet())).thenReturn(drafts);

        ModuleApi moduleApi = mock(ModuleApi.class);
        when(moduleApi.getUtlatandeFromJson(anyString())).thenReturn(buildSosDbUtlatande());
        when(moduleRegistry.getModuleApi(DbModuleEntryPoint.MODULE_ID, DB_INTYG_VERSION)).thenReturn(moduleApi);

        Patient patient = testee.resolvePatient(PNR, "doi", ANY_VERSION_1);
        assertEquals(PNR.getPersonnummer(), patient.getPersonId().getPersonnummer());
        assertEquals(DB_FNAMN, patient.getFornamn());
        assertEquals(DB_MNAMN, patient.getMellannamn());
        assertEquals(DB_ENAMN, patient.getEfternamn());
        assertEquals(DB_POST_ADDR, patient.getPostadress());
        assertEquals(DB_POST_NR, patient.getPostnummer());
        assertEquals(DB_POST_ORT, patient.getPostort());
        assertEquals(PU_AVLIDEN, patient.isAvliden());
        assertEquals(false, patient.isSekretessmarkering());
    }

    /**
     * DOI - Integration. DB saknas, PU finns == allt från PU
     */
    @Test
    public void testSosDoiIntygIntegrationWithNoDBIntygAndPuOk() throws ModuleNotFoundException {
        when(puService.getPerson(any(Personnummer.class))).thenReturn(buildPersonSvar());
        when(webCertUserService.getUser()).thenReturn(integratedWebCertUser);
        when(integratedWebCertUser.getValdVardgivare()).thenReturn(new Vardgivare("vg-1", "vardgivare-1"));

        List<Utkast> drafts = new ArrayList<>();
        when(utkastRepository.findDraftsByPatientAndVardgivareAndStatus(anyString(), anyString(), anyList(),
            anySet())).thenReturn(drafts);

        Patient patient = testee.resolvePatient(PNR, "doi", ANY_VERSION_1);
        assertEquals(PNR.getPersonnummer(), patient.getPersonId().getPersonnummer());
        assertEquals(FNAMN, patient.getFornamn());
        assertEquals(MNAMN, patient.getMellannamn());
        assertEquals(LNAMN, patient.getEfternamn());
        assertEquals(POST_ADDR, patient.getPostadress());
        assertEquals(POST_NR, patient.getPostnummer());
        assertEquals(POST_ORT, patient.getPostort());
        assertEquals(PU_AVLIDEN, patient.isAvliden());
        assertEquals(false, patient.isSekretessmarkering());
    }

    /**
     * DOI - Integration. DB saknas, PU saknas == Ingen info
     */
    @Test
    public void testSosDoiIntygIntegrationWithNoDBIntygAndPuUnavailable() throws ModuleNotFoundException {
        when(puService.getPerson(any(Personnummer.class))).thenReturn(buildErrorPersonSvar());
        when(webCertUserService.getUser()).thenReturn(integratedWebCertUser);

        Patient patient = testee.resolvePatient(PNR, "doi", ANY_VERSION_1);
        assertNull(patient);
    }

    /**
     * DOI - Fristående. DB saknas, PU finns. Namn och adress från PU.
     */
    @Test
    public void testSosDoiIntygFristaendeWithNoDBIntygAndPuOk() throws ModuleNotFoundException, IOException {
        when(puService.getPerson(any(Personnummer.class))).thenReturn(buildPersonSvar());
        when(webCertUserService.getUser()).thenReturn(freeWebCertUser);
        when(freeWebCertUser.getValdVardenhet()).thenReturn(buildVardenhet());

        Patient patient = testee.resolvePatient(PNR, "doi", ANY_VERSION_1);
        assertEquals(PNR.getPersonnummer(), patient.getPersonId().getPersonnummer());
        assertEquals(FNAMN, patient.getFornamn());
        assertEquals(MNAMN, patient.getMellannamn());
        assertEquals(LNAMN, patient.getEfternamn());
        assertEquals(POST_ADDR, patient.getPostadress());
        assertEquals(POST_NR, patient.getPostnummer());
        assertEquals(POST_ORT, patient.getPostort());
        assertEquals(PU_AVLIDEN, patient.isAvliden());
        assertEquals(false, patient.isSekretessmarkering());
    }

    private Vardenhet buildVardenhet() {
        return new Vardenhet("ve-1", "vardenhet-1");
    }

    /**
     * DOI - Fristående. DB saknas, PU saknas. Rubbet från Integration
     */
    @Test
    public void testSosDoiIntygFristaendeWithNoDBIntygAndPuUnavailable() throws ModuleNotFoundException, IOException {
        when(puService.getPerson(any(Personnummer.class))).thenReturn(buildErrorPersonSvar());
        when(webCertUserService.getUser()).thenReturn(freeWebCertUser);

        Patient patient = testee.resolvePatient(PNR, "doi", ANY_VERSION_1);
        assertNull(patient);
    }


    private Utlatande buildSosDbUtlatande() {
        GrundData grundData = new GrundData();
        grundData.setPatient(buildPatient());
        return DbUtlatandeV1.builder()
            .setGrundData(grundData)
            .setId("abc-123")
            .setTextVersion("1.0")
            .build();
    }

    private Patient buildPatient() {
        Patient patient = new Patient();
        patient.setPersonId(PNR);
        patient.setFornamn(DB_FNAMN);
        patient.setMellannamn(DB_MNAMN);
        patient.setEfternamn(DB_ENAMN);
        patient.setPostadress(DB_POST_ADDR);
        patient.setPostnummer(DB_POST_NR);
        patient.setPostort(DB_POST_ORT);
        patient.setAvliden(true);
        return patient;
    }

    private List<Utkast> buildSosDBDrafts() {
        Utkast u1 = mock(Utkast.class);
        when(u1.getModel()).thenReturn("the model");
        when(u1.getIntygTypeVersion()).thenReturn(DB_INTYG_VERSION);
        ArrayList<Utkast> utkastList = new ArrayList<>();
        utkastList.add(u1);
        return utkastList;
    }

    private PersonSvar buildPersonSvar() {
        Person person = buildPerson();
        return PersonSvar.found(person);
    }

    private PersonSvar buildErrorPersonSvar() {
        return PersonSvar.error();
    }

    private Person buildPerson() {
        return new Person(PNR, false, PU_AVLIDEN, FNAMN, MNAMN, LNAMN, POST_ADDR, POST_NR, POST_ORT);
    }
}
