package se.inera.webcert.service.signatur.grp;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Service;

import se.funktionstjanster.grp.v1.AuthenticateRequestType;
import se.funktionstjanster.grp.v1.GrpFault;
import se.funktionstjanster.grp.v1.GrpServicePortType;
import se.funktionstjanster.grp.v1.OrderResponseType;
import se.inera.webcert.hsa.model.WebCertUser;
import se.inera.webcert.persistence.utkast.model.Utkast;
import se.inera.webcert.persistence.utkast.repository.UtkastRepository;
import se.inera.webcert.service.signatur.SignaturService;
import se.inera.webcert.service.signatur.SignaturTicketTracker;
import se.inera.webcert.service.signatur.dto.SignaturTicket;
import se.inera.webcert.web.service.WebCertUserService;

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

    // private GrpTxIdGenerator grpTxIdGenerator = new GrpTxIdGenerator();

    @Override
    public SignaturTicket sendAuthenticateRequest(String intygId, long version) {

        Utkast utkast = utkastRepository.findOne(intygId);

        WebCertUser webCertUser = webCertUserService.getWebCertUser();
        String personId = webCertUser.getPersonId();

        SignaturTicket draftHash = signaturService.createDraftHash(intygId, utkast.getVersion());

        AuthenticateRequestType authRequest = buildAuthRequest(personId, draftHash);

        OrderResponseType orderResponse = null;
        try {
            orderResponse = grpService.authenticate(authRequest);
        } catch (GrpFault grpFault) {
            // TODO FIX
            throw new RuntimeException(grpFault.getMessage());
        }

        // If we could init the authentication, we create a SignaturTicket, reusing the mechanism already present for SITHS
        String orderRef = orderResponse.getOrderRef();

        String transactionId = orderResponse.getTransactionId();
        if (!authRequest.getTransactionId().equals(transactionId)) {
            throw new IllegalStateException("OrderResponse transactionId did not match AuthenticateRequest one.");
        }

        taskExecutor.execute(new GrpPoller(orderRef, transactionId, serviceId, displayName, webCertUser, grpService, signaturTicketTracker, signaturService), 6000L);

        return draftHash;
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
