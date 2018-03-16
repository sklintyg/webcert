/*
 * Copyright (C) 2018 Inera AB (http://www.inera.se)
 *
 * This file is part of sklintyg (https://github.com/sklintyg).
 *
 * sklintyg is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * sklintyg is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package se.inera.intyg.webcert.web.service.signatur.nias;

import java.io.StringReader;
import java.nio.charset.Charset;

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

import se.inera.intyg.infra.xmldsig.XMLDSigService;
import se.inera.intyg.infra.xmldsig.model.SignatureType;
import se.inera.intyg.webcert.common.service.exception.WebCertServiceErrorCodeEnum;
import se.inera.intyg.webcert.common.service.exception.WebCertServiceException;
import se.inera.intyg.webcert.persistence.utkast.model.Utkast;
import se.inera.intyg.webcert.persistence.utkast.repository.UtkastRepository;
import se.inera.intyg.webcert.web.service.signatur.SignaturService;
import se.inera.intyg.webcert.web.service.signatur.SignaturTicketTracker;
import se.inera.intyg.webcert.web.service.signatur.dto.SignaturTicket;
import se.inera.intyg.webcert.web.service.signatur.nias.factory.NiasCollectPollerFactory;
import se.inera.intyg.webcert.web.service.signatur.nias.xmldsig.UtkastModelToXmlConverterServiceImpl;
import se.inera.intyg.webcert.web.service.user.WebCertUserService;
import se.inera.intyg.webcert.web.service.user.dto.WebCertUser;

@Service
public class NiasSignaturServiceImpl implements NiasSignaturService {

    private static final Logger LOG = LoggerFactory.getLogger(NiasSignaturServiceImpl.class);

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

    @Autowired
    private UtkastModelToXmlConverterServiceImpl utkastModelToXmlConverterService;

    @Autowired
    private XMLDSigService xmldSigService;

    @Override
    public SignaturTicket startNiasAuthentication(String intygId, long version) {
        Utkast utkast = utkastRepository.findOne(intygId);
        validateUtkast(intygId, utkast);

        WebCertUser webCertUser = webCertUserService.getUser();
        validateWebCertUser(webCertUser);

        // Try to use personnummer. If not possible, use hsaId instead. This is a temporary hack for testing.
        String personId = webCertUser.getPersonId() != null ? webCertUser.getPersonId() : webCertUser.getHsaId();
      //  validatePersonId(personId);

        SignaturTicket draftHash = signaturService.createDraftHash(intygId, utkast.getVersion());

        // För NetID Access Server signering så behöver vi göra en XMLDSig signatur
        // inklusive en ordentlig digest av canoniserad XML.
        // Börja med att konvertera intyget till XML-format
        String xml = utkastModelToXmlConverterService.utkastToXml(utkast);
        //SignatureType signatureType = xmldSigService.prepareSignature(xml);
        byte[] digestValue = "temp".getBytes(Charset.forName("UTF-8"));//signatureType.getSignedInfo().getReference().get(0).getDigestValue();

        SignResponse response;
        try {
            String result = netiDAccessServerSoap.sign(personId, "Inera Webcert: Signera intyg " + utkast.getIntygsId(),
                    new String(digestValue, Charset.forName("UTF-8")), null);
            response = JAXB.unmarshal(new StringReader(result), SignResponse.class);

        } catch (Exception ex) {
            signaturTicketTracker.updateStatus(draftHash.getId(), SignaturTicket.Status.OKAND);

            throw new WebCertServiceException(WebCertServiceErrorCodeEnum.UNKNOWN_INTERNAL_PROBLEM, ex.getMessage());
        }

        // If we could init the authentication, we create a SignaturTicket, reusing
        // the mechanism already present for SITHS
        String orderRef = response.getSignResult();

        startAsyncNiasCollectPoller(orderRef, draftHash.getId(), new SignatureType());
        return draftHash;
    }

    private void startAsyncNiasCollectPoller(String orderRef, String transactionId, SignatureType signatureType) {
        NiasCollectPoller collectTask = niasCollectPollerFactory.getInstance();
        collectTask.setOrderRef(orderRef);
        collectTask.setTransactionId(transactionId);
        collectTask.setSignature(signatureType);
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
