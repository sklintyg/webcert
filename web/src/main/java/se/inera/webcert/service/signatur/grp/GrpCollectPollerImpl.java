package se.inera.webcert.service.signatur.grp;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import se.funktionstjanster.grp.v1.CollectRequestType;
import se.funktionstjanster.grp.v1.CollectResponseType;
import se.funktionstjanster.grp.v1.GrpFault;
import se.funktionstjanster.grp.v1.GrpServicePortType;
import se.inera.webcert.hsa.model.WebCertUser;
import se.inera.webcert.service.signatur.SignaturService;
import se.inera.webcert.service.signatur.SignaturTicketTracker;
import se.inera.webcert.service.signatur.dto.SignaturTicket;

/**
 * Runnable implementation / spring prototype bean responsible for performing once GRP collect lifecycle for a single
 * signerings attempt over CGI's GRP API.
 *
 * Will for up to {@link GrpPoller#TIMEOUT} milliseconds issue a GRP "collect" every 3 seconds and act on the response.
 *
 * The typical flow of a GRP authentication/collect is that a parent thread issues an AuthenticationRequest to the
 * GRP API and stores the AuthenticationResponse orderRef. An instance of this class then goes into a loop and
 * issues a "collect" request until either the may loop times out, a GrpFault is thrown from the API or a terminating
 * ProgressStatusType is returned. On {@link se.funktionstjanster.grp.v1.ProgressStatusType#COMPLETE} the operation has
 * successfully finished (e.g. the user has used BankID or Mobilt BankID and successfully authenticated themselves) and
 * we can notify waiting parties about the success.
 */
@Component(value = "grpCollectPoller")
@Scope(value = "prototype")
public class GrpCollectPollerImpl implements GrpCollectPoller {

    private static final Logger log = LoggerFactory.getLogger(GrpCollectPollerImpl.class);

    private static long TIMEOUT = 240000L; // 4 minutes, normally an EXPIRED_TRANSACTION will be returned after 3.

    private String orderRef;
    private String transactionId;
    private WebCertUser webCertUser;


    @Value("${cgi.grp.serviceId}")
    private String serviceId;

    @Value("${cgi.grp.displayName}")
    private String displayName;

    @Autowired
    private SignaturTicketTracker signaturTicketTracker;

    @Autowired
    private SignaturService signaturService;

    @Autowired
    private GrpServicePortType grpService;

    private long ms = 3000L;

    @Override
    public void run() {
        long startTimeMs = System.currentTimeMillis();

        while (startTimeMs + TIMEOUT > System.currentTimeMillis()) {

            CollectRequestType req = buildCollectRequest();
            try {

                CollectResponseType resp = grpService.collect(req);
                switch (resp.getProgressStatus()) {
                    case COMPLETE:
                        String signature = resp.getSignature();
                        log.info("GRP collect returned with complete status");
                        signaturService.clientGrpSignature(resp.getTransactionId(), signature, webCertUser);
                        log.info("Signature was successfully persisted and ticket updated.");
                        return;

                    case OUTSTANDING_TRANSACTION:
                    case STARTED:
                    case USER_REQ:
                    case USER_SIGN:
                        log.info("GRP collect returned ProgressStatusType: {}", resp.getProgressStatus());
                        break;

                    case NO_CLIENT:
                        log.info("GRP collect returned ProgressStatusType: {}, " +
                                "has the user started their BankID or Mobilt BankID application?", resp.getProgressStatus());
                        break;
                }

            } catch (GrpFault grpFault) {
                handleGrpFault(grpFault);
                // Always terminate loop after a GrpFault has been encountered
                return;
            }

            sleepMs(ms);
        }
    }

    private void handleGrpFault(GrpFault grpFault) {
        signaturTicketTracker.updateStatus(transactionId, SignaturTicket.Status.OKAND);
        switch (grpFault.getFaultInfo().getFaultStatus()) {
            case CLIENT_ERR:
                log.error("GRP collect failed with CLIENT_ERR, message: {}", grpFault.getFaultInfo().getDetailedDescription());
                break;
            case USER_CANCEL:
                log.info("User cancelled BankID signing.");
                break;
            case ALREADY_COLLECTED:
            case EXPIRED_TRANSACTION:
                log.info("GRP collect failed with status {}, this is expected " +
                                "when the user doesn't start their BankID client and transaction times out after ~3 minutes.",
                        grpFault.getFaultInfo().getFaultStatus());
                break;
            default:
                log.error("Unexpected GrpFault thrown when performing GRP collect: {}. Message: {}",
                        grpFault.getFaultInfo().getFaultStatus().toString(),
                        grpFault.getFaultInfo().getDetailedDescription());
                break;
        }
    }

    private CollectRequestType buildCollectRequest() {
        CollectRequestType req = new CollectRequestType();
        req.setOrderRef(orderRef);
        req.setTransactionId(transactionId);
        req.setPolicy(serviceId);
        req.setDisplayName(displayName);
        req.setProvider(GrpSignaturServiceImpl.BANK_ID_PROVIDER);
        return req;
    }

    private void sleepMs(long ms) {
        try {
            Thread.sleep(ms);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    /**
     * Use this for unit-testing purposes only
     */
    void setMs(long ms) {
        this.ms = ms;
    }

    public void setOrderRef(String orderRef) {
        this.orderRef = orderRef;
    }

    public void setTransactionId(String transactionId) {
        this.transactionId = transactionId;
    }

    public void setWebCertUser(WebCertUser webCertUser) {
        this.webCertUser = webCertUser;
    }
}
