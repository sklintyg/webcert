package se.inera.webcert.service.draft;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.UUID;

import org.apache.commons.codec.binary.Hex;
import org.joda.time.LocalDateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import se.inera.certificate.modules.registry.IntygModuleRegistry;
import se.inera.certificate.modules.registry.ModuleNotFoundException;
import se.inera.certificate.modules.support.api.ModuleApi;
import se.inera.certificate.modules.support.api.dto.InternalModelHolder;
import se.inera.certificate.modules.support.api.dto.InternalModelResponse;
import se.inera.certificate.modules.support.api.exception.ModuleException;
import se.inera.webcert.eid.services.SignatureService;
import se.inera.webcert.hsa.model.WebCertUser;
import se.inera.webcert.notifications.message.v1.NotificationRequestType;
import se.inera.webcert.persistence.intyg.model.Intyg;
import se.inera.webcert.persistence.intyg.model.IntygsStatus;
import se.inera.webcert.persistence.intyg.model.Signatur;
import se.inera.webcert.persistence.intyg.model.VardpersonReferens;
import se.inera.webcert.persistence.intyg.repository.IntygRepository;
import se.inera.webcert.service.draft.dto.SignatureTicket;
import se.inera.webcert.service.draft.util.UpdateUserUtil;
import se.inera.webcert.service.exception.WebCertServiceErrorCodeEnum;
import se.inera.webcert.service.exception.WebCertServiceException;
import se.inera.webcert.service.intyg.IntygService;
import se.inera.webcert.service.log.LogService;
import se.inera.webcert.service.notification.NotificationMessageFactory;
import se.inera.webcert.service.notification.NotificationService;
import se.inera.webcert.web.service.WebCertUserService;

import com.fasterxml.jackson.databind.ObjectMapper;

@Service
public class IntygSignatureServiceImpl implements IntygSignatureService {

    private static final Logger LOG = LoggerFactory.getLogger(IntygSignatureServiceImpl.class);

    @Autowired
    private IntygRepository intygRepository;

    @Autowired
    private WebCertUserService webCertUserService;

    @Autowired
    private TicketTracker ticketTracker;

    @Autowired
    private IntygService intygService;

    @Autowired
    private SignatureService signatureService;

    @Autowired
    private LogService logService;

    @Autowired
    private NotificationService notificationService;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private IntygModuleRegistry moduleRegistry;

    @Override
    public SignatureTicket ticketStatus(String ticketId) {
        SignatureTicket ticket = ticketTracker.getTicket(ticketId);
        if (ticket != null && ticket.getId().equals(ticketId)) {
            return ticket;
        } else {
            return new SignatureTicket(ticketId, SignatureTicket.Status.OKAND, null, null, null, new LocalDateTime());
        }
    }

    @Override
    @Transactional
    public SignatureTicket createDraftHash(String intygId) {
        LOG.debug("Hash for clientsignature of draft '{}'", intygId);

        // Fetch the certificate
        Intyg intyg = getIntygForSignering(intygId);

        // Fetch Webcert user
        WebCertUser user = webCertUserService.getWebCertUser();

        LocalDateTime signeringstid = LocalDateTime.now();
        
        // Update certificate with user information
        intyg = updateIntygForSignering(intyg, user, signeringstid);

        // Save the certificate
        intygRepository.save(intyg);

        SignatureTicket statusTicket = createSignatureTicket(intyg.getIntygsId(), intyg.getModel(), signeringstid);

        return statusTicket;
    }

    @Override
    @Transactional
    public SignatureTicket clientSignature(String ticketId, String rawSignatur) {

        // Lookup signature ticket
        SignatureTicket ticket = ticketTracker.getTicket(ticketId);

        if (ticket == null) {
            LOG.warn("Ticket '{}' hittades ej", ticketId);
            throw new WebCertServiceException(WebCertServiceErrorCodeEnum.INVALID_STATE, "Biljett " + ticketId + " hittades ej");
        }
        LOG.debug("Klientsignering ticket '{}' intyg '{}'", ticket.getId(), ticket.getIntygsId());

        // Fetch the certificate
        Intyg intyg = getIntygForSignering(ticket.getIntygsId());

        // Fetch Webcert user
        WebCertUser user = webCertUserService.getWebCertUser();

        // Check signature is valid and created by signing user
        try {
            String signature = objectMapper.readTree(rawSignatur).get("signatur").textValue();
            if (!signatureService.validateSiths(user.getHsaId(), ticket.getHash(), signature)) {
                throw new WebCertServiceException(WebCertServiceErrorCodeEnum.INVALID_STATE, "Kunde inte validera intyget");
            }
        } catch (IOException e) {
            throw new WebCertServiceException(WebCertServiceErrorCodeEnum.UNKNOWN_INTERNAL_PROBLEM, "Kunde inte validera intyget", e);
        }

        // Create and persist the new signature
        ticket = createAndPersistSignature(intyg, ticket, rawSignatur, user);

        // Notify stakeholders when certificate has been signed
        sendNotification(intyg);

        return ticketTracker.updateStatus(ticket.getId(), SignatureTicket.Status.SIGNERAD);
    }

    private SignatureTicket createAndPersistSignature(Intyg intyg, SignatureTicket ticket, String rawSignature, WebCertUser user) {

        String payload = intyg.getModel();

        if (!ticket.getHash().equals(createHash(payload))) {
            LOG.error("Signing of Intyg '{}' failed since the payload has been modified since signing was initialized", intyg.getIntygsId());
            throw new WebCertServiceException(WebCertServiceErrorCodeEnum.INVALID_STATE, "Internal error signing intyg, the payload of intyg "
                    + intyg.getIntygsId() + " has been modified since signing was initialized");
        }

        Signatur signatur = new Signatur(ticket.getSigneringstid(), user.getHsaId(), ticket.getIntygsId(), payload, ticket.getHash(), rawSignature);

        // Update user information ("senast sparat av")
        // Add signature to Intyg and set status as signed
        intyg.setSignatur(signatur);
        intyg.setStatus(IntygsStatus.SIGNED);

        // Persist intyg with signature
        Intyg savedIntyg = intygRepository.save(intyg);

        // Send to Intygstjanst
        intygService.storeIntyg(savedIntyg);

        return ticket;
    }

    @Override
    @Transactional
    public SignatureTicket serverSignature(String intygsId) {
        LOG.debug("Signera utkast '{}'", intygsId);

        // On server side we need to create our own signature ticket
        SignatureTicket ticket = createDraftHash(intygsId);

        // Fetch Webcert user
        WebCertUser user = webCertUserService.getWebCertUser();

        // Fetch the certificate
        Intyg intyg = getIntygForSignering(intygsId);

        // Create and persist signature
        ticket = createAndPersistSignature(intyg, ticket, "Signatur", user);

        // Notify stakeholders when a draft has been signed
        sendNotification(intyg);

        return ticketTracker.updateStatus(ticket.getId(), SignatureTicket.Status.SIGNERAD);
    }

    private Intyg getIntygForSignering(String intygId) {
        Intyg intyg = intygRepository.findOne(intygId);

        if (intyg == null) {
            LOG.warn("Intyg '{}' was not found", intygId);
            throw new WebCertServiceException(WebCertServiceErrorCodeEnum.DATA_NOT_FOUND, "Internal error signing intyg, the intyg " + intygId
                    + " could not be found");
        } else if (intyg.getStatus() != IntygsStatus.DRAFT_COMPLETE) {
            LOG.warn("Intyg '{}' med status '{}' kunde inte signeras. Måste vara i status {}", intygId, intyg.getStatus(),
                    IntygsStatus.DRAFT_COMPLETE);
            throw new WebCertServiceException(WebCertServiceErrorCodeEnum.INVALID_STATE, "Internal error signing intyg, the intyg " + intygId
                    + " was not in state " + IntygsStatus.DRAFT_COMPLETE);
        }

        return intyg;
    }

    /**
     * Update certificate with "senast sparad av" information
     *
     * @param intyg
     * @param userId
     * @param userName
     * @return
     */
    private Intyg updateIntygForSignering(Intyg intyg, WebCertUser user, LocalDateTime signeringstid) {
        VardpersonReferens vardpersonReferens = UpdateUserUtil.createVardpersonFromWebCertUser(user);
        intyg.setSenastSparadAv(vardpersonReferens);
        try {
            InternalModelHolder internalModel = new InternalModelHolder(intyg.getModel());
            ModuleApi moduleApi = moduleRegistry.getModuleApi(intyg.getIntygsTyp());
            InternalModelResponse updatedInternal = moduleApi.updateBeforeSigning(internalModel, UpdateUserUtil.createUserObject(user), signeringstid);
            intyg.setModel(updatedInternal.getInternalModel());
        } catch (ModuleException e) {
            throw new WebCertServiceException(WebCertServiceErrorCodeEnum.MODULE_PROBLEM, "Could not update with HoS personal", e);
        } catch (ModuleNotFoundException e) {
            throw new WebCertServiceException(WebCertServiceErrorCodeEnum.MODULE_PROBLEM, "Could not update with HoS personal", e);
        }
        return intyg;
    }

    private SignatureTicket createSignatureTicket(String intygId, String payload, LocalDateTime signeringstid) {
        try {
            String hash = createHash(payload);
            String id = UUID.randomUUID().toString();
            SignatureTicket statusTicket = new SignatureTicket(id, SignatureTicket.Status.BEARBETAR, intygId, signeringstid, hash, new LocalDateTime());
            ticketTracker.trackTicket(statusTicket);
            return statusTicket;
        } catch (IllegalStateException e) {
            LOG.error("Error occured when generating signing hash for intyg {}: {}", intygId, e);
            throw new WebCertServiceException(WebCertServiceErrorCodeEnum.UNKNOWN_INTERNAL_PROBLEM,
                    "Internal error signing intyg " + intygId + ", problem when creating signing ticket", e);
        }
    }

    private String createHash(String payload) {
        try {
            MessageDigest sha = MessageDigest.getInstance("SHA-256");
            sha.update(payload.getBytes("UTF-8"));
            byte[] digest = sha.digest();
            return new String(Hex.encodeHex(digest));
        } catch (NoSuchAlgorithmException | UnsupportedEncodingException e) {
            throw new IllegalStateException(e);
        }
    }

    private void sendNotification(Intyg intyg) {
        NotificationRequestType notificationRequestType = NotificationMessageFactory.createNotificationFromSignedDraft(intyg);
        notificationService.notify(notificationRequestType);
        LOG.debug("Notification sent: a certificate draft with id '{}' was signed", intyg.getIntygsId());
    }

}
