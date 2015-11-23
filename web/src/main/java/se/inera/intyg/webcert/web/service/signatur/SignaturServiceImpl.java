package se.inera.intyg.webcert.web.service.signatur;

import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.UUID;

import javax.persistence.OptimisticLockException;

import org.apache.commons.codec.binary.Hex;
import org.apache.commons.io.IOUtils;
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
import se.inera.webcert.common.security.authority.UserPrivilege;
import se.inera.webcert.hsa.model.AuthenticationMethod;
import se.inera.webcert.persistence.utkast.model.Signatur;
import se.inera.webcert.persistence.utkast.model.Utkast;
import se.inera.webcert.persistence.utkast.model.UtkastStatus;
import se.inera.webcert.persistence.utkast.model.VardpersonReferens;
import se.inera.webcert.persistence.utkast.repository.UtkastRepository;
import se.inera.webcert.service.exception.WebCertServiceErrorCodeEnum;
import se.inera.webcert.service.exception.WebCertServiceException;
import se.inera.intyg.webcert.web.service.intyg.IntygService;
import se.inera.intyg.webcert.web.service.log.LogRequestFactory;
import se.inera.intyg.webcert.web.service.log.LogService;
import se.inera.intyg.webcert.web.service.log.dto.LogRequest;
import se.inera.intyg.webcert.web.service.monitoring.MonitoringLogService;
import se.inera.intyg.webcert.web.service.notification.NotificationService;
import se.inera.intyg.webcert.web.service.signatur.asn1.ASN1Util;
import se.inera.intyg.webcert.web.service.signatur.dto.SignaturTicket;
import se.inera.intyg.webcert.web.service.user.WebCertUserService;
import se.inera.intyg.webcert.web.service.user.dto.WebCertUser;
import se.inera.intyg.webcert.web.service.util.UpdateUserUtil;

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
    private IntygModuleRegistry moduleRegistry;

    @Autowired
    private ASN1Util asn1Util;

    @Override
    public SignaturTicket ticketStatus(String ticketId) {
        SignaturTicket ticket = ticketTracker.getTicket(ticketId);
        if ((ticket != null) && ticket.getId().equals(ticketId)) {
            return ticket;
        } else {
            return new SignaturTicket(ticketId, SignaturTicket.Status.OKAND, null, 0, null, null, new LocalDateTime());
        }
    }

    @Override
    @Transactional("jpaTransactionManager")
    public SignaturTicket createDraftHash(String intygId, long version) {
        LOG.debug("Hash for clientsignature of draft '{}'", intygId);

        // Fetch Webcert user
        WebCertUser user = getWebcertUserForSignering();

        // Fetch the certificate draft
        Utkast utkast = getUtkastForSignering(intygId, version, user);

        LocalDateTime signeringstid = LocalDateTime.now();

        // Update certificate with user information
        utkast = updateUtkastForSignering(utkast, user, signeringstid);

        // Save the certificate draft
        utkast = utkastRepository.save(utkast);

        // Flush JPA changes, to make sure the version attribute is updated
        utkastRepository.flush();

        SignaturTicket statusTicket = createSignaturTicket(utkast.getIntygsId(), utkast.getVersion(), utkast.getModel(), signeringstid);

        return statusTicket;
    }

    private WebCertUser getWebcertUserForSignering() {
        WebCertUser user = webCertUserService.getUser();

        if (!user.hasPrivilege(UserPrivilege.PRIVILEGE_SIGNERA_INTYG)) {
            throw new WebCertServiceException(WebCertServiceErrorCodeEnum.AUTHORIZATION_PROBLEM,
                    "User is not a doctor. Could not sign utkast.");
        }
        return user;
    }

    @Override
    @Transactional("jpaTransactionManager")
    public SignaturTicket clientSignature(String ticketId, String rawSignatur) {

        // Fetch Webcert user
        WebCertUser user = getWebcertUserForSignering();

        // For NetID-based signing, we must match the personId / hsaId on the user principal with the serialNumber
        // extracted from the signature data.
        validateSigningIdentity(user, rawSignatur);

        // Use method common between NetID and BankID to finish signing.
        return finalizeClientSignature(ticketId, rawSignatur, user);
    }

    private void validateSigningIdentity(WebCertUser user, String rawSignatur) {

        // Privatläkare som loggat in med NET_ID-klient måste signera med NetID med samma identitet som i sessionen.
        if (user.isPrivatLakare() && (user.getAuthenticationMethod() == AuthenticationMethod.NET_ID)) {
            String signaturPersonId = asn1Util.parsePersonId(IOUtils.toInputStream(rawSignatur));

            if (verifyPersonIdEqual(user, signaturPersonId)) {
                String errMsg = "Cannot finalize signing of Utkast, the logged in user's personId and the personId in the ASN.1 "
                        + "signature data from the NetID client does not match.";
                LOG.error(errMsg);
                throw new WebCertServiceException(WebCertServiceErrorCodeEnum.INDETERMINATE_IDENTITY, errMsg);
            }

        }

        // Siths-inloggade måste signera med samma SITHS-kort som de loggade in med.
        if (user.getAuthenticationMethod() == AuthenticationMethod.SITHS) {
            String signaturHsaId = asn1Util.parseHsaId(IOUtils.toInputStream(rawSignatur));

            if (verifyHsaIdEqual(user, signaturHsaId)) {
                String errMsg = "Cannot finalize signing of Utkast, the logged in user's hsaId and the hsaId in the ASN.1 "
                        + "signature data from the NetID client does not match.";
                LOG.error(errMsg);
                throw new WebCertServiceException(WebCertServiceErrorCodeEnum.INDETERMINATE_IDENTITY, errMsg);
            }
        }
    }

    // Strips off any hyphens
    private boolean verifyPersonIdEqual(WebCertUser user, String signaturPersonId) {
        return !user.getPersonId().trim().replaceAll("\\-", "").equals(signaturPersonId.trim().replaceAll("\\-", ""));
    }

    // We may need to tweak this if it turns out we _somehow_ are getting HsaId's from the sig. that doesn't exactly
    // match what we got from SAML-tickets or HSA on session start. (Kronoberg, see WEBCERT-1501)
    // Consider making a 2-way "subset of" check, if either string is subset of the other, we're OK.
    private boolean verifyHsaIdEqual(WebCertUser user, String signaturHsaId) {
        return !user.getHsaId().trim().replaceAll("\\-", "").equalsIgnoreCase(signaturHsaId.trim().replaceAll("\\-", ""));
    }

    @Override
    @Transactional("jpaTransactionManager")
    public SignaturTicket clientGrpSignature(String biljettId, String rawSignatur, WebCertUser webCertUser) {
        return finalizeClientSignature(biljettId, rawSignatur, webCertUser);
    }

    private SignaturTicket finalizeClientSignature(String ticketId, String rawSignatur, WebCertUser user) {
        // Lookup signature ticket
        SignaturTicket ticket = ticketTracker.getTicket(ticketId);

        if (ticket == null) {
            LOG.warn("Ticket '{}' hittades ej", ticketId);
            throw new WebCertServiceException(WebCertServiceErrorCodeEnum.INVALID_STATE, "Biljett " + ticketId + " hittades ej");
        }
        LOG.debug("Klientsignering ticket '{}' intyg '{}'", ticket.getId(), ticket.getIntygsId());


        // Fetch the draft
        Utkast utkast = getUtkastForSignering(ticket.getIntygsId(), ticket.getVersion(), user);

        monitoringService.logIntygSigned(utkast.getIntygsId(), user.getHsaId(),
                user.getAuthenticationScheme());

        // Create and persist the new signature
        ticket = createAndPersistSignature(utkast, ticket, rawSignatur, user);

        // Notify stakeholders when certificate has been signed
        notificationService.sendNotificationForDraftSigned(utkast);

        LogRequest logRequest = LogRequestFactory.createLogRequestFromUtkast(utkast);
        // Note that we explictly supplies the WebCertUser here. The BankID finalization is not executed in a HTTP
        // request context and thus we need to supply the user instance manually.
        logService.logSignIntyg(logRequest, logService.getLogUser(user));

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
    @Transactional("jpaTransactionManager")
    public SignaturTicket serverSignature(String intygsId, long version) {
        LOG.debug("Signera utkast '{}'", intygsId);

        // On server side we need to create our own signature ticket
        SignaturTicket ticket = createDraftHash(intygsId, version);

        // Fetch Webcert user
        WebCertUser user = getWebcertUserForSignering();

        // Fetch the certificate
        Utkast utkast = getUtkastForSignering(intygsId, ticket.getVersion(), user);
        // Create and persist signature
        ticket = createAndPersistSignature(utkast, ticket, "Signatur", user);

        // Audit signing
        monitoringService.logIntygSigned(utkast.getIntygsId(), user.getHsaId(),
                user.getAuthenticationScheme());

        // Notify stakeholders when a draft has been signed
        notificationService.sendNotificationForDraftSigned(utkast);

        LogRequest logRequest = LogRequestFactory.createLogRequestFromUtkast(utkast);
        logService.logSignIntyg(logRequest);

        return ticketTracker.updateStatus(ticket.getId(), SignaturTicket.Status.SIGNERAD);
    }

    private Utkast getUtkastForSignering(String intygId, long version, WebCertUser user) {
        Utkast utkast = utkastRepository.findOne(intygId);

        if (utkast == null) {
            LOG.warn("Utkast '{}' was not found", intygId);
            throw new WebCertServiceException(WebCertServiceErrorCodeEnum.DATA_NOT_FOUND, "Internal error signing utkast, the utkast '" + intygId
                    + "' could not be found");
        } else if (!user.getIdsOfAllVardenheter().contains(utkast.getEnhetsId())) {
            throw new WebCertServiceException(WebCertServiceErrorCodeEnum.AUTHORIZATION_PROBLEM,
                    "User does not have privileges to sign utkast '" + intygId + "'");
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

    /** Update utkast with "senast sparad av" information.
     */
    private Utkast updateUtkastForSignering(Utkast utkast, WebCertUser user, LocalDateTime signeringstid) {
        VardpersonReferens vardpersonReferens = UpdateUserUtil.createVardpersonFromWebCertUser(user);
        utkast.setSenastSparadAv(vardpersonReferens);
        try {
            InternalModelHolder internalModel = new InternalModelHolder(utkast.getModel());
            ModuleApi moduleApi = moduleRegistry.getModuleApi(utkast.getIntygsTyp());
            InternalModelResponse updatedInternal = moduleApi
                    .updateBeforeSigning(internalModel, UpdateUserUtil.createUserObject(user), signeringstid);
            utkast.setModel(updatedInternal.getInternalModel());
        } catch (ModuleException | ModuleNotFoundException e) {
            throw new WebCertServiceException(WebCertServiceErrorCodeEnum.MODULE_PROBLEM, "Could not update with HoS personal", e);
        }

        return utkast;
    }

    private SignaturTicket createSignaturTicket(String intygId, long version, String payload, LocalDateTime signeringstid) {
        try {
            String hash = createHash(payload);
            String id = UUID.randomUUID().toString();
            SignaturTicket statusTicket = new SignaturTicket(id, SignaturTicket.Status.BEARBETAR, intygId, version, signeringstid, hash,
                    new LocalDateTime());
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
