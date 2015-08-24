package se.inera.webcert.service.signatur.grp;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import se.funktionstjanster.grp.v1.CollectRequestType;
import se.funktionstjanster.grp.v1.CollectResponseType;
import se.funktionstjanster.grp.v1.GrpFault;
import se.funktionstjanster.grp.v1.GrpServicePortType;
import se.inera.webcert.hsa.model.WebCertUser;
import se.inera.webcert.service.signatur.SignaturService;
import se.inera.webcert.service.signatur.SignaturTicketTracker;
import se.inera.webcert.service.signatur.dto.SignaturTicket;

/**
 * Runnable implementation responsible for performing once GRP collect lifecycle for a single signerings attempt
 * over CGI's GRP API.
 *
 * Will for up to {@link GrpPoller#TIMEOUT} milliseconds issue a GRP "collect" every 3 seconds and act on the response.
 *
 * The typical flow of a GRP authentication/collect is that a parent thread issues an AuthenticationRequest to the
 * GRP API and stores the AuthenticationResponse orderRef. An instance of this class then goes into a loop and
 * issues a "collect" request until either the may loop times out, a GrpFault is thrown from the API or a terminating
 * ProgressStatusType is returned. On {@link se.funktionstjanster.grp.v1.ProgressStatusType#COMPLETE} the operation has
 * successfully finished (e.g. the user has used BankID or Mobilt BankID and successfully authenticated themselves) and
 * we can notify waiting parties about the success.
 *
 * TODO Uses constructor injection, should clean that up.
 *
 * Created by eriklupander on 2015-08-21.
 */
public class GrpPoller implements Runnable {

    private static final Logger log = LoggerFactory.getLogger(GrpPoller.class);

    private static final long TIMEOUT = 240000L; // 4 minutes, normally an EXPIRED_TRANSACTION will be returned after 3.

    private final String orderRef;
    private final String transactionId;
    private final String serviceId;
    private final String displayName;
    private final WebCertUser webCertUser;
    private GrpServicePortType grpService;
    private SignaturTicketTracker signaturTicketTracker;
    private SignaturService signaturService;

    public GrpPoller(String orderRef, String transactionId, String serviceId, String displayName, WebCertUser webCertUser, GrpServicePortType grpService, SignaturTicketTracker signaturTicketTracker, SignaturService signaturService) {
        this.orderRef = orderRef;
        this.transactionId = transactionId;
        this.serviceId = serviceId;
        this.displayName = displayName;
        this.webCertUser = webCertUser;
        this.grpService = grpService;
        this.signaturTicketTracker = signaturTicketTracker;
        this.signaturService = signaturService;
    }

    @Override
    public void run() {
        long startTimeMs = System.currentTimeMillis();

        while (startTimeMs + TIMEOUT > System.currentTimeMillis()) {

            CollectRequestType req = buildCollectRequest();
            try {

                CollectResponseType resp = grpService.collect(req);
                switch(resp.getProgressStatus()) {
                    case COMPLETE:
                        String signature = resp.getSignature();
                        log.info("GRP collect returned with complete status");
                        signaturService.clientGrpSignature(resp.getTransactionId(), signature, webCertUser);
                        log.info("Signature was successfully persisted and ticket updated.");
                        return;
                    case OUTSTANDING_TRANSACTION:
                    case STARTED:
                    case USER_SIGN:
                        log.info("GRP collect returned ProgressStatusType: " + resp.getProgressStatus());
                        break;
                    case NO_CLIENT:
                        log.info("GRP collect returned ProgressStatusType: " + resp.getProgressStatus() + ", " +
                                "has the user started their BankID or Mobilt BankID application?");
                        break;
                    default:
                        log.info("GRP collect returned unexpected ProgressStatusType: " + resp.getProgressStatus());
                        throw new IllegalStateException("GRP collect returned unexpected ProgressStatusType: " + resp.getProgressStatus());
                }

            } catch (GrpFault grpFault) {
                handleGrpFault(grpFault);
            }

            sleepThreeSeconds();
        }
    }

    private void handleGrpFault(GrpFault grpFault) {
        signaturTicketTracker.updateStatus(transactionId, SignaturTicket.Status.OKAND);
        switch(grpFault.getFaultInfo().getFaultStatus()) {
            case CLIENT_ERR:
                log.error("GRP collect failed with CLIENT_ERR, message: " + grpFault.getFaultInfo().getDetailedDescription());
                break;
            case USER_CANCEL:
                log.info("User cancelled BankID signing.");
                break;
            case ALREADY_COLLECTED:
            case EXPIRED_TRANSACTION:
                log.info("GRP collect failed with status " + grpFault.getFaultInfo().getFaultStatus() + ", this is expected.");
                break;
        }
        throw new RuntimeException(grpFault.getMessage());
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

    private void sleepThreeSeconds() {
        try {
            Thread.sleep(3000L);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
