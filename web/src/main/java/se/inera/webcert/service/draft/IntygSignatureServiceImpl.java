package se.inera.webcert.service.draft;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.codec.binary.Hex;
import org.joda.time.LocalDateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import se.inera.webcert.eid.services.SignatureService;
import se.inera.webcert.hsa.model.WebCertUser;
import se.inera.webcert.notifications.message.v1.NotificationRequestType;
import se.inera.webcert.persistence.intyg.model.Intyg;
import se.inera.webcert.persistence.intyg.model.IntygsStatus;
import se.inera.webcert.persistence.intyg.model.Signatur;
import se.inera.webcert.persistence.intyg.repository.IntygRepository;
import se.inera.webcert.persistence.intyg.repository.SignaturRepository;
import se.inera.webcert.service.draft.dto.SignatureTicket;
import se.inera.webcert.service.exception.WebCertServiceErrorCodeEnum;
import se.inera.webcert.service.exception.WebCertServiceException;
import se.inera.webcert.service.intyg.IntygService;
import se.inera.webcert.service.log.LogService;
import se.inera.webcert.service.notification.NotificationMessageFactory;
import se.inera.webcert.service.notification.NotificationService;
import se.inera.webcert.web.service.WebCertUserService;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.UUID;

@Service
public class IntygSignatureServiceImpl implements IntygSignatureService {

    private static final Logger LOG = LoggerFactory.getLogger(IntygSignatureServiceImpl.class);

    @Autowired
    private IntygRepository intygRepository;

    @Autowired
    private SignaturRepository signaturRepository;

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

    @Override
    public SignatureTicket ticketStatus(String ticketId) {
        SignatureTicket ticket = ticketTracker.getTicket(ticketId);
        if (ticket != null && ticket.getId().equals(ticketId)) {
            return ticket;
        } else {
            return new SignatureTicket(ticketId, SignatureTicket.Status.OKAND, null, null, new LocalDateTime());
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

        // Update certificate with user information
        intyg = updateIntygForSignering(intyg, user.getHsaId(), user.getNamn());

        // Save the certificate
        intygRepository.save(intyg);

        SignatureTicket statusTicket = createSignatureTicket(intyg.getIntygsId(), intyg.getModel());
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

        // Update user information ("senast sparat av")
        intyg = updateIntygForSignering(intyg, user.getHsaId(), user.getNamn());

        // Save certificate wth new status
        saveIntyg(intyg, IntygsStatus.SIGNED);

        // Create new signature
        ticket = clientSignature(ticket, intyg.getModel(), rawSignatur, user.getHsaId());

        // Notify stakeholders when certificate has been signed
        notify(intyg);

        return ticket;
    }

    private SignatureTicket clientSignature(SignatureTicket ticket, String payload, String rawSignature, String userId) {

        if (!ticket.getHash().equals(createHash(payload))) {
            throw new WebCertServiceException(WebCertServiceErrorCodeEnum.INVALID_STATE, "Intyget är modifierat");
        }

        // Create and save a new signature
        createAndSaveSignature(ticket, payload, rawSignature, userId);

        return ticketTracker.updateStatus(ticket.getId(), SignatureTicket.Status.SIGNERAD);
    }

    @Override
    @Transactional
    public SignatureTicket serverSignature(String intygsId) {
        LOG.debug("Signera utkast '{}'", intygsId);

        // Fetch the certificate
        Intyg intyg = getIntygForSignering(intygsId);

        // Fetch Webcert user
        WebCertUser user = webCertUserService.getWebCertUser();

        // Update user information ("senast sparad av")
        intyg = updateIntygForSignering(intyg, user.getHsaId(), user.getNamn());

        // Save certificate wth new status
        saveIntyg(intyg, IntygsStatus.SIGNED);

        // Sign draft
        SignatureTicket ticket = serverSignature(intygsId, intyg.getModel(), "Signatur", user.getHsaId());

        // Notify stakeholders when a draft has been signed
        notify(intyg);

        return ticket;
    }

    private SignatureTicket serverSignature(String intygsId, String payload, String rawSignature, String userId) {

        // On server side we need to create our own signature ticket
        SignatureTicket statusTicket = createSignatureTicket(intygsId, payload);

        // Create and save a new signature
        createAndSaveSignature(statusTicket, payload, rawSignature, userId);

        return ticketTracker.updateStatus(statusTicket.getId(), SignatureTicket.Status.SIGNERAD);

        // return statusTicket;
    }

    private void createAndSaveSignature(SignatureTicket ticket, String payload, String rawSignature, String userId) {
        Signatur signatur = new Signatur(new LocalDateTime(), userId, ticket.getIntygsId(), payload, ticket.getHash(), rawSignature);
        signaturRepository.save(signatur);
    }

    /**
     * Save a certificate draft to repository and Intygstjänsten
     *
     * @param intyg
     *            the certificate draft to save
     * @param intygsStatus
     *            set certificate draft status
     */
    private void saveIntyg(Intyg intyg, IntygsStatus intygsStatus) {
        intyg.setStatus(intygsStatus);
        intygRepository.save(intyg);
        intygService.storeIntyg(intyg);
    }

    private Intyg getIntygForSignering(String intygId) {
        Intyg intyg = intygRepository.findOne(intygId);

        if (intyg == null) {
            LOG.warn("Intyg '{}' was not found", intygId);
            throw new WebCertServiceException(WebCertServiceErrorCodeEnum.DATA_NOT_FOUND, "The intyg could not be found");
        } else if (intyg.getStatus() != IntygsStatus.DRAFT_COMPLETE) {
            LOG.warn("Intyg '{}' med status '{}' kunde inte signeras", intygId, intyg.getStatus());
            throw new WebCertServiceException(WebCertServiceErrorCodeEnum.INVALID_STATE, "The intyg was not in state " + IntygsStatus.DRAFT_COMPLETE);
        }

        return intyg;
    }

    /**
     * Update certificate with "senast sparad av" information
     *
     * @param intyg
     * @param user
     * @return
     */
    private Intyg updateIntygForSignering(Intyg intyg, WebCertUser user) {
        return updateIntygForSignering(intyg, user.getHsaId(), user.getNamn());
    }

    /**
     * Update certificate with "senast sparad av" information
     *
     * @param intyg
     * @param userId
     * @param userName
     * @return
     */
    private Intyg updateIntygForSignering(Intyg intyg, String userId, String userName) {
        intyg.getSenastSparadAv().setHsaId(userId);
        intyg.getSenastSparadAv().setNamn(userName);
        return intyg;
    }

    private SignatureTicket createSignatureTicket(String intygId, String payload) {
        try {
            String hash = createHash(payload);
            String id = UUID.randomUUID().toString();
            SignatureTicket statusTicket = new SignatureTicket(id, SignatureTicket.Status.BEARBETAR, intygId, hash, new LocalDateTime());
            ticketTracker.trackTicket(statusTicket);
            return statusTicket;
        } catch (IllegalStateException e) {
            LOG.error("Fel vid hashgenerering intyg {}. {}", intygId, e);
            throw new WebCertServiceException(WebCertServiceErrorCodeEnum.UNKNOWN_INTERNAL_PROBLEM, "Internal error signing intyg", e);
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

    private void notify(Intyg intyg) {
        NotificationRequestType notificationRequestType = NotificationMessageFactory.createNotificationFromSignedDraft(intyg);
        notificationService.notify(notificationRequestType);
    }

}
