package se.inera.intyg.webcert.web.service.signatur.nias;

import java.io.StringReader;

import javax.xml.bind.JAXB;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import com.secmaker.netid.nias.v1.AuthenticateResponse;
import com.secmaker.netid.nias.v1.NetiDAccessServerSoap;
import com.secmaker.netid.nias.v1.ResultCollect;
import com.secmaker.netid.nias.v1.SignResponse;

import se.inera.intyg.webcert.common.service.exception.WebCertServiceErrorCodeEnum;
import se.inera.intyg.webcert.common.service.exception.WebCertServiceException;
import se.inera.intyg.webcert.persistence.utkast.model.Utkast;
import se.inera.intyg.webcert.persistence.utkast.repository.UtkastRepository;
import se.inera.intyg.webcert.web.service.signatur.SignaturService;
import se.inera.intyg.webcert.web.service.signatur.SignaturTicketTracker;
import se.inera.intyg.webcert.web.service.signatur.dto.SignaturTicket;
import se.inera.intyg.webcert.web.service.signatur.nias.factory.NiasCollectPollerFactory;
import se.inera.intyg.webcert.web.service.user.WebCertUserService;
import se.inera.intyg.webcert.web.service.user.dto.WebCertUser;

@Service
public class NiasSignaturServiceImpl implements NiasSignaturService {

    private  static final Logger LOG = LoggerFactory.getLogger(NiasSignaturServiceImpl.class);

    @Autowired
    private NetiDAccessServerSoap netiDAccessServerSoap;

    @Autowired
    private WebCertUserService webCertUserService;

    @Autowired
    private SignaturTicketTracker signaturTicketTracker;

    @Autowired
    private SignaturService signaturService;

    @Autowired
    private UtkastRepository utkastRepository;

    @Autowired
    private ThreadPoolTaskExecutor taskExecutor;

    @Autowired
    private NiasCollectPollerFactory niasCollectPollerFactory;


    @Override
    public SignaturTicket startNiasAuthentication(String intygId, long version) {
        Utkast utkast = utkastRepository.findOne(intygId);
        validateUtkast(intygId, utkast);

        WebCertUser webCertUser = webCertUserService.getUser();
        validateWebCertUser(webCertUser);

        String personId = webCertUser.getPersonId();
        validatePersonId(personId);

        SignaturTicket draftHash = signaturService.createDraftHash(intygId, utkast.getVersion());


        SignResponse response;
        try {
            String result = netiDAccessServerSoap.sign(personId, null, null, null);
            response = JAXB.unmarshal(new StringReader(result), SignResponse.class);

        } catch (Exception ex) {
            signaturTicketTracker.updateStatus(draftHash.getId(), SignaturTicket.Status.OKAND);

            throw new WebCertServiceException(WebCertServiceErrorCodeEnum.UNKNOWN_INTERNAL_PROBLEM, ex.getMessage());
        }

        // If we could init the authentication, we create a SignaturTicket, reusing
        // the mechanism already present for SITHS
        String orderRef = response.getSignResult();

        startAsyncNiasCollectPoller(orderRef, draftHash.getId());
        return draftHash;
    }

    private void startAsyncNiasCollectPoller(String orderRef, String transactionId) {
        NiasCollectPoller collectTask = niasCollectPollerFactory.getInstance();
        collectTask.setOrderRef(orderRef);
        collectTask.setTransactionId(transactionId);
        collectTask.setSecurityContext(SecurityContextHolder.getContext());
        final long startTimeout = 6000L;
        taskExecutor.execute(collectTask, startTimeout);
    }

    private void validatePersonId(String personId) {
        if (personId == null) {
            throw new IllegalArgumentException("User principal contained no personId. Cannot issue a NIAS auth request "
                    + "without a valid personId. This condition could theoretically occur if a SITHS-logged in lakare "
                    + "accidently managed to init a signing with BankID.");
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


    @Override
    public String authenticate(String personId, String userNonVisibleData, String endUserInfo) {
        String result = netiDAccessServerSoap.authenticate(personId, userNonVisibleData, endUserInfo);
        AuthenticateResponse response = JAXB.unmarshal(new StringReader(result), AuthenticateResponse.class);
        return response.getAuthenticateResult();
    }

    @Override
    public ResultCollect collect(String orderRef) {
        ResultCollect resultCollect = netiDAccessServerSoap.collect(orderRef);
        return resultCollect;
    }

    @Override
    public SignResponse sign(String personalNumber, String userVisibleData, String userNonVisibleData, String endUserInfo) {
        String xml = netiDAccessServerSoap.sign(personalNumber, userVisibleData, userNonVisibleData, endUserInfo);
        SignResponse response = JAXB.unmarshal(new StringReader(xml), SignResponse.class);
        return response;
    }
}
