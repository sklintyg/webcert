package se.inera.intyg.webcert.web.service.utkast;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.runners.MockitoJUnitRunner;
import org.mockito.stubbing.Answer;

import se.inera.intyg.common.support.modules.support.api.dto.Personnummer;
import se.inera.intyg.webcert.web.integration.registry.IntegreradeEnheterRegistry;
import se.inera.intyg.webcert.web.integration.registry.dto.IntegreradEnhetEntry;
import se.inera.intyg.webcert.persistence.utkast.model.Utkast;
import se.inera.intyg.webcert.persistence.utkast.model.VardpersonReferens;
import se.inera.intyg.webcert.persistence.utkast.repository.UtkastRepository;
import se.inera.intyg.webcert.integration.pu.model.Person;
import se.inera.intyg.webcert.integration.pu.model.PersonSvar;
import se.inera.intyg.webcert.integration.pu.services.PUService;
import se.inera.intyg.webcert.web.service.dto.HoSPerson;
import se.inera.intyg.webcert.web.service.dto.Vardenhet;
import se.inera.intyg.webcert.web.service.dto.Vardgivare;
import se.inera.intyg.webcert.web.service.log.LogService;
import se.inera.intyg.webcert.web.service.log.dto.LogRequest;
import se.inera.intyg.webcert.web.service.monitoring.MonitoringLogService;
import se.inera.intyg.webcert.web.service.notification.NotificationService;
import se.inera.intyg.webcert.web.service.user.WebCertUserService;
import se.inera.intyg.webcert.web.service.utkast.dto.CopyUtkastBuilderResponse;
import se.inera.intyg.webcert.web.service.utkast.dto.CreateNewDraftCopyRequest;
import se.inera.intyg.webcert.web.service.utkast.dto.CreateNewDraftCopyResponse;

@RunWith(MockitoJUnitRunner.class)
public class CopyUtkastServiceImplTest {

    private static final String INTYG_ID = "abc123";
    private static final String INTYG_COPY_ID = "def456";

    private static final String INTYG_JSON = "A bit of text representing json";

    private static final String INTYG_TYPE = "fk7263";

    private static final Personnummer PATIENT_SSN = new Personnummer("19121212-1212");
    private static final String PATIENT_FNAME = "Adam";
    private static final String PATIENT_MNAME = "Bertil";
    private static final String PATIENT_LNAME = "Caesarsson";

    private static final String VARDENHET_ID = "SE00001234-5678";
    private static final String VARDENHET_NAME = "Vårdenheten 1";

    private static final String VARDGIVARE_ID = "SE00001234-1234";
    private static final String VARDGIVARE_NAME = "Vårdgivaren 1";

    private static final String HOSPERSON_ID = "SE12345678-0001";
    private static final String HOSPERSON_NAME = "Dr Börje Dengroth";

    @Mock
    private UtkastRepository mockUtkastRepository;

    @Mock
    private PUService mockPUService;

    @Mock
    private CopyUtkastBuilder mockUtkastBuilder;

    @Mock
    private NotificationService mockNotificationService;

    @Mock
    private MonitoringLogService mockMonitoringService;

    @Mock
    private LogService logService;

    @Mock
    private WebCertUserService userService;

    @Mock
    private IntegreradeEnheterRegistry mockIntegreradeEnheterRegistry;

    @InjectMocks
    private CopyUtkastService copyService = new CopyUtkastServiceImpl();

    private HoSPerson hoSPerson;

    private Vardenhet vardenhet;

    @Before
    public void setup() {
        hoSPerson = new HoSPerson();
        hoSPerson.setHsaId(HOSPERSON_ID);
        hoSPerson.setNamn(HOSPERSON_NAME);

        Vardgivare vardgivare = new Vardgivare();
        vardgivare.setHsaId(VARDGIVARE_ID);
        vardgivare.setNamn(VARDGIVARE_NAME);

        vardenhet = new Vardenhet();
        vardenhet.setHsaId(VARDENHET_ID);
        vardenhet.setNamn(VARDENHET_NAME);
        vardenhet.setVardgivare(vardgivare);
    }

    @Before
    public void expectCallToPUService() throws Exception {
        PersonSvar personSvar = new PersonSvar(
                new Person(PATIENT_SSN, false, "Adam", "Bertilsson", "Cedergren", "Testgatan 12", "12345", "Testberga"),
                PersonSvar.Status.FOUND);
        when(mockPUService.getPerson(PATIENT_SSN)).thenReturn(personSvar);
    }

    @Before
    public void expectSaveOfUtkast() {
        when(mockUtkastRepository.save(any(Utkast.class))).thenAnswer(new Answer<Utkast>() {
            @Override
            public Utkast answer(InvocationOnMock invocation) throws Throwable {
                Object[] args = invocation.getArguments();
                return (Utkast) args[0];
            }
        });
    }

    @Test
    public void testCreateCopy() throws Exception {

        when(mockUtkastRepository.exists(INTYG_ID)).thenReturn(Boolean.FALSE);

        CopyUtkastBuilderResponse resp = createCopyUtkastBuilderResponse();
        when(mockUtkastBuilder.populateCopyUtkastFromSignedIntyg(any(CreateNewDraftCopyRequest.class), any(Person.class))).thenReturn(resp);

        CreateNewDraftCopyRequest copyReq = buildCopyRequest();

        CreateNewDraftCopyResponse copyResp = copyService.createCopy(copyReq);

        assertNotNull(copyResp);
        assertEquals(INTYG_COPY_ID, copyResp.getNewDraftIntygId());
        assertEquals(INTYG_TYPE, copyResp.getNewDraftIntygType());

        verify(mockPUService).getPerson(PATIENT_SSN);
        verify(mockUtkastBuilder).populateCopyUtkastFromSignedIntyg(any(CreateNewDraftCopyRequest.class), any(Person.class));
        verify(mockUtkastRepository).save(any(Utkast.class));

        verify(mockNotificationService).sendNotificationForDraftCreated(any(Utkast.class));

        // Assert pdl log
        verify(logService).logCreateIntyg(any(LogRequest.class));

    }

    @Test
    public void testCreateCopyWhenIntegrated() throws Exception {

        when(mockUtkastRepository.exists(INTYG_ID)).thenReturn(Boolean.TRUE);

        CopyUtkastBuilderResponse resp = createCopyUtkastBuilderResponse();
        when(mockUtkastBuilder.populateCopyUtkastFromOrignalUtkast(any(CreateNewDraftCopyRequest.class), any(Person.class))).thenReturn(resp);

        CreateNewDraftCopyRequest copyReq = buildCopyRequest();
        copyReq.setDjupintegrerad(true);

        CreateNewDraftCopyResponse copyResp = copyService.createCopy(copyReq);

        assertNotNull(copyResp);
        assertEquals(INTYG_COPY_ID, copyResp.getNewDraftIntygId());
        assertEquals(INTYG_TYPE, copyResp.getNewDraftIntygType());

        verifyZeroInteractions(mockPUService);
        verify(mockUtkastBuilder).populateCopyUtkastFromOrignalUtkast(any(CreateNewDraftCopyRequest.class), any(Person.class));
        verify(mockUtkastRepository).save(any(Utkast.class));
        verify(mockIntegreradeEnheterRegistry).addIfSameVardgivareButDifferentUnits(any(String.class), any(IntegreradEnhetEntry.class));

        verify(mockNotificationService).sendNotificationForDraftCreated(any(Utkast.class));
    }

    private CopyUtkastBuilderResponse createCopyUtkastBuilderResponse() {
        CopyUtkastBuilderResponse resp = new CopyUtkastBuilderResponse();
        resp.setOrginalEnhetsId(VARDENHET_ID);
        resp.setUtkastCopy(createCopyUtkast());
        return resp;
    }

    private Utkast createCopyUtkast() {

        Utkast utkast = new Utkast();
        utkast.setIntygsId(INTYG_COPY_ID);
        utkast.setIntygsTyp(INTYG_TYPE);
        utkast.setPatientPersonnummer(PATIENT_SSN);
        utkast.setPatientFornamn(PATIENT_FNAME);
        utkast.setPatientMellannamn(PATIENT_MNAME);
        utkast.setPatientEfternamn(PATIENT_LNAME);
        utkast.setEnhetsId(VARDENHET_ID);
        utkast.setEnhetsNamn(VARDENHET_NAME);
        utkast.setVardgivarId(VARDGIVARE_ID);
        utkast.setVardgivarNamn(VARDGIVARE_NAME);
        utkast.setModel(INTYG_JSON);

        VardpersonReferens vpRef = new VardpersonReferens();
        vpRef.setHsaId(HOSPERSON_ID);
        vpRef.setNamn(HOSPERSON_NAME);

        utkast.setSenastSparadAv(vpRef);
        utkast.setSkapadAv(vpRef);

        return utkast;
    }

    private CreateNewDraftCopyRequest buildCopyRequest() {
        return new CreateNewDraftCopyRequest(INTYG_ID, INTYG_TYPE, PATIENT_SSN, hoSPerson, vardenhet);
    }

    // testCreateNewDraftCopyPUtjanstFailed()

    // testCreateNewDraftCopyWithNewPersonnummer
}
