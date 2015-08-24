package se.inera.webcert.service.signatur;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;

import javax.persistence.OptimisticLockException;

import org.joda.time.LocalDateTime;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import se.inera.certificate.integration.json.CustomObjectMapper;
import se.inera.certificate.modules.registry.IntygModuleRegistry;
import se.inera.certificate.modules.registry.ModuleNotFoundException;
import se.inera.certificate.modules.support.api.ModuleApi;
import se.inera.certificate.modules.support.api.dto.HoSPersonal;
import se.inera.certificate.modules.support.api.dto.InternalModelHolder;
import se.inera.certificate.modules.support.api.dto.InternalModelResponse;
import se.inera.certificate.modules.support.api.exception.ModuleException;
import se.inera.webcert.hsa.model.Vardenhet;
import se.inera.webcert.hsa.model.Vardgivare;
import se.inera.webcert.hsa.model.WebCertUser;
import se.inera.webcert.persistence.utkast.model.Utkast;
import se.inera.webcert.persistence.utkast.model.UtkastStatus;
import se.inera.webcert.persistence.utkast.model.VardpersonReferens;
import se.inera.webcert.persistence.utkast.repository.UtkastRepository;
import se.inera.webcert.service.dto.HoSPerson;
import se.inera.webcert.service.exception.WebCertServiceException;
import se.inera.webcert.service.intyg.IntygService;
import se.inera.webcert.service.log.LogService;
import se.inera.webcert.service.log.dto.LogRequest;
import se.inera.webcert.service.log.dto.LogUser;
import se.inera.webcert.service.monitoring.MonitoringLogService;
import se.inera.webcert.service.notification.NotificationService;
import se.inera.webcert.service.signatur.dto.SignaturTicket;
import se.inera.webcert.util.ReflectionUtils;
import se.inera.webcert.web.service.WebCertUserService;

@RunWith(MockitoJUnitRunner.class)
public class SignaturServiceImplTest {

    private static final String ENHET_ID = "testID";

    private static final String INTYG_ID = "abc123";

    private static final String INTYG_JSON = "A bit of text representing json";

    private static final String INTYG_TYPE = "fk7263";

    @Mock
    private UtkastRepository mockUtkastRepository;

    @Mock
    IntygService intygService;

    @Mock
    private LogService logService;

    @Mock
    private NotificationService notificationService;

    @Mock
    private MonitoringLogService monitoringService;

    @Mock
    private WebCertUserService webcertUserService;

    @Mock
    private IntygModuleRegistry moduleRegistry;

    @Mock
    private ModuleApi moduleApi;

    @InjectMocks
    private SignaturServiceImpl intygSignatureService = new SignaturServiceImpl();

    private Utkast utkast;

    private Utkast completedUtkast;

    private Utkast signedUtkast;

    private HoSPerson hoSPerson;

    private Vardenhet vardenhet;

    private Vardgivare vardgivare;

    private InternalModelResponse internalModelResponse;

    private WebCertUser user;

    @Before
    public void setup() throws ModuleException, ModuleNotFoundException {
        hoSPerson = new HoSPerson();
        hoSPerson.setHsaId("AAA");
        hoSPerson.setNamn("Dr Dengroth");

        VardpersonReferens vardperson = new VardpersonReferens();
        vardperson.setHsaId(hoSPerson.getHsaId());
        vardperson.setNamn(hoSPerson.getNamn());

        utkast = createUtkast(INTYG_ID, 1, INTYG_TYPE, UtkastStatus.DRAFT_INCOMPLETE, INTYG_JSON, vardperson, ENHET_ID);
        completedUtkast = createUtkast(INTYG_ID, 2, INTYG_TYPE, UtkastStatus.DRAFT_COMPLETE, INTYG_JSON, vardperson, ENHET_ID);
        signedUtkast = createUtkast(INTYG_ID, 3, INTYG_TYPE, UtkastStatus.SIGNED, INTYG_JSON, vardperson, ENHET_ID);

        internalModelResponse = new InternalModelResponse(INTYG_JSON);
        vardenhet = new Vardenhet(ENHET_ID, "testNamn");
        vardgivare = new Vardgivare("123", "vardgivare");
        vardgivare.setVardenheter(Arrays.asList(vardenhet));

        user = new WebCertUser();
        user.setNamn(hoSPerson.getNamn());
        user.setHsaId(hoSPerson.getHsaId());
        user.setLakare(true);
        user.setVardgivare(Arrays.asList(vardgivare));

        user.setValdVardenhet(vardenhet);
        user.setValdVardgivare(vardgivare);

        when(webcertUserService.getWebCertUser()).thenReturn(user);
        when(moduleRegistry.getModuleApi(any(String.class))).thenReturn(moduleApi);
        when(moduleApi.updateBeforeSigning(any(InternalModelHolder.class), any(HoSPersonal.class), any(LocalDateTime.class))).thenReturn(
                internalModelResponse);

        ReflectionUtils.setTypedField(intygSignatureService, new CustomObjectMapper());
        ReflectionUtils.setTypedField(intygSignatureService, new SignaturTicketTracker());
    }

    private Utkast createUtkast(String intygId, long version, String type, UtkastStatus status, String model, VardpersonReferens vardperson,
            String enhetsId) {
        Utkast utkast = new Utkast();
        utkast.setIntygsId(intygId);
        utkast.setVersion(version);
        utkast.setIntygsTyp(type);
        utkast.setStatus(status);
        utkast.setModel(model);
        utkast.setSkapadAv(vardperson);
        utkast.setSenastSparadAv(vardperson);
        utkast.setEnhetsId(enhetsId);
        return utkast;
    }

    @Test(expected = WebCertServiceException.class)
    public void getSignatureHashReturnsErrorIfIntygNotCompleted() {
        when(mockUtkastRepository.findOne(INTYG_ID)).thenReturn(utkast);
        intygSignatureService.createDraftHash(INTYG_ID, utkast.getVersion());
    }

    @Test(expected = WebCertServiceException.class)
    public void getSignatureHashReturnsErrorIfIntygAlreadySigned() {
        when(mockUtkastRepository.findOne(INTYG_ID)).thenReturn(signedUtkast);
        intygSignatureService.createDraftHash(INTYG_ID, signedUtkast.getVersion());
    }

    @Test(expected = OptimisticLockException.class)
    public void getSignatureHashReturnsErrorIfWrongVersion() {
        when(mockUtkastRepository.findOne(INTYG_ID)).thenReturn(signedUtkast);
        intygSignatureService.createDraftHash(INTYG_ID, signedUtkast.getVersion() - 1);
    }

    @Test
    public void getSignatureHashReturnsTicket() throws ModuleNotFoundException, ModuleException {
        when(mockUtkastRepository.findOne(INTYG_ID)).thenReturn(completedUtkast);
        when(mockUtkastRepository.save(completedUtkast)).thenReturn(completedUtkast);
        SignaturTicket ticket = intygSignatureService.createDraftHash(INTYG_ID, completedUtkast.getVersion());
        assertEquals(INTYG_ID, ticket.getIntygsId());
        assertEquals(completedUtkast.getVersion(), ticket.getVersion());
        assertEquals(SignaturTicket.Status.BEARBETAR, ticket.getStatus());
    }

    @Test(expected = WebCertServiceException.class)
    public void clientSignatureFailsIfTicketDoesNotExist() {
        when(mockUtkastRepository.findOne(INTYG_ID)).thenReturn(completedUtkast);

        intygSignatureService.clientSignature("unknownId", "SIGNATURE");
    }

    @Test(expected = WebCertServiceException.class)
    public void clientSignatureFailsIfIntygWasModified() throws IOException {
        when(mockUtkastRepository.findOne(INTYG_ID)).thenReturn(completedUtkast);
        when(mockUtkastRepository.save(completedUtkast)).thenReturn(completedUtkast);
        SignaturTicket ticket = intygSignatureService.createDraftHash(INTYG_ID, completedUtkast.getVersion());

        completedUtkast.setModel("{}");

        String signature = "{\"signatur\":\"SIGNATURE\"}";

        intygSignatureService.clientSignature(ticket.getId(), signature);
    }

    @Test
    public void clientSignatureSuccess() throws IOException {

        when(mockUtkastRepository.findOne(INTYG_ID)).thenReturn(completedUtkast);
        when(mockUtkastRepository.save(completedUtkast)).thenReturn(completedUtkast);

        SignaturTicket ticket = intygSignatureService.createDraftHash(INTYG_ID, completedUtkast.getVersion());
        SignaturTicket status = intygSignatureService.ticketStatus(ticket.getId());
        assertEquals(SignaturTicket.Status.BEARBETAR, status.getStatus());

        String signature = "{\"signatur\":\"SIGNATURE\"}";
        when(mockUtkastRepository.save(any(Utkast.class))).thenReturn(completedUtkast);

        // Do the call
        SignaturTicket signatureTicket = intygSignatureService.clientSignature(ticket.getId(), signature);

        verify(intygService).storeIntyg(completedUtkast);
        verify(notificationService).sendNotificationForDraftSigned(any(Utkast.class));
        // Assert pdl log
        verify(logService).logSignIntyg(any(LogRequest.class), any(LogUser.class));

        assertNotNull(signatureTicket);

        assertNotNull(completedUtkast.getSignatur());
        assertEquals(UtkastStatus.SIGNED, completedUtkast.getStatus());

        // Assert ticket status has changed from BEARBETAR to SIGNERAD
        status = intygSignatureService.ticketStatus(ticket.getId());
        assertEquals(SignaturTicket.Status.SIGNERAD, status.getStatus());
    }

    @Test
    public void serverSignatureSuccess() throws IOException {

        when(mockUtkastRepository.findOne(INTYG_ID)).thenReturn(completedUtkast);
        when(mockUtkastRepository.save(any(Utkast.class))).thenReturn(completedUtkast);

        // Do the call
        SignaturTicket signatureTicket = intygSignatureService.serverSignature(INTYG_ID, completedUtkast.getVersion());

        verify(intygService).storeIntyg(completedUtkast);
        verify(notificationService).sendNotificationForDraftSigned(any(Utkast.class));
        // Assert pdl log
        verify(logService).logSignIntyg(any(LogRequest.class));

        assertNotNull(signatureTicket);

        assertNotNull(completedUtkast.getSignatur());
        assertEquals(UtkastStatus.SIGNED, completedUtkast.getStatus());
    }

    @Test(expected = WebCertServiceException.class)
    public void userNotAuthorizedDraft() throws IOException {
        when(mockUtkastRepository.findOne(INTYG_ID)).thenReturn(completedUtkast);
        user.setVardgivare(Collections.<Vardgivare> emptyList());

        intygSignatureService.createDraftHash(INTYG_ID, 1);
    }

    @Test(expected = WebCertServiceException.class)
    public void userIsNotDoctorDraft() throws IOException {
        when(mockUtkastRepository.findOne(INTYG_ID)).thenReturn(completedUtkast);
        user.setLakare(false);

        intygSignatureService.createDraftHash(INTYG_ID, 1);
    }

    @Test(expected = WebCertServiceException.class)
    public void userNotAuthorizedClientSignature() throws IOException {
        when(mockUtkastRepository.findOne(INTYG_ID)).thenReturn(completedUtkast);
        when(mockUtkastRepository.save(completedUtkast)).thenReturn(completedUtkast);
        SignaturTicket ticket = intygSignatureService.createDraftHash(INTYG_ID, completedUtkast.getVersion());
        user.setVardgivare(Collections.<Vardgivare> emptyList());

        intygSignatureService.clientSignature(ticket.getId(), "test");
    }

    @Test(expected = WebCertServiceException.class)
    public void userIsNotDoctorClientSignature() throws IOException {
        when(mockUtkastRepository.findOne(INTYG_ID)).thenReturn(completedUtkast);
        when(mockUtkastRepository.save(completedUtkast)).thenReturn(completedUtkast);
        SignaturTicket ticket = intygSignatureService.createDraftHash(INTYG_ID, completedUtkast.getVersion());
        user.setLakare(false);

        intygSignatureService.clientSignature(ticket.getId(), "test");
    }

    @Test(expected = WebCertServiceException.class)
    public void userNotAuthorizedServerSignature() throws IOException {
        when(mockUtkastRepository.findOne(INTYG_ID)).thenReturn(completedUtkast);
        when(mockUtkastRepository.save(completedUtkast)).thenReturn(completedUtkast);
        user.setVardgivare(Collections.<Vardgivare> emptyList());

        intygSignatureService.serverSignature(INTYG_ID, completedUtkast.getVersion());
    }

    @Test(expected = WebCertServiceException.class)
    public void userIsNotDoctorServerSignature() throws IOException {
        when(mockUtkastRepository.findOne(INTYG_ID)).thenReturn(completedUtkast);
        when(mockUtkastRepository.save(completedUtkast)).thenReturn(completedUtkast);
        user.setLakare(false);

        intygSignatureService.serverSignature(INTYG_ID, completedUtkast.getVersion());
    }
}
