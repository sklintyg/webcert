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
import se.inera.intyg.webcert.persistence.model.Filter;
import se.inera.intyg.webcert.persistence.model.Status;
import se.inera.intyg.webcert.persistence.utkast.model.Signatur;
import se.inera.intyg.webcert.persistence.utkast.model.Utkast;
import se.inera.intyg.webcert.persistence.utkast.repository.UtkastRepository;
import se.inera.intyg.webcert.web.auth.authorities.*;
import se.inera.intyg.webcert.web.auth.bootstrap.AuthoritiesConfigurationTestSetup;
import se.inera.intyg.webcert.web.converter.util.TransportToArende;
import se.inera.intyg.webcert.web.service.dto.Lakare;
import se.inera.intyg.webcert.web.service.fragasvar.FragaSvarService;
import se.inera.intyg.webcert.web.service.fragasvar.dto.QueryFragaSvarParameter;
import se.inera.intyg.webcert.web.service.fragasvar.dto.QueryFragaSvarResponse;
import se.inera.intyg.webcert.web.service.monitoring.MonitoringLogService;
import se.inera.intyg.webcert.web.service.user.WebCertUserService;
import se.inera.intyg.webcert.web.service.user.dto.WebCertUser;
import se.inera.intyg.webcert.web.web.controller.api.dto.ArendeMetaData;
import se.riv.infrastructure.directory.employee.getemployeeincludingprotectedpersonresponder.v1.GetEmployeeIncludingProtectedPersonResponseType;
import se.riv.infrastructure.directory.v1.PersonInformationType;
import se.inera.intyg.webcert.web.web.controller.api.dto.ArendeConversationView;
import se.inera.intyg.webcert.web.web.controller.api.dto.ArendeView;
import se.inera.intyg.webcert.web.web.controller.api.dto.ArendeView.ArendeType;

@RunWith(MockitoJUnitRunner.class)
public class ArendeServiceTest extends AuthoritiesConfigurationTestSetup {

    private static final long FIXED_TIME_MILLIS = 1456329300599L;
    private static final LocalDateTime JANUARY = new LocalDateTime("2013-01-12T11:22:11");
    private static final LocalDateTime FEBRUARY = new LocalDateTime("2013-02-12T11:22:11");
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

    @Mock
    private FragaSvarService fragaSvarService;

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
        final String[] lakare4 = {"hsaid4", "namn4"};
        final List<Object[]> repoResult = Arrays.asList(lakare1, lakare2, lakare3);
        final List<Object[]> expected = Arrays.asList(lakare1, lakare2, lakare3, lakare4);

        WebCertUser user = Mockito.mock(WebCertUser.class);
        when(user.getIdsOfSelectedVardenhet()).thenReturn(selectedUnits);
        when(webcertUserService.getUser()).thenReturn(user);
        when(repo.findSigneratAvByEnhet(selectedUnits)).thenReturn(repoResult);
        when(fragaSvarService.getFragaSvarHsaIdByEnhet(eq(null))).thenReturn(Arrays.asList(new Lakare(lakare4[0],lakare4[1])));

        List<Lakare> res = service.listSignedByForUnits(null);

        assertEquals(expected.stream().map(arr -> new Lakare((String) arr[0], (String) arr[1])).collect(Collectors.toList()), res);

        verify(webcertUserService).getUser();
        verify(repo).findSigneratAvByEnhet(selectedUnits);
    }

    @Test
    public void testListSignedByForUnitsSpecifiedUnit() {
        final List<String> selectedUnit = Arrays.asList("enhet1");
        final String[] lakare1 = {"hsaid1", "namn1"};
        final String[] lakare2 = {"hsaid2", "namn2"};
        final String[] lakare3 = {"hsaid3", "namn3"};
        final List<Object[]> repoResult = Arrays.asList(lakare1, lakare2, lakare3);

        when(webcertUserService.isAuthorizedForUnit(anyString(), eq(true))).thenReturn(true);
        when(repo.findSigneratAvByEnhet(selectedUnit)).thenReturn(repoResult);
        when(fragaSvarService.getFragaSvarHsaIdByEnhet(eq(null))).thenReturn(new ArrayList<>());

        List<Lakare> res = service.listSignedByForUnits(selectedUnit.get(0));

        assertEquals(repoResult.stream().map(arr -> new Lakare((String) arr[0], (String) arr[1])).collect(Collectors.toList()), res);

        verify(repo).findSigneratAvByEnhet(selectedUnit);
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
        List<ArendeView> arendeViewList = new ArrayList<>();
        
       arendeList.add(buildArende(1L, FEBRUARY, FEBRUARY));
        arendeList.add(buildArende(2L, JANUARY, JANUARY));
        arendeList.add(buildArende(3L, DECEMBER_YEAR_9999, DECEMBER_YEAR_9999));
        arendeList.add(buildArende(4L, FEBRUARY, FEBRUARY));
        arendeViewList.add(buildArendeView(arendeList.get(0), arendeList.get(0).getMeddelandeId(), null, null, FEBRUARY)); //fraga
        arendeViewList.add(buildArendeView(arendeList.get(1), "meddelandeId2", arendeList.get(0).getMeddelandeId(), null, JANUARY)); //svar
        arendeViewList.add(buildArendeView(arendeList.get(2), "meddelandeId3", null, arendeList.get(0).getMeddelandeId(), DECEMBER_YEAR_9999)); //paminnelse
        arendeViewList.add(buildArendeView(arendeList.get(3), "meddelandeId4", null, null, FEBRUARY)); //fraga

        when(repo.findByIntygsId("intyg-1")).thenReturn(arendeList);
        when(transportToArende.convert(arendeList.get(0))).thenReturn(arendeViewList.get(0));
        when(transportToArende.convert(arendeList.get(1))).thenReturn(arendeViewList.get(1));
        when(transportToArende.convert(arendeList.get(2))).thenReturn(arendeViewList.get(2));
        when(transportToArende.convert(arendeList.get(3))).thenReturn(arendeViewList.get(3));

        when(webcertUserService.getUser()).thenReturn(createUser());

        List<ArendeConversationView> result = service.getArenden("intyg-1");

        verify(repo).findByIntygsId("intyg-1");
        verify(webcertUserService).getUser();

        assertEquals(2, result.size());
        assertEquals(1, result.get(0).getPaminnelser().size());
        assertEquals(arendeViewList.get(0), result.get(0).getFraga());
        assertEquals(arendeViewList.get(1), result.get(0).getSvar());
        assertEquals(arendeViewList.get(2), result.get(0).getPaminnelser().get(0));
        assertEquals(arendeViewList.get(3), result.get(1).getFraga());
        assertEquals(DECEMBER_YEAR_9999, result.get(0).getSenasteHandelse());
        assertEquals(FEBRUARY, result.get(1).getSenasteHandelse());
    }

    private ArendeView buildArendeView(Arende arende, String meddelandeId, String svarPaId, String paminnelseMeddelandeId, LocalDateTime timestamp) {
        ArendeType arendeType = null;
        if(paminnelseMeddelandeId !=null){
            arendeType = ArendeType.PAMINNELSE;
        }else if(svarPaId !=null){
            arendeType = ArendeType.SVAR;
        }else{
            arendeType = ArendeType.FRAGA;
        }
        ArendeView view = ArendeView.builder().
                setAmne(ArendeAmne.ARBTID).
                setArendeType(arendeType).
                setInternReferens(meddelandeId).
                setIntygId("intyg-1").
                setStatus(Status.PENDING_INTERNAL_ACTION).
                setMeddelandeRubrik("rubrik").
                setSvarPaId(svarPaId).
                setTimestamp(timestamp).
                setVidarebefordrad(false).
                setPaminnelseMeddelandeId(paminnelseMeddelandeId).
                setSvarPaId(svarPaId).
                
                build();
        
        return view;
    }

    @Test(expected = WebCertServiceException.class)
    public void testFilterArendeWithAuthFail() {
        WebCertUser webCertUser = createUser();
        when(webcertUserService.getUser()).thenReturn(webCertUser);

        QueryFragaSvarParameter params = new QueryFragaSvarParameter();
        params.setEnhetId("no-auth");

        service.filterArende(params);
    }

    @Test
    public void testFilterArendeWithEnhetsIdAsParam() {

        WebCertUser webCertUser = createUser();
        when(webcertUserService.isAuthorizedForUnit(any(String.class), eq(true))).thenReturn(true);

        List<Arende> queryResults = new ArrayList<>();
        queryResults.add(buildArende(1L, LocalDateTime.now(), null));
        queryResults.add(buildArende(2L, LocalDateTime.now().minusDays(1), null));

        when(repo.filterArende(any(Filter.class))).thenReturn(queryResults);
        when(repo.filterArendeCount(any(Filter.class))).thenReturn(queryResults.size() + 1);

        QueryFragaSvarResponse fsResponse = new QueryFragaSvarResponse();
        fsResponse.setResults(new ArrayList<>());
        fsResponse.setTotalCount(0);

        when(fragaSvarService.filterFragaSvar(any(Filter.class))).thenReturn(fsResponse);

        QueryFragaSvarParameter params = new QueryFragaSvarParameter();
        params.setEnhetId(webCertUser.getValdVardenhet().getId());

        QueryFragaSvarResponse response = service.filterArende(params);

        verify(webcertUserService).isAuthorizedForUnit(anyString(), eq(true));

        verify(repo).filterArende(any(Filter.class));
        verify(repo).filterArendeCount(any(Filter.class));
        verify(fragaSvarService).filterFragaSvar(any(Filter.class));

        assertEquals(2, response.getResults().size());
        assertEquals(3, response.getTotalCount());
    }

    @Test
    public void testFilterArendeWithNoEnhetsIdAsParam() {

        when(webcertUserService.getUser()).thenReturn(createUser());

        List<Arende> queryResults = new ArrayList<>();
        queryResults.add(buildArende(1L, LocalDateTime.now(), null));
        queryResults.add(buildArende(2L, LocalDateTime.now().plusDays(1), null));

        when(repo.filterArende(any(Filter.class))).thenReturn(queryResults);
        when(repo.filterArendeCount(any(Filter.class))).thenReturn(queryResults.size() + 1);

        QueryFragaSvarResponse fsResponse = new QueryFragaSvarResponse();
        fsResponse.setResults(new ArrayList<>());
        fsResponse.setTotalCount(0);

        when(fragaSvarService.filterFragaSvar(any(Filter.class))).thenReturn(fsResponse);

        QueryFragaSvarParameter params = new QueryFragaSvarParameter();

        QueryFragaSvarResponse response = service.filterArende(params);

        verify(webcertUserService).getUser();

        verify(repo).filterArende(any(Filter.class));
        verify(repo).filterArendeCount(any(Filter.class));
        verify(fragaSvarService).filterFragaSvar(any(Filter.class));

        assertEquals(2, response.getResults().size());
        assertEquals(3, response.getTotalCount());
    }

    @Test
    public void testFilterArendeMergesFragaSvar() {

        when(webcertUserService.getUser()).thenReturn(createUser());

        List<Arende> queryResults = new ArrayList<>();
        queryResults.add(buildArende(1L, LocalDateTime.now(), null));
        queryResults.add(buildArende(2L, LocalDateTime.now().plusDays(1), null));

        when(repo.filterArende(any(Filter.class))).thenReturn(queryResults);
        when(repo.filterArendeCount(any(Filter.class))).thenReturn(queryResults.size() + 1);

        QueryFragaSvarResponse fsResponse = new QueryFragaSvarResponse();
        fsResponse.setResults(new ArrayList<>());
        fsResponse.getResults().add(buildArendeListItem("intyg1", LocalDateTime.now().minusDays(1)));
        fsResponse.setTotalCount(1);

        when(fragaSvarService.filterFragaSvar(any(Filter.class))).thenReturn(fsResponse);

        QueryFragaSvarParameter params = new QueryFragaSvarParameter();

        QueryFragaSvarResponse response = service.filterArende(params);

        verify(webcertUserService).getUser();

        verify(repo).filterArende(any(Filter.class));
        verify(repo).filterArendeCount(any(Filter.class));
        verify(fragaSvarService).filterFragaSvar(any(Filter.class));

        assertEquals(3, response.getResults().size());
        assertEquals(4, response.getTotalCount());
    }

    @Test
    public void testFilterArendeInvalidStartPosition() {

        when(webcertUserService.getUser()).thenReturn(createUser());

        List<Arende> queryResults = new ArrayList<>();
        queryResults.add(buildArende(1L, LocalDateTime.now(), null));
        queryResults.add(buildArende(2L, LocalDateTime.now().plusDays(1), null));

        when(repo.filterArende(any(Filter.class))).thenReturn(queryResults);
        when(repo.filterArendeCount(any(Filter.class))).thenReturn(queryResults.size() + 1);

        QueryFragaSvarResponse fsResponse = new QueryFragaSvarResponse();
        fsResponse.setResults(new ArrayList<>());
        fsResponse.getResults().add(buildArendeListItem("intyg1", LocalDateTime.now().minusDays(1)));
        fsResponse.setTotalCount(1);

        when(fragaSvarService.filterFragaSvar(any(Filter.class))).thenReturn(fsResponse);

        QueryFragaSvarParameter params = new QueryFragaSvarParameter();
        params.setStartFrom(5);

        QueryFragaSvarResponse response = service.filterArende(params);

        verify(webcertUserService).getUser();

        verify(repo).filterArende(any(Filter.class));
        verify(repo).filterArendeCount(any(Filter.class));
        verify(fragaSvarService).filterFragaSvar(any(Filter.class));

        assertEquals(0, response.getResults().size());
        assertEquals(4, response.getTotalCount());
    }

    @Test
    public void testFilterArendeSelection() {

        when(webcertUserService.getUser()).thenReturn(createUser());

        List<Arende> queryResults = new ArrayList<>();
        queryResults.add(buildArende(1L, LocalDateTime.now(), null));
        queryResults.add(buildArende(2L, LocalDateTime.now().plusDays(1), null));

        when(repo.filterArende(any(Filter.class))).thenReturn(queryResults);
        when(repo.filterArendeCount(any(Filter.class))).thenReturn(queryResults.size() + 1);

        QueryFragaSvarResponse fsResponse = new QueryFragaSvarResponse();
        fsResponse.setResults(new ArrayList<>());
        fsResponse.getResults().add(buildArendeListItem("intyg1", LocalDateTime.now().minusDays(1)));
        fsResponse.setTotalCount(1);

        when(fragaSvarService.filterFragaSvar(any(Filter.class))).thenReturn(fsResponse);

        QueryFragaSvarParameter params = new QueryFragaSvarParameter();
        params.setStartFrom(2);
        params.setPageSize(10);

        QueryFragaSvarResponse response = service.filterArende(params);

        verify(webcertUserService).getUser();

        verify(repo).filterArende(any(Filter.class));
        verify(repo).filterArendeCount(any(Filter.class));
        verify(fragaSvarService).filterFragaSvar(any(Filter.class));

        assertEquals(1, response.getResults().size());
        assertEquals(4, response.getTotalCount());
    }

    @Test
    public void testFilterArendeSortsArendeListItemsByReceivedDate() {
        final String intygId1 = "intygid1";
        final String intygId2 = "intygid2";
        final String intygId3 = "intygid3";

        when(webcertUserService.getUser()).thenReturn(createUser());

        List<Arende> queryResults = new ArrayList<>();
        queryResults.add(buildArende(1L, intygId3, LocalDateTime.now().plusDays(2), null));
        queryResults.add(buildArende(2L, intygId2, LocalDateTime.now(), null));

        when(repo.filterArende(any(Filter.class))).thenReturn(queryResults);
        when(repo.filterArendeCount(any(Filter.class))).thenReturn(queryResults.size());

        QueryFragaSvarResponse fsResponse = new QueryFragaSvarResponse();
        fsResponse.setResults(new ArrayList<>());
        fsResponse.getResults().add(buildArendeListItem(intygId1, LocalDateTime.now().minusDays(1)));
        fsResponse.setTotalCount(1);

        when(fragaSvarService.filterFragaSvar(any(Filter.class))).thenReturn(fsResponse);

        QueryFragaSvarParameter params = new QueryFragaSvarParameter();

        QueryFragaSvarResponse response = service.filterArende(params);

        assertEquals(3, response.getResults().size());
        assertEquals(intygId1, response.getResults().get(0).getIntygId());
        assertEquals(intygId2, response.getResults().get(1).getIntygId());
        assertEquals(intygId3, response.getResults().get(2).getIntygId());
    }

    private Arende buildArende(Long id, LocalDateTime skickadTidpunkt, LocalDateTime timestamp) {
        return buildArende(id, "<intygsId>", skickadTidpunkt, timestamp);
    }

    private Arende buildArende(Long id, String intygId, LocalDateTime skickadTidpunkt, LocalDateTime timestamp) {
        Arende arende = new Arende();
        arende.setStatus(Status.PENDING_INTERNAL_ACTION);
        arende.setAmne(ArendeAmne.OVRIGT);
        arende.setReferensId("<fk-extern-referens>");
        arende.setMeddelandeId("meddelandeId");
        arende.setId(id);
        arende.setEnhet("enhet");
        arende.setSkickatTidpunkt(skickadTidpunkt);
        arende.setMeddelande("frageText");
        arende.setTimestamp(timestamp);
        List<MedicinsktArende> komplettering = new ArrayList<MedicinsktArende>();
        arende.setIntygsId(intygId);
        arende.setPatientPersonId(PATIENT_ID.getPersonnummer());
        arende.setKomplettering(komplettering);
        return arende;
    }

    private ArendeMetaData buildArendeListItem(String intygId, LocalDateTime receivedDate) {
        ArendeMetaData arende = new ArendeMetaData();
        arende.setIntygId(intygId);
        arende.setReceivedDate(receivedDate);

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
