package se.inera.webcert.service.draft;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.util.Arrays;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import se.inera.certificate.integration.json.CustomObjectMapper;
import se.inera.certificate.modules.support.api.ModuleApi;
import se.inera.certificate.modules.support.api.dto.InternalModelHolder;
import se.inera.certificate.modules.support.api.dto.ValidateDraftResponse;
import se.inera.certificate.modules.support.api.dto.ValidationMessage;
import se.inera.certificate.modules.support.api.dto.ValidationStatus;
import se.inera.certificate.modules.support.api.exception.ModuleException;
import se.inera.webcert.eid.services.SignatureService;
import se.inera.webcert.hsa.model.WebCertUser;
import se.inera.webcert.modules.IntygModuleRegistry;
import se.inera.webcert.persistence.intyg.model.Intyg;
import se.inera.webcert.persistence.intyg.model.IntygsStatus;
import se.inera.webcert.persistence.intyg.model.Signatur;
import se.inera.webcert.persistence.intyg.model.VardpersonReferens;
import se.inera.webcert.persistence.intyg.repository.IntygRepository;
import se.inera.webcert.persistence.intyg.repository.SignaturRepository;
import se.inera.webcert.service.IntygService;
import se.inera.webcert.service.draft.dto.DraftValidation;
import se.inera.webcert.service.draft.dto.SaveAndValidateDraftRequest;
import se.inera.webcert.service.draft.dto.SignatureTicket;
import se.inera.webcert.service.dto.HoSPerson;
import se.inera.webcert.service.exception.WebCertServiceException;
import se.inera.webcert.service.log.LogService;
import se.inera.webcert.service.log.dto.LogRequest;
import se.inera.webcert.util.ReflectionUtils;
import se.inera.webcert.web.service.WebCertUserService;

@RunWith(MockitoJUnitRunner.class)
public class IntygDraftServiceImplTest {

    private static final String INTYG_ID = "abc123";

    private static final String INTYG_JSON = "A bit of text represeting json";

    private static final String INTYG_TYPE = "fk7263";

    @Mock
    private IntygRepository intygRepository;

    @Mock
    private IntygModuleRegistry moduleRegistry;

    @Mock
    private WebCertUserService webcertUserService;

    @Mock
    private LogService logService;

    @Mock
    private SignatureService signatureService;

    @Mock
    private SignaturRepository signatureRepository;

    @Mock
    IntygService intygService;

    @InjectMocks
    private IntygDraftService draftService = new IntygDraftServiceImpl();

    private Intyg intygDraft;

    private Intyg intygCompleted;

    private Intyg intygSigned;

    private HoSPerson hoSPerson;
    
    @Before
    public void setup() {
        hoSPerson = new HoSPerson();
        hoSPerson.setHsaId("AAA");
        hoSPerson.setNamn("Dr Dengroth");

        VardpersonReferens vardperson = new VardpersonReferens();
        vardperson.setHsaId(hoSPerson.getHsaId());
        vardperson.setNamn(hoSPerson.getNamn());

        intygDraft = createIntyg(INTYG_ID, INTYG_TYPE, IntygsStatus.DRAFT_INCOMPLETE, INTYG_JSON, vardperson);
        intygCompleted = createIntyg(INTYG_ID, INTYG_TYPE, IntygsStatus.DRAFT_COMPLETE, INTYG_JSON, vardperson);
        intygSigned = createIntyg(INTYG_ID, INTYG_TYPE, IntygsStatus.SIGNED, INTYG_JSON, vardperson);

        WebCertUser user = new WebCertUser();
        user.setNamn(hoSPerson.getNamn());
        user.setHsaId(hoSPerson.getHsaId());
        when(webcertUserService.getWebCertUser()).thenReturn(user);
        ReflectionUtils.setTypedField(draftService, new TicketTracker());
        ReflectionUtils.setTypedField(draftService, new CustomObjectMapper());
    }

    private Intyg createIntyg(String intygId, String type, IntygsStatus status, String model, VardpersonReferens vardperson) {
        Intyg intyg = new Intyg();
        intyg.setIntygsId(intygId);
        intyg.setIntygsTyp(type);
        intyg.setStatus(status);
        intyg.setModel(model);
        intyg.setSkapadAv(vardperson);
        intyg.setSenastSparadAv(vardperson);
        return intyg;
    }

    @Test
    public void testDeleteDraftThatIsUnsigned() {
        
        when(intygRepository.findOne(INTYG_ID)).thenReturn(intygDraft);
                
        draftService.deleteUnsignedDraft(INTYG_ID);
        
        verify(intygRepository).findOne(INTYG_ID);
        verify(intygRepository).delete(intygDraft);
        verify(logService).logDeleteOfDraft(any(LogRequest.class));
    }
    
    @Test(expected = WebCertServiceException.class)
    public void testDeleteDraftThatIsSigned() {
        
        when(intygRepository.findOne(INTYG_ID)).thenReturn(intygSigned);
                
        draftService.deleteUnsignedDraft(INTYG_ID);
        
        verify(intygRepository).findOne(INTYG_ID);
    }
    
    @Test(expected = WebCertServiceException.class)
    public void testDeleteDraftThatDoesNotExist() {
        
        when(intygRepository.findOne(INTYG_ID)).thenReturn(null);
                
        draftService.deleteUnsignedDraft(INTYG_ID);
        
        verify(intygRepository).findOne(INTYG_ID);
    }
    
    @Test
    public void testSaveAndValidateDraft() throws Exception {

        when(intygRepository.findOne(INTYG_ID)).thenReturn(intygDraft);
        
        ModuleApi mockModuleApi = mock(ModuleApi.class);
        when(moduleRegistry.getModuleApi(INTYG_TYPE)).thenReturn(mockModuleApi);
        
        ValidationMessage valMsg = new ValidationMessage("a.field.somewhere", "This is soooo wrong!");
        
        ValidateDraftResponse validationResponse = new ValidateDraftResponse(ValidationStatus.INVALID, Arrays.asList(valMsg));
        when(mockModuleApi.validateDraft(any(InternalModelHolder.class))).thenReturn(validationResponse);
        
        when(intygRepository.save(intygDraft)).thenReturn(intygDraft);
        
        SaveAndValidateDraftRequest request = buildSaveAndValidateRequest();
        
        DraftValidation res = draftService.saveAndValidateDraft(request);

        verify(intygRepository).save(any(Intyg.class));

        assertNotNull("An DraftValidation should be returned", res);
        assertFalse("Validation should fail", res.isDraftValid());
        assertEquals("Validation should have 1 message", 1, res.getMessages().size());
    }
    
    @Test(expected = WebCertServiceException.class)
    public void testSaveAndValidateDraftThatIsSigned() {
        
        when(intygRepository.findOne(INTYG_ID)).thenReturn(intygSigned);
        
        draftService.saveAndValidateDraft(buildSaveAndValidateRequest());
        
        verify(intygRepository).findOne(INTYG_ID);
    }
    
    @Test(expected = WebCertServiceException.class)
    public void testSaveAndValidateDraftWithExceptionInModule() throws Exception {
    
        when(intygRepository.findOne(INTYG_ID)).thenReturn(intygDraft);
    
        ModuleApi mockModuleApi = mock(ModuleApi.class);
        when(moduleRegistry.getModuleApi(INTYG_TYPE)).thenReturn(mockModuleApi);

        when(mockModuleApi.validateDraft(any(InternalModelHolder.class))).thenThrow(ModuleException.class);

        SaveAndValidateDraftRequest request = buildSaveAndValidateRequest();
        draftService.saveAndValidateDraft(request);
    }

    @Test(expected = WebCertServiceException.class)
    public void getSignatureHashReturnsErrorIfIntygNotCompleted() {
        when(intygRepository.findOne(INTYG_ID)).thenReturn(intygDraft);
        draftService.createDraftHash(INTYG_ID);
        fail();
    }

    @Test
    public void getSignatureHashReturnsTicket() {
        when(intygRepository.findOne(INTYG_ID)).thenReturn(intygCompleted);
        SignatureTicket ticket = draftService.createDraftHash(INTYG_ID);
        assertEquals(INTYG_ID, ticket.getIntygsId());
        assertEquals(SignatureTicket.Status.BEARBETAR, ticket.getStatus());
    }

    @Test(expected = WebCertServiceException.class)
    public void clientSignatureFailsIfTicketDoesNotExist() {
        when(intygRepository.findOne(INTYG_ID)).thenReturn(intygCompleted);

        draftService.clientSignature("unknownId", "SIGNATURE");
        fail();
    }

    @Test(expected = WebCertServiceException.class)
    public void clientSignatureFailsIfIntygWasModified() throws IOException {
        when(intygRepository.findOne(INTYG_ID)).thenReturn(intygCompleted);
        SignatureTicket ticket = draftService.createDraftHash(INTYG_ID);

        intygCompleted.setModel("{}");

        String signature = "{\"signature\":\"SIGNATURE\"}";
        when(signatureService.validateSiths(hoSPerson.getHsaId(), ticket.getHash(), "SIGNATURE")).thenReturn(true);
        when(signatureRepository.save(any(Signatur.class))).thenReturn(null);

        draftService.clientSignature(ticket.getId(), signature);
        fail();
    }

    @Test
    public void clientSignatureSuccess() throws IOException {
        when(intygRepository.findOne(INTYG_ID)).thenReturn(intygCompleted);
        SignatureTicket ticket = draftService.createDraftHash(INTYG_ID);

        SignatureTicket status = draftService.ticketStatus(ticket.getId());
        assertEquals(SignatureTicket.Status.BEARBETAR, status.getStatus());

        String signature = "{\"signature\":\"SIGNATURE\"}";
        when(signatureService.validateSiths(hoSPerson.getHsaId(), ticket.getHash(), "SIGNATURE")).thenReturn(true);
        when(signatureRepository.save(any(Signatur.class))).thenReturn(null);

        SignatureTicket signatureTicket = draftService.clientSignature(ticket.getId(), signature);
        assertNotNull(signatureTicket);
        verify(intygService).storeIntyg(intygCompleted);

        status = draftService.ticketStatus(ticket.getId());
        assertEquals(SignatureTicket.Status.SIGNERAD, status.getStatus());
    }

    private SaveAndValidateDraftRequest buildSaveAndValidateRequest() {
        SaveAndValidateDraftRequest request = new SaveAndValidateDraftRequest();
        request.setIntygId(INTYG_ID);
        request.setDraftAsJson(INTYG_JSON);
        request.setSavedBy(hoSPerson);
        return request;
    }

}
