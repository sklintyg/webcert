package se.inera.intyg.webcert.web.service.patient;

import org.jetbrains.annotations.NotNull;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import se.inera.intyg.common.sos_db.model.internal.DbUtlatande;
import se.inera.intyg.common.support.model.common.internal.GrundData;
import se.inera.intyg.common.support.model.common.internal.Patient;
import se.inera.intyg.common.support.model.common.internal.Utlatande;
import se.inera.intyg.common.support.modules.registry.IntygModuleRegistry;
import se.inera.intyg.common.support.modules.registry.ModuleNotFoundException;
import se.inera.intyg.common.support.modules.support.api.ModuleApi;
import se.inera.intyg.infra.integration.hsa.model.Vardenhet;
import se.inera.intyg.infra.integration.hsa.model.Vardgivare;
import se.inera.intyg.infra.integration.pu.model.Person;
import se.inera.intyg.infra.integration.pu.model.PersonSvar;
import se.inera.intyg.infra.integration.pu.services.PUService;
import se.inera.intyg.schemas.contract.Personnummer;
import se.inera.intyg.webcert.persistence.utkast.model.Utkast;
import se.inera.intyg.webcert.persistence.utkast.repository.UtkastRepository;
import se.inera.intyg.webcert.web.security.WebCertUserOriginType;
import se.inera.intyg.webcert.web.service.user.WebCertUserService;
import se.inera.intyg.webcert.web.service.user.dto.IntegrationParameters;
import se.inera.intyg.webcert.web.service.user.dto.WebCertUser;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyList;
import static org.mockito.Matchers.anySet;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Created by eriklupander on 2017-08-14.
 */
@RunWith(MockitoJUnitRunner.class)
public class PatientDetailsResolverTest {

    private static final Personnummer PNR = Personnummer.createValidatedPersonnummerWithDash("191212121212").get();
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

    @Mock
    private PUService puService;

    @Mock
    private WebCertUserService webCertUserService;

    @Mock
    private UtkastRepository utkastRepository;

    @Mock
    private IntygModuleRegistry moduleRegistry;

    @InjectMocks
    private PatientDetailsResolverImpl testee = new PatientDetailsResolverImpl();

    @Mock
    private WebCertUser integratedWebCertUser;

    @Mock
    private WebCertUser freeWebCertUser;

    @Before
    public void init() {
        when(webCertUserService.hasAuthenticationContext()).thenReturn(true);
        when(integratedWebCertUser.getParameters()).thenReturn(buildIntegrationParameters());
        when(integratedWebCertUser.getOrigin()).thenReturn(WebCertUserOriginType.DJUPINTEGRATION.name());

        when(freeWebCertUser.getParameters()).thenReturn(null);
        when(freeWebCertUser.getOrigin()).thenReturn(WebCertUserOriginType.NORMAL.name());
    }

    private IntegrationParameters buildIntegrationParameters() {
        IntegrationParameters params = new IntegrationParameters("ref", "hospname", "20121212-1212", INTEGR_FNAMN, INTEGR_MNAMN,
                INTEGR_LNAMN,
                INTEGR_POST_ADDR, INTEGR_POST_NR, INTEGR_POST_ORT, false, true, false, true);
        return params;
    }

    private IntegrationParameters buildIntegrationParametersWithNullAddress() {
        IntegrationParameters params = new IntegrationParameters("ref", "hospname", "20121212-1212", INTEGR_FNAMN, INTEGR_MNAMN,
                INTEGR_LNAMN,
                null, null, null, false, true, false, true);
        return params;
    }

    // - START FK-intyg - //

    /**
     * Standardfallet för FK-intyg är namn + sekr + avliden från PU, alltid nullad address.
     */
    @Test
    public void testFKIntygIntegrationWithPuOk() {
        when(puService.getPerson(any(Personnummer.class))).thenReturn(buildPersonSvar());
        when(webCertUserService.getUser()).thenReturn(integratedWebCertUser);

        Patient patient = testee.resolvePatient(PNR, "luae_fs");
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
     * För FK + integration + UTAN PU vill vi ha namn etc från Integrations-parametrar.
     */
    @Test
    public void testFKIntygIntegrationWithPuUnavailable() {
        when(puService.getPerson(any(Personnummer.class))).thenReturn(buildErrorPersonSvar());
        when(webCertUserService.getUser()).thenReturn(integratedWebCertUser);

        Patient patient = testee.resolvePatient(PNR, "luae_fs");
        assertEquals(PNR, patient.getPersonId());
        assertEquals(INTEGR_FNAMN, patient.getFornamn());
        assertEquals(INTEGR_MNAMN, patient.getMellannamn());
        assertEquals(INTEGR_LNAMN, patient.getEfternamn());
        assertNull(patient.getPostadress());
        assertNull(patient.getPostnummer());
        assertNull(patient.getPostort());
        assertEquals(INTEGR_AVLIDEN, patient.isAvliden());
        assertEquals(false, patient.isSekretessmarkering());
    }

    /**
     * FK - fristående, fungerande PU.
     */
    @Test
    public void testFKIntygFristaendeWithPuOk() {
        when(puService.getPerson(any(Personnummer.class))).thenReturn(buildPersonSvar());
        when(webCertUserService.getUser()).thenReturn(freeWebCertUser);

        Patient patient = testee.resolvePatient(PNR, "luae_fs");
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
    public void testFKIntygFristaendeWithPuUnavailable() {
        when(puService.getPerson(any(Personnummer.class))).thenReturn(buildErrorPersonSvar());
        when(webCertUserService.getUser()).thenReturn(freeWebCertUser);

        Patient patient = testee.resolvePatient(PNR, "luae_fs");
        assertNull(patient);
    }

    // - START TS-intyg - //

    /**
     * TS - integration - PU: Namn + meta från PU, adress från INTEGR
     */
    @Test
    public void testTSIntygIntegrationWithPuOk() {
        when(puService.getPerson(any(Personnummer.class))).thenReturn(buildPersonSvar());
        when(webCertUserService.getUser()).thenReturn(integratedWebCertUser);

        Patient patient = testee.resolvePatient(PNR, "ts-bas");
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
    public void testTSIntygIntegrationWithPuOkButAddressMissingFromIntegration() {
        when(puService.getPerson(any(Personnummer.class))).thenReturn(buildPersonSvar());
        when(webCertUserService.getUser()).thenReturn(integratedWebCertUser);
        when(integratedWebCertUser.getParameters()).thenReturn(buildIntegrationParametersWithNullAddress());

        Patient patient = testee.resolvePatient(PNR, "ts-bas");
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
     * TS + integration + EJ PU, allt som går skall hämtas från parametrar.
     */
    @Test
    public void testTSIntygIntegrationWithPuUnavailable() {
        when(puService.getPerson(any(Personnummer.class))).thenReturn(buildErrorPersonSvar());
        when(webCertUserService.getUser()).thenReturn(integratedWebCertUser);

        Patient patient = testee.resolvePatient(PNR, "ts-bas");
        assertEquals(PNR, patient.getPersonId());
        assertEquals(INTEGR_FNAMN, patient.getFornamn());
        assertEquals(INTEGR_MNAMN, patient.getMellannamn());
        assertEquals(INTEGR_LNAMN, patient.getEfternamn());
        assertEquals(INTEGR_POST_ADDR, patient.getPostadress());
        assertEquals(INTEGR_POST_NR, patient.getPostnummer());
        assertEquals(INTEGR_POST_ORT, patient.getPostort());
        assertEquals(INTEGR_AVLIDEN, patient.isAvliden());
        assertEquals(false, patient.isSekretessmarkering());
    }

    /**
     * TS + fristående + PU == Allt från PU
     */
    @Test
    public void testTSIntygFristaendeWithPuOk() {
        when(puService.getPerson(any(Personnummer.class))).thenReturn(buildPersonSvar());
        when(webCertUserService.getUser()).thenReturn(freeWebCertUser);

        Patient patient = testee.resolvePatient(PNR, "ts-bas");
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
     * TS + fristående + EJ PU == null
     */
    @Test
    public void testTSIntygFristaendeWithPuUnavailable() {
        when(puService.getPerson(any(Personnummer.class))).thenReturn(buildErrorPersonSvar());
        when(webCertUserService.getUser()).thenReturn(freeWebCertUser);

        Patient patient = testee.resolvePatient(PNR, "ts-bas");
        assertNull(patient);
    }

    // - START Dödsbevis - //
    // (DB har nästan exakt samma regler som TS)

    /**
     * Dödsbevis - integration - PU: Namn + meta från PU, adress från INTEGR
     */
    @Test
    public void testSOSDBIntygIntegrationWithPuOk() {
        when(puService.getPerson(any(Personnummer.class))).thenReturn(buildPersonSvar());
        when(webCertUserService.getUser()).thenReturn(integratedWebCertUser);

        Patient patient = testee.resolvePatient(PNR, "db");
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
     * Dödsbevis - integration - PU: Namn + meta från PU, adress från PU
     */
    @Test
    public void testSOSDBIntygIntegrationWithPuOkButAddressMissingFromIntegration() {
        when(puService.getPerson(any(Personnummer.class))).thenReturn(buildPersonSvar());
        when(webCertUserService.getUser()).thenReturn(integratedWebCertUser);
        when(integratedWebCertUser.getParameters()).thenReturn(buildIntegrationParametersWithNullAddress());

        Patient patient = testee.resolvePatient(PNR, "db");
        assertEquals(PNR, patient.getPersonId());
        assertEquals(FNAMN, patient.getFornamn());
        assertEquals(MNAMN, patient.getMellannamn());
        assertEquals(LNAMN, patient.getEfternamn());
        assertEquals(null, patient.getPostadress());
        assertEquals(null, patient.getPostnummer());
        assertEquals(null, patient.getPostort());
        assertEquals(PU_AVLIDEN, patient.isAvliden());
        assertEquals(false, patient.isSekretessmarkering());
    }

    /**
     * Dödsbevis + integration + EJ PU, allt som går skall hämtas från parametrar.
     */
    @Test
    public void testSOSDBIntygIntegrationWithPuUnavailable() {
        when(puService.getPerson(any(Personnummer.class))).thenReturn(buildErrorPersonSvar());
        when(webCertUserService.getUser()).thenReturn(integratedWebCertUser);

        Patient patient = testee.resolvePatient(PNR, "db");
        assertEquals(PNR, patient.getPersonId());
        assertEquals(INTEGR_FNAMN, patient.getFornamn());
        assertEquals(INTEGR_MNAMN, patient.getMellannamn());
        assertEquals(INTEGR_LNAMN, patient.getEfternamn());
        assertEquals(INTEGR_POST_ADDR, patient.getPostadress());
        assertEquals(INTEGR_POST_NR, patient.getPostnummer());
        assertEquals(INTEGR_POST_ORT, patient.getPostort());
        assertEquals(INTEGR_AVLIDEN, patient.isAvliden());
        assertEquals(false, patient.isSekretessmarkering());
    }

    /**
     * Dödsbevis + fristående + PU == Allt från PU
     */
    @Test
    public void testSOSDBIntygFristaendeWithPuOk() {
        when(puService.getPerson(any(Personnummer.class))).thenReturn(buildPersonSvar());
        when(webCertUserService.getUser()).thenReturn(freeWebCertUser);

        Patient patient = testee.resolvePatient(PNR, "db");
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
    public void testSOSDBIntygFristaendeWithPuUnavailable() {
        when(puService.getPerson(any(Personnummer.class))).thenReturn(buildErrorPersonSvar());
        when(webCertUserService.getUser()).thenReturn(freeWebCertUser);

        Patient patient = testee.resolvePatient(PNR, "db");
        assertNull(patient);
    }

    // - Dödsorsaksintyg - //

    /**
     * DOI - Integration. DB finns, PU finns. Namn och adress från DB-intyget, avliden/sekr från PU.
     */
    @Test
    public void testSosDoiIntygIntegrationWithExistingDBIntygAndPuOk() throws ModuleNotFoundException, IOException {
        when(puService.getPerson(any(Personnummer.class))).thenReturn(buildPersonSvar());
        when(webCertUserService.getUser()).thenReturn(integratedWebCertUser);
        when(integratedWebCertUser.getValdVardgivare()).thenReturn(new Vardgivare("vg-1", "vardgivare-1"));


        List<Utkast> drafts = buildSosDBDrafts();
        when(utkastRepository.findDraftsByPatientAndVardgivareAndStatus(anyString(), anyString(), anyList(),
                anySet())).thenReturn(drafts);

        ModuleApi moduleApi = mock(ModuleApi.class);
        when(moduleApi.getUtlatandeFromJson(anyString())).thenReturn(buildSosDoiUtlatande());
        when(moduleRegistry.getModuleApi("db")).thenReturn(moduleApi);


        Patient patient = testee.resolvePatient(PNR, "doi");
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
     * DOI - Integration. DB finns, PU saknas. Namn och adress från DB-intyget, avliden från integrationsparam.
     */
    @Test
    public void testSosDoiIntygIntegrationWithExistingDBIntygAndPuUnavailable() throws ModuleNotFoundException, IOException {
        when(puService.getPerson(any(Personnummer.class))).thenReturn(buildErrorPersonSvar());
        when(webCertUserService.getUser()).thenReturn(integratedWebCertUser);
        when(integratedWebCertUser.getValdVardgivare()).thenReturn(new Vardgivare("vg-1", "vardgivare-1"));


        List<Utkast> drafts = buildSosDBDrafts();
        when(utkastRepository.findDraftsByPatientAndVardgivareAndStatus(anyString(), anyString(), anyList(),
                anySet())).thenReturn(drafts);

        ModuleApi moduleApi = mock(ModuleApi.class);
        when(moduleApi.getUtlatandeFromJson(anyString())).thenReturn(buildSosDoiUtlatande());
        when(moduleRegistry.getModuleApi("db")).thenReturn(moduleApi);


        Patient patient = testee.resolvePatient(PNR, "doi");
        assertEquals(PNR.getPersonnummer(), patient.getPersonId().getPersonnummer());
        assertEquals(DB_FNAMN, patient.getFornamn());
        assertEquals(DB_MNAMN, patient.getMellannamn());
        assertEquals(DB_ENAMN, patient.getEfternamn());
        assertEquals(DB_POST_ADDR, patient.getPostadress());
        assertEquals(DB_POST_NR, patient.getPostnummer());
        assertEquals(DB_POST_ORT, patient.getPostort());
        assertEquals(INTEGR_AVLIDEN, patient.isAvliden());
        assertEquals(false, patient.isSekretessmarkering());
    }


    /**
     * DOI - Integration. DB saknas, PU finns. Namn och adress från PU.
     */
    @Test
    public void testSosDoiIntygIntegrationWithNoDBIntygAndPuOk() throws ModuleNotFoundException, IOException {
        when(puService.getPerson(any(Personnummer.class))).thenReturn(buildPersonSvar());
        when(webCertUserService.getUser()).thenReturn(integratedWebCertUser);
        when(integratedWebCertUser.getValdVardgivare()).thenReturn(new Vardgivare("vg-1", "vardgivare-1"));


        List<Utkast> drafts = new ArrayList<>();
        when(utkastRepository.findDraftsByPatientAndVardgivareAndStatus(anyString(), anyString(), anyList(),
                anySet())).thenReturn(drafts);


        Patient patient = testee.resolvePatient(PNR, "doi");
        assertEquals(PNR.getPersonnummer(), patient.getPersonId().getPersonnummer());
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
     * DOI - Integration. DB saknas, PU saknas. Rubbet från Integration
     */
    @Test
    public void testSosDoiIntygIntegrationWithNoDBIntygAndPuUnavailable() throws ModuleNotFoundException, IOException {
        when(puService.getPerson(any(Personnummer.class))).thenReturn(buildErrorPersonSvar());
        when(webCertUserService.getUser()).thenReturn(integratedWebCertUser);
        when(integratedWebCertUser.getValdVardgivare()).thenReturn(new Vardgivare("vg-1", "vardgivare-1"));


        List<Utkast> drafts = new ArrayList<>();
        when(utkastRepository.findDraftsByPatientAndVardgivareAndStatus(anyString(), anyString(), anyList(),
                anySet())).thenReturn(drafts);


        Patient patient = testee.resolvePatient(PNR, "doi");
        assertEquals(PNR.getPersonnummer(), patient.getPersonId().getPersonnummer());
        assertEquals(INTEGR_FNAMN, patient.getFornamn());
        assertEquals(INTEGR_MNAMN, patient.getMellannamn());
        assertEquals(INTEGR_LNAMN, patient.getEfternamn());
        assertEquals(INTEGR_POST_ADDR, patient.getPostadress());
        assertEquals(INTEGR_POST_NR, patient.getPostnummer());
        assertEquals(INTEGR_POST_ORT, patient.getPostort());
        assertEquals(INTEGR_AVLIDEN, patient.isAvliden());
        assertEquals(false, patient.isSekretessmarkering());
    }


    /**
     * DOI - Fristående. DB finns, PU saknas. Namn och adress från DB-intyget. Avliden vet vi egentligen inte...
     */
    @Test
    public void testSosDoiIntygFristaendeWithExistingDBIntygAndPuUnavailable() throws ModuleNotFoundException, IOException {
        when(puService.getPerson(any(Personnummer.class))).thenReturn(buildErrorPersonSvar());
        when(webCertUserService.getUser()).thenReturn(freeWebCertUser);
        when(freeWebCertUser.getValdVardenhet()).thenReturn(buildVardenhet());


        List<Utkast> drafts = buildSosDBDrafts();
        when(utkastRepository.findDraftsByPatientAndEnhetAndStatus(anyString(), anyList(), anyList(),
                anySet())).thenReturn(drafts);

        ModuleApi moduleApi = mock(ModuleApi.class);
        when(moduleApi.getUtlatandeFromJson(anyString())).thenReturn(buildSosDoiUtlatande());
        when(moduleRegistry.getModuleApi("db")).thenReturn(moduleApi);


        Patient patient = testee.resolvePatient(PNR, "doi");
        assertEquals(PNR.getPersonnummer(), patient.getPersonId().getPersonnummer());
        assertEquals(DB_FNAMN, patient.getFornamn());
        assertEquals(DB_MNAMN, patient.getMellannamn());
        assertEquals(DB_ENAMN, patient.getEfternamn());
        assertEquals(DB_POST_ADDR, patient.getPostadress());
        assertEquals(DB_POST_NR, patient.getPostnummer());
        assertEquals(DB_POST_ORT, patient.getPostort());

        // Vi har ingen möjlighet att känna till avliden i det här fallet, men eftersom det är ett DB/DOI så kan vi utgå
        // från att patienten är avliden.
        assertEquals(true, patient.isAvliden());
        assertEquals(false, patient.isSekretessmarkering());
    }


    /**
     * DOI - Fristående. DB saknas, PU finns. Namn och adress från PU.
     */
    @Test
    public void testSosDoiIntygFristaendeWithNoDBIntygAndPuOk() throws ModuleNotFoundException, IOException {
        when(puService.getPerson(any(Personnummer.class))).thenReturn(buildPersonSvar());
        when(webCertUserService.getUser()).thenReturn(freeWebCertUser);
        when(freeWebCertUser.getValdVardenhet()).thenReturn(buildVardenhet());


        List<Utkast> drafts = new ArrayList<>();
        when(utkastRepository.findDraftsByPatientAndVardgivareAndStatus(anyString(), anyString(), anyList(),
                anySet())).thenReturn(drafts);


        Patient patient = testee.resolvePatient(PNR, "doi");
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

    @NotNull
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
        when(freeWebCertUser.getValdVardenhet()).thenReturn(buildVardenhet());

        List<Utkast> drafts = new ArrayList<>();
        when(utkastRepository.findDraftsByPatientAndEnhetAndStatus(anyString(), anyList(), anyList(),
                anySet())).thenReturn(drafts);

        Patient patient = testee.resolvePatient(PNR, "doi");
        assertNull(patient);
    }


    private Utlatande buildSosDoiUtlatande() {
        GrundData grundData = new GrundData();
        grundData.setPatient(buildPatient());
        return DbUtlatande.builder()
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
        return patient;
    }

    private List<Utkast> buildSosDBDrafts() {
        Utkast u1 = mock(Utkast.class);
        when(u1.getSenastSparadDatum()).thenReturn(LocalDateTime.now().minusDays(1));
        when(u1.getModel()).thenReturn("the model");
        ArrayList<Utkast> utkastList = new ArrayList<>();
        utkastList.add(u1);
        return utkastList;
    }

    private PersonSvar buildPersonSvar() {
        Person person = buildPerson();
        return new PersonSvar(person, PersonSvar.Status.FOUND);
    }

    private PersonSvar buildErrorPersonSvar() {
        return new PersonSvar(null, PersonSvar.Status.ERROR);
    }

    private Person buildPerson() {
        return new Person(Personnummer.createValidatedPersonnummerWithDash("19121212-1212").get(),
                false, false, FNAMN, MNAMN, LNAMN, POST_ADDR, POST_NR, POST_ORT);
    }
}
