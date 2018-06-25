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
package se.inera.intyg.webcert.web.service.underskrift.nias;

import java.nio.charset.Charset;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import com.secmaker.netid.nias.v1.NetiDAccessServerSoap;

import se.inera.intyg.webcert.common.service.exception.WebCertServiceErrorCodeEnum;
import se.inera.intyg.webcert.common.service.exception.WebCertServiceException;
import se.inera.intyg.webcert.web.service.underskrift.model.SignaturBiljett;
import se.inera.intyg.webcert.web.service.underskrift.model.SignaturStatus;
import se.inera.intyg.webcert.web.service.underskrift.nias.factory.NiasCollectPollerFactory;
import se.inera.intyg.webcert.web.service.underskrift.tracker.RedisTicketTracker;

@Service
public class NiasUnderskriftServiceImpl implements NiasUnderskriftService {

    private static final Logger LOG = LoggerFactory.getLogger(NiasUnderskriftServiceImpl.class);

    @Autowired
    private NetiDAccessServerSoap netiDAccessServerSoap;

    @Autowired
    private RedisTicketTracker redisTicketTracker;

    @Autowired
    private ThreadPoolTaskExecutor taskExecutor;

    @Autowired
    private NiasCollectPollerFactory niasCollectPollerFactory;

    @Override
    public void startNiasCollectPoller(String personId, SignaturBiljett signaturBiljett) {
        try {
            String base64digest = sha256AsBase64(signaturBiljett.getIntygSignature().getSigningData());
            String orderRef = netiDAccessServerSoap.sign(personId, null, base64digest, null);
            LOG.info("NIAS Sign(..) request returned orderRef {}", orderRef);
            startAsyncNiasCollectPoller(orderRef, signaturBiljett.getTicketId());
        } catch (Exception ex) {
            redisTicketTracker.updateStatus(signaturBiljett.getTicketId(), SignaturStatus.OKAND);
            throw new WebCertServiceException(WebCertServiceErrorCodeEnum.UNKNOWN_INTERNAL_PROBLEM, ex.getMessage());
        }
    }

    private String sha256AsBase64(String signingData) {
        try {
            MessageDigest sha = MessageDigest.getInstance("SHA-256");
            sha.update(signingData.getBytes(Charset.forName("UTF-8")));
            return Base64.getEncoder().encodeToString(sha.digest());
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalArgumentException("Unable to digest signingData, unknown algorithm");
        }
    }

    private void startAsyncNiasCollectPoller(String orderRef, String ticketId) {
        NiasCollectPoller collectTask = niasCollectPollerFactory.getInstance();
        collectTask.setOrderRef(orderRef);
        collectTask.setTicketId(ticketId);
        collectTask.setSecurityContext(SecurityContextHolder.getContext());
        final long startTimeout = 6000L;
        taskExecutor.execute(collectTask, startTimeout);
    }
}
