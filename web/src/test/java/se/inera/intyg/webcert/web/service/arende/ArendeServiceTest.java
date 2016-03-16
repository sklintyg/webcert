package se.inera.intyg.webcert.web.service.arende;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.*;
import java.util.stream.Collectors;

import org.joda.time.DateTimeUtils;
import org.joda.time.LocalDateTime;
import org.junit.*;
import org.junit.runner.RunWith;
import org.mockito.*;
import org.mockito.runners.MockitoJUnitRunner;

import se.inera.intyg.common.integration.hsa.model.Vardenhet;
import se.inera.intyg.common.integration.hsa.model.Vardgivare;
import se.inera.intyg.common.integration.hsa.services.HsaEmployeeService;
import se.inera.intyg.common.support.modules.support.api.dto.Personnummer;
import se.inera.intyg.webcert.common.service.exception.WebCertServiceErrorCodeEnum;
import se.inera.intyg.webcert.common.service.exception.WebCertServiceException;
import se.inera.intyg.webcert.persistence.arende.model.*;
import se.inera.intyg.webcert.persistence.arende.repository.ArendeRepository;
import se.inera.intyg.webcert.persistence.model.Status;
import se.inera.intyg.webcert.persistence.utkast.model.Signatur;
import se.inera.intyg.webcert.persistence.utkast.model.Utkast;
import se.inera.intyg.webcert.persistence.utkast.repository.UtkastRepository;
import se.inera.intyg.webcert.web.auth.authorities.*;
import se.inera.intyg.webcert.web.auth.bootstrap.AuthoritiesConfigurationTestSetup;
import se.inera.intyg.webcert.web.converter.util.TransportToArende;
import se.inera.intyg.webcert.web.service.dto.Lakare;
import se.inera.intyg.webcert.web.service.monitoring.MonitoringLogService;
import se.inera.intyg.webcert.web.service.user.WebCertUserService;
import se.inera.intyg.webcert.web.service.user.dto.WebCertUser;
import se.riv.infrastructure.directory.employee.getemployeeincludingprotectedpersonresponder.v1.GetEmployeeIncludingProtectedPersonResponseType;
import se.riv.infrastructure.directory.v1.PersonInformationType;

@RunWith(MockitoJUnitRunner.class)
public class ArendeServiceTest extends AuthoritiesConfigurationTestSetup {

    private static final long FIXED_TIME_MILLIS = 1456329300599L;
    private static final LocalDateTime JANUARY = new LocalDateTime("2013-01-12T11:22:11");
    private static final LocalDateTime DECEMBER_YEAR_9999 = new LocalDateTime("9999-12-11T10:22:00");
    private static final Personnummer PATIENT_ID = new Personnummer("19121212-1212");

    @Mock
    private ArendeRepository repo;

    @Mock
    private UtkastRepository utkastRepository;

    @Mock
    private WebCertUserService webcertUserService;

    @Mock
    private MonitoringLogService monitoringLog;

    @Mock
    private TransportToArende transportToArende;

    @Mock
    private HsaEmployeeService hsaEmployeeService;

    @InjectMocks
    private ArendeServiceImpl service;

    @Before
    public void setUp() {
        DateTimeUtils.setCurrentMillisFixed(FIXED_TIME_MILLIS);
    }

    @After
    public void cleanUp() {
        DateTimeUtils.setCurrentMillisSystem();
    }

    @Test
    public void testProcessIncomingMessage() throws WebCertServiceException {
        final String intygId = "intygsid";
        final String intygTyp = "intygTyp";
        final String signeratAv = "signeratAv";
        final String enhet = "enhet";
        final String givenName = "Test";
        final String surname = "Testorsson Svensson";

        Arende arende = new Arende();
        arende.setIntygsId(intygId);
        when(repo.save(any(Arende.class))).thenReturn(arende);
        Utkast utkast = new Utkast();
        utkast.setIntygsTyp(intygTyp);
        utkast.setEnhetsId(enhet);
        utkast.setSignatur(mock(Signatur.class));
        when(utkast.getSignatur().getSigneradAv()).thenReturn(signeratAv);
        when(utkastRepository.findOne(intygId)).thenReturn(utkast);
        when(hsaEmployeeService.getEmployee(eq(signeratAv), eq(null))).thenReturn(createHsaResponse(givenName, surname));

        Arende res = service.processIncomingMessage(arende);

        assertNotNull(res);
        assertEquals(FIXED_TIME_MILLIS, res.getTimestamp().toDateTime().getMillis());
        assertEquals(FIXED_TIME_MILLIS, res.getSenasteHandelse().toDateTime().getMillis());
        assertEquals(Boolean.FALSE, res.getVidarebefordrad());
        assertEquals(Status.PENDING_INTERNAL_ACTION, res.getStatus());
        assertEquals(intygTyp, res.getIntygTyp());
        assertEquals(signeratAv, res.getSigneratAv());
        assertEquals(enhet, res.getEnhet());
        assertEquals("Test Testorsson Svensson", res.getSigneratAvName());

        verify(utkastRepository).findOne(intygId);
    }


    @Test
    public void testProcessIncomingMessageNoGivenName() throws WebCertServiceException {
        final String intygId = "intygsid";
        final String intygTyp = "intygTyp";
        final String signeratAv = "signeratAv";
        final String enhet = "enhet";
        final String givenName = null;
        final String surname = "Testorsson Svensson";

        Arende arende = new Arende();
        arende.setIntygsId(intygId);
        when(repo.save(any(Arende.class))).thenReturn(arende);
        Utkast utkast = new Utkast();
        utkast.setIntygsTyp(intygTyp);
        utkast.setEnhetsId(enhet);
        utkast.setSignatur(mock(Signatur.class));
        when(utkast.getSignatur().getSigneradAv()).thenReturn(signeratAv);
        when(utkastRepository.findOne(intygId)).thenReturn(utkast);
        when(hsaEmployeeService.getEmployee(eq(signeratAv), eq(null))).thenReturn(createHsaResponse(givenName, surname));

        Arende res = service.processIncomingMessage(arende);

        assertNotNull(res);
        assertEquals("Testorsson Svensson", res.getSigneratAvName());
    }

    @Test
    public void testProcessIncomingMessageUpdatingRelatedSvar() throws WebCertServiceException {
        final String intygId = "intygid";
        final String frageid = "frageid";
        final String signeratAv = "signeratAv";

        Arende fragearende = new Arende();

        Arende svararende = new Arende();
        svararende.setIntygsId(intygId);
        svararende.setSvarPaId(frageid);

        Utkast utkast = new Utkast();
        utkast.setIntygsTyp("intygstyp");
        utkast.setEnhetsId("enhet");
        utkast.setSignatur(mock(Signatur.class));

        when(utkast.getSignatur().getSigneradAv()).thenReturn(signeratAv);
        when(utkastRepository.findOne(intygId)).thenReturn(utkast);
        when(hsaEmployeeService.getEmployee(eq(signeratAv), eq(null))).thenReturn(createHsaResponse("sune", "svensson"));
        when(repo.save(any(Arende.class))).thenReturn(svararende);
        when(repo.findOneByMeddelandeId(eq(frageid))).thenReturn(fragearende);

        Arende res = service.processIncomingMessage(svararende);
        assertEquals(Status.ANSWERED, res.getStatus());
        assertEquals(Status.ANSWERED, fragearende.getStatus());
        assertEquals(FIXED_TIME_MILLIS, res.getSenasteHandelse().toDateTime().getMillis());
        assertEquals(FIXED_TIME_MILLIS, fragearende.getSenasteHandelse().toDateTime().getMillis());

        verify(repo).findOneByMeddelandeId(eq(frageid));
    }

    @Test
    public void testProcessIncomingMessageUpdatingRelatedPaminnelse() throws WebCertServiceException {
        final String intygId = "intygid";
        final String paminnelseid = "paminnelseid";
        final String signeratAv = "signeratAv";

        Arende paminnelse = new Arende();

        Arende svararende = new Arende();
        svararende.setIntygsId(intygId);
        svararende.setPaminnelseMeddelandeId(paminnelseid);

        Utkast utkast = new Utkast();
        utkast.setIntygsTyp("intygstyp");
        utkast.setEnhetsId("enhet");
        utkast.setSignatur(mock(Signatur.class));

        when(utkast.getSignatur().getSigneradAv()).thenReturn(signeratAv);
        when(utkastRepository.findOne(intygId)).thenReturn(utkast);
        when(hsaEmployeeService.getEmployee(eq(signeratAv), eq(null))).thenReturn(createHsaResponse("sune", "svensson"));
        when(repo.save(any(Arende.class))).thenReturn(svararende);
        when(repo.findOneByMeddelandeId(eq(paminnelseid))).thenReturn(paminnelse);

        Arende res = service.processIncomingMessage(svararende);
        assertEquals(FIXED_TIME_MILLIS, res.getSenasteHandelse().toDateTime().getMillis());
        assertEquals(FIXED_TIME_MILLIS, paminnelse.getSenasteHandelse().toDateTime().getMillis());

        verify(repo).findOneByMeddelandeId(eq(paminnelseid));
    }

    @Test
    public void testProcessIncomingMessageHsaNotResponding() {
        Utkast utkast = new Utkast();
        utkast.setIntygsTyp("intygstyp");
        utkast.setEnhetsId("enhetsid");
        utkast.setSignatur(mock(Signatur.class));
        when(utkast.getSignatur().getSigneradAv()).thenReturn("signeratav");
        when(utkastRepository.findOne(anyString())).thenReturn(utkast);
        when(hsaEmployeeService.getEmployee(anyString(), eq(null))).thenReturn(null);
        try {
            service.processIncomingMessage(new Arende());
            fail("Should throw");
        } catch (WebCertServiceException e) {
            assertEquals(WebCertServiceErrorCodeEnum.EXTERNAL_SYSTEM_PROBLEM, e.getErrorCode());
        }
    }

    @Test
    public void testProcessIncomingMessageHsaNotGivingName() {
        Utkast utkast = new Utkast();
        utkast.setIntygsTyp("intygstyp");
        utkast.setEnhetsId("enhetsid");
        utkast.setSignatur(mock(Signatur.class));
        when(utkast.getSignatur().getSigneradAv()).thenReturn("signeratav");
        when(utkastRepository.findOne(anyString())).thenReturn(utkast);
        when(hsaEmployeeService.getEmployee(anyString(), eq(null))).thenReturn(createHsaResponse(null, null));
        try {
            service.processIncomingMessage(new Arende());
            fail("Should throw");
        } catch (WebCertServiceException e) {
            assertEquals(WebCertServiceErrorCodeEnum.DATA_NOT_FOUND, e.getErrorCode());
        }
    }

    @Test
    public void testProcessIncomingMessageCertificateNotFound() {
        when(utkastRepository.findOne(anyString())).thenReturn(null);
        try {
            service.processIncomingMessage(new Arende());
            fail("Should throw");
        } catch (WebCertServiceException e) {
            assertEquals(WebCertServiceErrorCodeEnum.DATA_NOT_FOUND, e.getErrorCode());
        }
    }

    @Test
    public void testProcessIncomingMessageCertificateNotSigned() {
        when(utkastRepository.findOne(anyString())).thenReturn(new Utkast());
        try {
            service.processIncomingMessage(new Arende());
            fail("Should throw");
        } catch (WebCertServiceException e) {
            assertEquals(WebCertServiceErrorCodeEnum.INVALID_STATE, e.getErrorCode());
        }
    }

    @Test
    public void testListSignedByForUnits() {
        final List<String> selectedUnits = Arrays.asList("enhet1", "enhet2");
        final String[] lakare1 = {"hsaid1", "namn1"};
        final String[] lakare2 = {"hsaid2", "namn2"};
        final String[] lakare3 = {"hsaid3", "namn3"};
        final List<Object[]> repoResult = Arrays.asList(lakare1, lakare2, lakare3);

        WebCertUser user = Mockito.mock(WebCertUser.class);
        when(user.getIdsOfSelectedVardenhet()).thenReturn(selectedUnits);
        when(webcertUserService.getUser()).thenReturn(user);
        when(repo.findSigneratAvByEnhet(selectedUnits)).thenReturn(repoResult);

        List<Lakare> res = service.listSignedByForUnits();

        assertEquals(repoResult.stream().map(arr -> new Lakare((String) arr[0], (String) arr[1])).collect(Collectors.toList()), res);

        verify(webcertUserService).getUser();
        verify(repo).findSigneratAvByEnhet(selectedUnits);
    }

    @Test
    public void testListArendeForUnits() {
        final List<String> selectedUnits = Arrays.asList("enhet1", "enhet2");
        final List<Arende> repoResult = Arrays.asList(new Arende());
        WebCertUser user = Mockito.mock(WebCertUser.class);
        when(user.getIdsOfSelectedVardenhet()).thenReturn(selectedUnits);
        when(webcertUserService.getUser()).thenReturn(user);
        when(repo.findByEnhet(selectedUnits)).thenReturn(repoResult);
        List<Arende> res = service.listArendeForUnits();
        assertEquals(repoResult, res);
    }

    @Test
    public void testGetArendeForIntyg() {
        List<Arende> arendeList = new ArrayList<>();
        arendeList.add(buildArende(1L, DECEMBER_YEAR_9999, DECEMBER_YEAR_9999));
        arendeList.add(buildArende(2L, new LocalDateTime(), new LocalDateTime()));
        arendeList.add(buildArende(3L, JANUARY, JANUARY));

        when(repo.findByIntygsId("intyg-1")).thenReturn(new ArrayList<>(arendeList));
        when(transportToArende.decorate(arendeList.get(0))).thenReturn(arendeList.get(0));
        when(transportToArende.decorate(arendeList.get(1))).thenReturn(arendeList.get(1));
        when(transportToArende.decorate(arendeList.get(2))).thenReturn(arendeList.get(2));

        when(webcertUserService.getUser()).thenReturn(createUser());

        List<Arende> result = service.getArende("intyg-1");

        verify(repo).findByIntygsId("intyg-1");
        verify(webcertUserService).getUser();

        assertEquals(3, result.size());
        assertEquals(arendeList, result);
    }

    private Arende buildArende(Long id, LocalDateTime fragaSkickadDatum, LocalDateTime svarSkickadDatum) {
        Arende arende = new Arende();
        arende.setStatus(Status.PENDING_INTERNAL_ACTION);
        arende.setAmne(ArendeAmne.OVRIGT);
        arende.setReferensId("<fk-extern-referens>");
        arende.setId(id);
        arende.setEnhet("enhet");
        arende.setSkickatTidpunkt(fragaSkickadDatum);
        arende.setMeddelande("frageText");
        arende.setTimestamp(svarSkickadDatum);
        List<MedicinsktArende> komplettering = new ArrayList<MedicinsktArende>();
        arende.setIntygsId("<intygsId>");
        arende.setPatientPersonId(PATIENT_ID.getPersonnummer());
        arende.setKomplettering(komplettering);
        return arende;
    }

    private WebCertUser createUser() {
        Role role = AUTHORITIES_RESOLVER.getRole(AuthoritiesConstants.ROLE_LAKARE);

        WebCertUser user = new WebCertUser();
        user.setRoles(AuthoritiesResolverUtil.toMap(role));
        user.setAuthorities(AuthoritiesResolverUtil.toMap(role.getPrivileges()));
        user.setHsaId("testuser");
        user.setNamn("test userman");

        Vardenhet vardenhet = new Vardenhet("enhet", "Enhet");

        Vardgivare vardgivare = new Vardgivare("vardgivare", "Vardgivaren");
        vardgivare.getVardenheter().add(vardenhet);

        user.setVardgivare(Collections.singletonList(vardgivare));
        user.setValdVardenhet(vardenhet);

        return user;
    }

    private GetEmployeeIncludingProtectedPersonResponseType createHsaResponse(String givenName, String middleAndSurname) {
        GetEmployeeIncludingProtectedPersonResponseType res = new GetEmployeeIncludingProtectedPersonResponseType();
        PersonInformationType pit = new PersonInformationType();
        pit.setGivenName(givenName);
        pit.setMiddleAndSurName(middleAndSurname);
        res.getPersonInformation().add(new PersonInformationType());
        res.getPersonInformation().add(pit);
        res.getPersonInformation().add(new PersonInformationType());
        res.getPersonInformation().add(new PersonInformationType());
        return res;
    }

}
