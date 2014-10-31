package se.inera.webcert.service.draft;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.IOException;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import se.inera.certificate.integration.json.CustomObjectMapper;
import se.inera.webcert.eid.services.SignatureService;
import se.inera.webcert.hsa.model.WebCertUser;
import se.inera.webcert.persistence.intyg.model.Intyg;
import se.inera.webcert.persistence.intyg.model.IntygsStatus;
import se.inera.webcert.persistence.intyg.model.Signatur;
import se.inera.webcert.persistence.intyg.model.VardpersonReferens;
import se.inera.webcert.persistence.intyg.repository.IntygRepository;
import se.inera.webcert.persistence.intyg.repository.SignaturRepository;
import se.inera.webcert.service.draft.dto.SignatureTicket;
import se.inera.webcert.service.dto.HoSPerson;
import se.inera.webcert.service.exception.WebCertServiceException;
import se.inera.webcert.service.intyg.IntygService;
import se.inera.webcert.service.log.LogService;
import se.inera.webcert.util.ReflectionUtils;
import se.inera.webcert.web.service.WebCertUserService;

@RunWith(MockitoJUnitRunner.class)
public class IntygSignatureServiceImplTest {

    private static final String INTYG_ID = "abc123";

    private static final String INTYG_JSON = "A bit of text representing json";

    private static final String INTYG_TYPE = "fk7263";

    @Mock
    private IntygRepository intygRepository;

    @Mock
    private WebCertUserService webcertUserService;

    @Mock
    private SignatureService signatureService;

    @Mock
    private SignaturRepository signatureRepository;

    @Mock
    IntygService intygService;
    
    @Mock
    private LogService logService;

    @InjectMocks
    private IntygSignatureServiceImpl intygSignatureService = new IntygSignatureServiceImpl();

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
        ReflectionUtils.setTypedField(intygSignatureService, new TicketTracker());
        ReflectionUtils.setTypedField(intygSignatureService, new CustomObjectMapper());
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

    @Test(expected = WebCertServiceException.class)
    public void getSignatureHashReturnsErrorIfIntygNotCompleted() {
        when(intygRepository.findOne(INTYG_ID)).thenReturn(intygDraft);
        intygSignatureService.createDraftHash(INTYG_ID);
        fail();
    }

    @Test(expected = WebCertServiceException.class)
    public void getSignatureHashReturnsErrorIfIntygAlreadySigned() {
        when(intygRepository.findOne(INTYG_ID)).thenReturn(intygSigned);
        intygSignatureService.createDraftHash(INTYG_ID);
        fail();
    }

    @Test
    public void getSignatureHashReturnsTicket() {
        when(intygRepository.findOne(INTYG_ID)).thenReturn(intygCompleted);
        SignatureTicket ticket = intygSignatureService.createDraftHash(INTYG_ID);
        assertEquals(INTYG_ID, ticket.getIntygsId());
        assertEquals(SignatureTicket.Status.BEARBETAR, ticket.getStatus());
    }

    @Test(expected = WebCertServiceException.class)
    public void clientSignatureFailsIfTicketDoesNotExist() {
        when(intygRepository.findOne(INTYG_ID)).thenReturn(intygCompleted);

        intygSignatureService.clientSignature("unknownId", "SIGNATURE");
        fail();
    }

    @Test(expected = WebCertServiceException.class)
    public void clientSignatureFailsIfIntygWasModified() throws IOException {
        when(intygRepository.findOne(INTYG_ID)).thenReturn(intygCompleted);
        SignatureTicket ticket = intygSignatureService.createDraftHash(INTYG_ID);

        intygCompleted.setModel("{}");

        String signature = "{\"signatur\":\"SIGNATURE\"}";
        when(signatureService.validateSiths(hoSPerson.getHsaId(), ticket.getHash(), "SIGNATURE")).thenReturn(true);
        when(signatureRepository.save(any(Signatur.class))).thenReturn(null);

        intygSignatureService.clientSignature(ticket.getId(), signature);
        fail();
    }

    @Test
    public void clientSignatureSuccess() throws IOException {
        when(intygRepository.findOne(INTYG_ID)).thenReturn(intygCompleted);
        SignatureTicket ticket = intygSignatureService.createDraftHash(INTYG_ID);

        SignatureTicket status = intygSignatureService.ticketStatus(ticket.getId());
        assertEquals(SignatureTicket.Status.BEARBETAR, status.getStatus());

        String signature = "{\"signatur\":\"SIGNATURE\"}";
        when(signatureService.validateSiths(hoSPerson.getHsaId(), ticket.getHash(), "SIGNATURE")).thenReturn(true);
        when(signatureRepository.save(any(Signatur.class))).thenReturn(null);

        SignatureTicket signatureTicket = intygSignatureService.clientSignature(ticket.getId(), signature);
        assertNotNull(signatureTicket);
        verify(intygService).storeIntyg(intygCompleted);

        status = intygSignatureService.ticketStatus(ticket.getId());
        assertEquals(SignatureTicket.Status.SIGNERAD, status.getStatus());
    }
}
