package se.inera.webcert.service.signatur;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.util.Arrays;

import org.joda.time.LocalDateTime;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
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
import se.inera.webcert.eid.services.SignatureService;
import se.inera.webcert.hsa.model.Mottagning;
import se.inera.webcert.hsa.model.Vardenhet;
import se.inera.webcert.hsa.model.Vardgivare;
import se.inera.webcert.hsa.model.WebCertUser;
import se.inera.webcert.notifications.message.v1.HandelseType;
import se.inera.webcert.notifications.message.v1.NotificationRequestType;
import se.inera.webcert.persistence.utkast.model.Utkast;
import se.inera.webcert.persistence.utkast.model.UtkastStatus;
import se.inera.webcert.persistence.utkast.model.VardpersonReferens;
import se.inera.webcert.persistence.utkast.repository.UtkastRepository;
import se.inera.webcert.service.dto.HoSPerson;
import se.inera.webcert.service.exception.WebCertServiceException;
import se.inera.webcert.service.intyg.IntygService;
import se.inera.webcert.service.log.LogService;
import se.inera.webcert.service.notification.NotificationService;
import se.inera.webcert.service.signatur.SignaturServiceImpl;
import se.inera.webcert.service.signatur.SignaturTicketTracker;
import se.inera.webcert.service.signatur.dto.SignaturTicket;
import se.inera.webcert.util.ReflectionUtils;
import se.inera.webcert.web.service.WebCertUserService;

@RunWith(MockitoJUnitRunner.class)
public class SignaturServiceImplTest {

    private static final String INTYG_ID = "abc123";

    private static final String INTYG_JSON = "A bit of text representing json";

    private static final String INTYG_TYPE = "fk7263";

    @Mock
    private UtkastRepository mockUtkastRepository;

    @Mock
    private SignatureService signatureService;

    @Mock
    IntygService intygService;
    
    @Mock
    private LogService logService;

    @Mock
    private NotificationService notificationService;

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

    @Before
    public void setup() throws ModuleException, ModuleNotFoundException {
        hoSPerson = new HoSPerson();
        hoSPerson.setHsaId("AAA");
        hoSPerson.setNamn("Dr Dengroth");

        VardpersonReferens vardperson = new VardpersonReferens();
        vardperson.setHsaId(hoSPerson.getHsaId());
        vardperson.setNamn(hoSPerson.getNamn());

        utkast = createUtkast(INTYG_ID, INTYG_TYPE, UtkastStatus.DRAFT_INCOMPLETE, INTYG_JSON, vardperson);
        completedUtkast = createUtkast(INTYG_ID, INTYG_TYPE, UtkastStatus.DRAFT_COMPLETE, INTYG_JSON, vardperson);
        signedUtkast = createUtkast(INTYG_ID, INTYG_TYPE, UtkastStatus.SIGNED, INTYG_JSON, vardperson);
        
        internalModelResponse = new InternalModelResponse(INTYG_JSON);
        vardenhet = new Vardenhet("testID", "testNamn");
        vardgivare = new Vardgivare("123", "vardgivare");

        WebCertUser user = new WebCertUser();
        user.setNamn(hoSPerson.getNamn());
        user.setHsaId(hoSPerson.getHsaId());

        user.setValdVardenhet(vardenhet);
        user.setValdVardgivare(vardgivare);

        when(webcertUserService.getWebCertUser()).thenReturn(user);
        when(moduleRegistry.getModuleApi(any(String.class))).thenReturn(moduleApi);
        when(moduleApi.updateBeforeSigning(any(InternalModelHolder.class), any(HoSPersonal.class), any(LocalDateTime.class))).thenReturn(internalModelResponse);
        
        ReflectionUtils.setTypedField(intygSignatureService, new CustomObjectMapper());
        ReflectionUtils.setTypedField(intygSignatureService, new SignaturTicketTracker());
    }

    private Utkast createUtkast(String intygId, String type, UtkastStatus status, String model, VardpersonReferens vardperson) {
        Utkast utkast = new Utkast();
        utkast.setIntygsId(intygId);
        utkast.setIntygsTyp(type);
        utkast.setStatus(status);
        utkast.setModel(model);
        utkast.setSkapadAv(vardperson);
        utkast.setSenastSparadAv(vardperson);
        return utkast;
    }

    @Test(expected = WebCertServiceException.class)
    public void getSignatureHashReturnsErrorIfIntygNotCompleted() {
        when(mockUtkastRepository.findOne(INTYG_ID)).thenReturn(utkast);
        intygSignatureService.createDraftHash(INTYG_ID);
        fail();
    }

    @Test(expected = WebCertServiceException.class)
    public void getSignatureHashReturnsErrorIfIntygAlreadySigned() {
        when(mockUtkastRepository.findOne(INTYG_ID)).thenReturn(signedUtkast);
        intygSignatureService.createDraftHash(INTYG_ID);
        fail();
    }

    @Test
    public void getSignatureHashReturnsTicket() throws ModuleNotFoundException, ModuleException {
        when(mockUtkastRepository.findOne(INTYG_ID)).thenReturn(completedUtkast);
        SignaturTicket ticket = intygSignatureService.createDraftHash(INTYG_ID);
        assertEquals(INTYG_ID, ticket.getIntygsId());
        assertEquals(SignaturTicket.Status.BEARBETAR, ticket.getStatus());
    }

    @Test(expected = WebCertServiceException.class)
    public void clientSignatureFailsIfTicketDoesNotExist() {
        when(mockUtkastRepository.findOne(INTYG_ID)).thenReturn(completedUtkast);

        intygSignatureService.clientSignature("unknownId", "SIGNATURE");
        fail();
    }

    @Test(expected = WebCertServiceException.class)
    public void clientSignatureFailsIfIntygWasModified() throws IOException {
        when(mockUtkastRepository.findOne(INTYG_ID)).thenReturn(completedUtkast);
        SignaturTicket ticket = intygSignatureService.createDraftHash(INTYG_ID);

        completedUtkast.setModel("{}");

        String signature = "{\"signatur\":\"SIGNATURE\"}";
        when(signatureService.validateSiths(hoSPerson.getHsaId(), ticket.getHash(), "SIGNATURE")).thenReturn(true);

        intygSignatureService.clientSignature(ticket.getId(), signature);
        fail();
    }

    @Test
    public void clientSignatureSuccess() throws IOException {

        when(mockUtkastRepository.findOne(INTYG_ID)).thenReturn(completedUtkast);

        SignaturTicket ticket = intygSignatureService.createDraftHash(INTYG_ID);
        SignaturTicket status = intygSignatureService.ticketStatus(ticket.getId());
        assertEquals(SignaturTicket.Status.BEARBETAR, status.getStatus());

        String signature = "{\"signatur\":\"SIGNATURE\"}";
        when(signatureService.validateSiths(hoSPerson.getHsaId(), ticket.getHash(), "SIGNATURE")).thenReturn(true);
        when(mockUtkastRepository.save(any(Utkast.class))).thenReturn(completedUtkast);

        ArgumentCaptor<NotificationRequestType> notificationRequestTypeArgumentCaptor = ArgumentCaptor.forClass(NotificationRequestType.class);

        // Do the call
        SignaturTicket signatureTicket = intygSignatureService.clientSignature(ticket.getId(), signature);

        verify(intygService).storeIntyg(completedUtkast);
        verify(notificationService).notify(notificationRequestTypeArgumentCaptor.capture());

        assertNotNull(signatureTicket);
        
        assertNotNull(completedUtkast.getSignatur());
        assertEquals(UtkastStatus.SIGNED, completedUtkast.getStatus());

        // Assert ticket status has changed from BEARBETAR to SIGNERAD
        status = intygSignatureService.ticketStatus(ticket.getId());
        assertEquals(SignaturTicket.Status.SIGNERAD, status.getStatus());

        // Assert notification message
        NotificationRequestType notificationRequestType = notificationRequestTypeArgumentCaptor.getValue();
        assertEquals(INTYG_ID, notificationRequestType.getIntygsId());
        assertEquals(HandelseType.INTYGSUTKAST_SIGNERAT, notificationRequestType.getHandelse());
    }
}
