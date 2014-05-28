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
import se.inera.webcert.persistence.intyg.model.Intyg;
import se.inera.webcert.persistence.intyg.model.IntygsStatus;
import se.inera.webcert.persistence.intyg.model.Signatur;
import se.inera.webcert.persistence.intyg.repository.IntygRepository;
import se.inera.webcert.persistence.intyg.repository.SignaturRepository;
import se.inera.webcert.service.IntygService;
import se.inera.webcert.service.draft.dto.SignatureTicket;
import se.inera.webcert.service.exception.WebCertServiceErrorCodeEnum;
import se.inera.webcert.service.exception.WebCertServiceException;
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

        Intyg intyg = getIntygForSignering(intygId);
        WebCertUser user = webCertUserService.getWebCertUser();

        intyg.getSenastSparadAv().setHsaId(user.getHsaId());
        intyg.getSenastSparadAv().setNamn(user.getNamn());

        String payload = intyg.getModel();
        SignatureTicket statusTicket = createSignatureTicket(intyg.getIntygsId(), payload);

        intygRepository.save(intyg);

        return statusTicket;
    }


    @Override
    @Transactional
    public SignatureTicket clientSignature(String ticketId, String rawSignatur) {

        SignatureTicket ticket = ticketTracker.getTicket(ticketId);

        if (ticket == null) {
            LOG.warn("Ticket '{}' hittades ej", ticketId);
            throw new WebCertServiceException(WebCertServiceErrorCodeEnum.INVALID_STATE, "Biljett " + ticketId + " hittades ej");
        }
        LOG.debug("Klientsignering ticket '{}' intyg '{}'", ticket.getId(), ticket.getIntygsId());

        WebCertUser user = webCertUserService.getWebCertUser();
        String userId = user.getHsaId();

        try {
            String signature = objectMapper.readTree(rawSignatur).get("signatur").textValue();
            if (!signatureService.validateSiths(userId, ticket.getHash(), signature)) {
                throw new WebCertServiceException(WebCertServiceErrorCodeEnum.INVALID_STATE, "Kunde inte validera intyget");
            }
        } catch (IOException e) {
            throw new WebCertServiceException(WebCertServiceErrorCodeEnum.UNKNOWN_INTERNAL_PROBLEM, "Kunde inte validera intyget", e);
        }

        Intyg intyg = getIntygForSignering(ticket.getIntygsId());
        String payload = intyg.getModel();

        if (!ticket.getHash().equals(createHash(payload))) {
            throw new WebCertServiceException(WebCertServiceErrorCodeEnum.INVALID_STATE, "Intyget är modifierat");
        }

        intyg.setStatus(IntygsStatus.SIGNED);
        intygRepository.save(intyg);

        Signatur signatur = new Signatur(new LocalDateTime(), userId, ticket.getIntygsId(), payload, ticket.getHash(), rawSignatur);
        signaturRepository.save(signatur);

        ticket = ticketTracker.updateStatus(ticket.getId(), SignatureTicket.Status.SIGNERAD);

        intygService.storeIntyg(intyg);

        return ticket;
    }

    @Override
    @Transactional
    public SignatureTicket serverSignature(String intygId) {
        LOG.debug("Signera utkast '{}'", intygId);

        Intyg intyg = getIntygForSignering(intygId);

        WebCertUser user = webCertUserService.getWebCertUser();
        String userId = user.getHsaId();

        intyg.getSenastSparadAv().setHsaId(userId);
        intyg.getSenastSparadAv().setNamn(user.getNamn());

        String payload = intyg.getModel();
        SignatureTicket statusTicket = createSignatureTicket(intyg.getIntygsId(), payload);

        intyg.setStatus(IntygsStatus.SIGNED);
        intygRepository.save(intyg);
        Signatur signatur = new Signatur(new LocalDateTime(), userId, intygId, payload, statusTicket.getHash(), "Signatur");
        signaturRepository.save(signatur);

        ticketTracker.updateStatus(statusTicket.getId(), SignatureTicket.Status.SIGNERAD);

        // TODO hantera fallet att skicka misslyckas.
        intygService.storeIntyg(intyg);

        return statusTicket;
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
}
