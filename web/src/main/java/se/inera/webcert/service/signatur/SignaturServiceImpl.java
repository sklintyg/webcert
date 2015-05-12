package se.inera.webcert.service.signatur;

import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.UUID;

import javax.persistence.OptimisticLockException;

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
import se.inera.webcert.hsa.model.WebCertUser;
import se.inera.webcert.persistence.utkast.model.Signatur;
import se.inera.webcert.persistence.utkast.model.Utkast;
import se.inera.webcert.persistence.utkast.model.UtkastStatus;
import se.inera.webcert.persistence.utkast.model.VardpersonReferens;
import se.inera.webcert.persistence.utkast.repository.UtkastRepository;
import se.inera.webcert.service.draft.util.UpdateUserUtil;
import se.inera.webcert.service.exception.WebCertServiceErrorCodeEnum;
import se.inera.webcert.service.exception.WebCertServiceException;
import se.inera.webcert.service.intyg.IntygService;
import se.inera.webcert.service.log.LogRequestFactory;
import se.inera.webcert.service.log.LogService;
import se.inera.webcert.service.log.dto.LogRequest;
import se.inera.webcert.service.monitoring.MonitoringLogService;
import se.inera.webcert.service.notification.NotificationService;
import se.inera.webcert.service.signatur.dto.SignaturTicket;
import se.inera.webcert.web.service.WebCertUserService;

import com.fasterxml.jackson.databind.ObjectMapper;

@Service
public class SignaturServiceImpl implements SignaturService {

    private static final Logger LOG = LoggerFactory.getLogger(SignaturServiceImpl.class);

    @Autowired
    private UtkastRepository utkastRepository;

    @Autowired
    private WebCertUserService webCertUserService;

    @Autowired
    private SignaturTicketTracker ticketTracker;

    @Autowired
    private IntygService intygService;

    @Autowired
    private LogService logService;

    @Autowired
    private NotificationService notificationService;
    
    @Autowired
    private MonitoringLogService monitoringService; 

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private IntygModuleRegistry moduleRegistry;

    @Override
    public SignaturTicket ticketStatus(String ticketId) {
        SignaturTicket ticket = ticketTracker.getTicket(ticketId);
        if (ticket != null && ticket.getId().equals(ticketId)) {
            return ticket;
        } else {
            return new SignaturTicket(ticketId, SignaturTicket.Status.OKAND, null, 0, null, null, new LocalDateTime());
        }
    }

    @Override
    @Transactional
    public Utkast prepareUtkastForSignering(String intygId, long version, WebCertUser user, LocalDateTime signeringstid) {
        LOG.debug("Hash for clientsignature of draft '{}'", intygId);

        // Fetch the certificate draft
        Utkast utkast = getUtkastForSignering(intygId, version);

        // Update certificate with user information
        utkast = updateUtkastForSignering(utkast, user, signeringstid);

        // Save the certificate draft
        utkast = utkastRepository.save(utkast);

        return utkast;
    }

    @Override
    @Transactional(noRollbackFor=javax.xml.ws.WebServiceException.class)
    public SignaturTicket clientSignature(String ticketId, String rawSignatur) {

        // Lookup signature ticket
        SignaturTicket ticket = ticketTracker.getTicket(ticketId);

        if (ticket == null) {
            LOG.warn("Ticket '{}' hittades ej", ticketId);
            throw new WebCertServiceException(WebCertServiceErrorCodeEnum.INVALID_STATE, "Biljett " + ticketId + " hittades ej");
        }
        LOG.debug("Klientsignering ticket '{}' intyg '{}'", ticket.getId(), ticket.getIntygsId());

        // Fetch the draft
        Utkast utkast = getUtkastForSignering(ticket.getIntygsId(), ticket.getVersion());

        // Fetch Webcert user
        WebCertUser user = webCertUserService.getWebCertUser();

        // Check signature is valid and created by signing user
        /*try {
            String signature = objectMapper.readTree(rawSignatur).get("signatur").textValue();
            if (!signatureService.validateSiths(user.getHsaId(), ticket.getHash(), signature)) {
                throw new WebCertServiceException(WebCertServiceErrorCodeEnum.AUTHORIZATION_PROBLEM, "Kunde inte validera SITHS signatur");
            }
        } catch (IOException e) {
            throw new WebCertServiceException(WebCertServiceErrorCodeEnum.UNKNOWN_INTERNAL_PROBLEM, "Kunde inte validera SITHS signatur", e);
        }*/

        monitoringService.logIntygSigned(utkast.getIntygsId(), user.getHsaId(),
                user.getAuthenticationScheme());

        // Create and persist the new signature
        ticket = createAndPersistSignature(utkast, ticket, rawSignatur, user);

        // Notify stakeholders when certificate has been signed
        notificationService.sendNotificationForDraftSigned(utkast);
        LOG.debug("Notification sent: a certificate draft with id '{}' was signed using CLIENT method", utkast.getIntygsId());

        LogRequest logRequest = LogRequestFactory.createLogRequestFromUtkast(utkast);
        logService.logSignIntyg(logRequest);

        return ticketTracker.updateStatus(ticket.getId(), SignaturTicket.Status.SIGNERAD);
    }

    private SignaturTicket createAndPersistSignature(Utkast utkast, SignaturTicket ticket, String rawSignature, WebCertUser user) {

        String payload = utkast.getModel();

        if (!ticket.getHash().equals(createHash(payload))) {
            LOG.error("Signing of utkast '{}' failed since the payload has been modified since signing was initialized", utkast.getIntygsId());
            throw new WebCertServiceException(WebCertServiceErrorCodeEnum.INVALID_STATE, "Internal error signing utkast, the payload of utkast "
                    + utkast.getIntygsId() + " has been modified since signing was initialized");
        }

        Signatur signatur = new Signatur(ticket.getSigneringstid(), user.getHsaId(), ticket.getIntygsId(), payload, ticket.getHash(), rawSignature);

        // Update user information ("senast sparat av")
        // Add signature to the utkast and set status as signed
        utkast.setSignatur(signatur);
        utkast.setStatus(UtkastStatus.SIGNED);

        // Persist utkast with added signature
        Utkast savedUtkast = utkastRepository.save(utkast);

        // Send to Intygstjanst
        intygService.storeIntyg(savedUtkast);

        return ticket;
    }

    @Override
    @Transactional(noRollbackFor=javax.xml.ws.WebServiceException.class)
    public SignaturTicket serverSignature(Utkast utkast, WebCertUser user, LocalDateTime signeringstid) {
        LOG.debug("Signera utkast '{}'", utkast.getIntygsId());

        // Create and persist signature
        SignaturTicket ticket = createSignaturTicket(utkast.getIntygsId(), utkast.getVersion(), utkast.getModel(), signeringstid);

        ticket = createAndPersistSignature(utkast, ticket, "Signatur", user);
        
        // Audit signing
        monitoringService.logIntygSigned(utkast.getIntygsId(), user.getHsaId(),
                user.getAuthenticationScheme());
        
        // Notify stakeholders when a draft has been signed
        notificationService.sendNotificationForDraftSigned(utkast);
        LOG.debug("Notification sent: a certificate draft with id '{}' was signed using SERVER method", utkast.getIntygsId());

        LogRequest logRequest = LogRequestFactory.createLogRequestFromUtkast(utkast);
        logService.logSignIntyg(logRequest);

        return ticketTracker.updateStatus(ticket.getId(), SignaturTicket.Status.SIGNERAD);
    }

    private Utkast getUtkastForSignering(String intygId, long version) {
        Utkast utkast = utkastRepository.findOne(intygId);

        if (utkast == null) {
            LOG.warn("Utkast '{}' was not found", intygId);
            throw new WebCertServiceException(WebCertServiceErrorCodeEnum.DATA_NOT_FOUND, "Internal error signing utkast, the utkast '" + intygId
                    + "' could not be found");
        } else if (utkast.getVersion() != version) {
                LOG.debug("Utkast '{}' was concurrently modified", intygId);
                throw new OptimisticLockException(utkast.getSenastSparadAv().getNamn());
        } else if (utkast.getStatus() != UtkastStatus.DRAFT_COMPLETE) {
            LOG.warn("Utkast '{}' med status '{}' kunde inte signeras. Måste vara i status {}", intygId, utkast.getStatus(),
                    UtkastStatus.DRAFT_COMPLETE);
            throw new WebCertServiceException(WebCertServiceErrorCodeEnum.INVALID_STATE, "Internal error signing utkast, the utkast '" + intygId
                    + "' was not in state " + UtkastStatus.DRAFT_COMPLETE);
        }

        return utkast;
    }

    /**
     * Update utkast with "senast sparad av" information
     *
     * @param utkast
     * @param userId
     * @param userName
     * @return
     */
    private Utkast updateUtkastForSignering(Utkast utkast, WebCertUser user, LocalDateTime signeringstid) {
        VardpersonReferens vardpersonReferens = UpdateUserUtil.createVardpersonFromWebCertUser(user);
        utkast.setSenastSparadAv(vardpersonReferens);
        try {
            InternalModelHolder internalModel = new InternalModelHolder(utkast.getModel());
            ModuleApi moduleApi = moduleRegistry.getModuleApi(utkast.getIntygsTyp());
            InternalModelResponse updatedInternal = moduleApi.updateBeforeSigning(internalModel, UpdateUserUtil.createUserObject(user), signeringstid);
            utkast.setModel(updatedInternal.getInternalModel());
        } catch (ModuleException e) {
            throw new WebCertServiceException(WebCertServiceErrorCodeEnum.MODULE_PROBLEM, "Could not update with HoS personal", e);
        } catch (ModuleNotFoundException e) {
            throw new WebCertServiceException(WebCertServiceErrorCodeEnum.MODULE_PROBLEM, "Could not update with HoS personal", e);
        }
        
        return utkast;
    }

    public SignaturTicket createSignaturTicket(String intygId, long version, String payload, LocalDateTime signeringstid) {
        try {
            String hash = createHash(payload);
            String id = UUID.randomUUID().toString();
            SignaturTicket statusTicket = new SignaturTicket(id, SignaturTicket.Status.BEARBETAR, intygId, version, signeringstid, hash, new LocalDateTime());
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

}
