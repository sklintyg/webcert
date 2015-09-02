package se.inera.webcert.service.signatur.grp;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Service;

import se.funktionstjanster.grp.v1.AuthenticateRequestType;
import se.funktionstjanster.grp.v1.GrpFault;
import se.funktionstjanster.grp.v1.GrpServicePortType;
import se.funktionstjanster.grp.v1.OrderResponseType;
import se.inera.webcert.service.user.dto.WebCertUser;
import se.inera.webcert.persistence.utkast.model.Utkast;
import se.inera.webcert.persistence.utkast.repository.UtkastRepository;
import se.inera.webcert.service.signatur.SignaturService;
import se.inera.webcert.service.signatur.SignaturTicketTracker;
import se.inera.webcert.service.signatur.dto.SignaturTicket;
import se.inera.webcert.service.signatur.grp.factory.GrpCollectPollerFactory;
import se.inera.webcert.service.user.WebCertUserService;

/**
 * Created by eriklupander on 2015-08-21.
 */
@Service
public class GrpSignaturServiceImpl implements GrpSignaturService {

    static final String BANK_ID_PROVIDER = "bankid"; // As specified in CGI GRP docs

    @Value("${cgi.grp.serviceId}")
    private String serviceId;

    @Value("${cgi.grp.displayName}")
    private String displayName;

    @Autowired
    private WebCertUserService webCertUserService;

    @Autowired
    private SignaturTicketTracker signaturTicketTracker;

    @Autowired
    private SignaturService signaturService;

    @Autowired
    private GrpServicePortType grpService;

    @Autowired
    UtkastRepository utkastRepository;

    @Autowired
    ThreadPoolTaskExecutor taskExecutor;

    @Autowired
    GrpCollectPollerFactory grpCollectPollerFactory;

    @Override
    public SignaturTicket startGrpAuthentication(String intygId, long version) {

        Utkast utkast = utkastRepository.findOne(intygId);
        validateUtkast(intygId, utkast);

        WebCertUser webCertUser = webCertUserService.getWebCertUser();
        validateWebCertUser(webCertUser);

        String personId = webCertUser.getPersonId();
        validatePersonId(personId);

        SignaturTicket draftHash = signaturService.createDraftHash(intygId, utkast.getVersion());

        AuthenticateRequestType authRequest = buildAuthRequest(personId, draftHash);

        OrderResponseType orderResponse = null;
        try {
            orderResponse = grpService.authenticate(authRequest);
        } catch (GrpFault grpFault) {
            // TODO FIX
            signaturTicketTracker.updateStatus(draftHash.getId(), SignaturTicket.Status.OKAND);
            throw new RuntimeException(grpFault.getMessage());
        }

        // If we could init the authentication, we create a SignaturTicket, reusing the mechanism already present for SITHS
        String orderRef = orderResponse.getOrderRef();

        String transactionId = validateOrderResponseTxId(authRequest, orderResponse);

        //taskExecutor.execute(new GrpPoller(orderRef, transactionId, serviceId, displayName, webCertUser, grpService, signaturTicketTracker, signaturService), 6000L);
        startAsyncCollectPoller(webCertUser, orderRef, transactionId);
        return draftHash;
    }

    private void startAsyncCollectPoller(WebCertUser webCertUser, String orderRef, String transactionId) {
        GrpCollectPoller collectTask = grpCollectPollerFactory.getInstance();
        collectTask.setOrderRef(orderRef);
        collectTask.setTransactionId(transactionId);
        collectTask.setWebCertUser(webCertUser);
        taskExecutor.execute(collectTask, 6000L);
    }

    private String validateOrderResponseTxId(AuthenticateRequestType authRequest, OrderResponseType orderResponse) {
        String transactionId = orderResponse.getTransactionId();
        if (!authRequest.getTransactionId().equals(transactionId)) {
            throw new IllegalStateException("OrderResponse transactionId did not match AuthenticateRequest one.");
        }
        return transactionId;
    }

    private void validatePersonId(String personId) {
        if (personId == null) {
            throw new IllegalArgumentException("User principal contained no personId. Cannot issue a GRP auth request " +
                    "without a valid personId. This condition could theoretically occur if a SITHS-logged in lakare " +
                    "accidently managed to init a signing with BankID.");
        }
    }

    private void validateWebCertUser(WebCertUser webCertUser) {
        if (webCertUser == null) {
            throw new IllegalArgumentException("Could not send GRP authenticate request, no user principal found in session.");
        }
    }

    private void validateUtkast(String intygId, Utkast utkast) {
        if (utkast == null) {
            throw new IllegalArgumentException("Could not send GRP authenticate request, no Utkast found for intygId '" + intygId + "'");
        }
    }

    private AuthenticateRequestType buildAuthRequest(String personId, SignaturTicket draftHash) {
        AuthenticateRequestType authRequest = new AuthenticateRequestType();
        authRequest.setPersonalNumber(personId);
        authRequest.setTransactionId(draftHash.getId());
        authRequest.setPolicy(serviceId);
        authRequest.setProvider(BANK_ID_PROVIDER);
        authRequest.setDisplayName(displayName);
        return authRequest;
    }
}
