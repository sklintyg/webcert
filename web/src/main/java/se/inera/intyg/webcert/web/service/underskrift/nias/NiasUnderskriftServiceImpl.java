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

import com.secmaker.netid.nias.v1.NetiDAccessServerSoap;
import com.secmaker.netid.nias.v1.SignResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import se.inera.intyg.webcert.common.service.exception.WebCertServiceErrorCodeEnum;
import se.inera.intyg.webcert.common.service.exception.WebCertServiceException;
import se.inera.intyg.webcert.web.service.underskrift.model.SignaturBiljett;
import se.inera.intyg.webcert.web.service.underskrift.model.SignaturStatus;
import se.inera.intyg.webcert.web.service.underskrift.nias.factory.NiasCollectPollerFactory;
import se.inera.intyg.webcert.web.service.underskrift.tracker.RedisTicketTracker;

import javax.xml.bind.JAXB;
import java.io.StringReader;

@Service
public class NiasUnderskriftServiceImpl implements NiasUnderskriftService {

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
        SignResponse response;
        try {
            String result = netiDAccessServerSoap.sign(personId, "Inera Webcert: Signera intyg " + signaturBiljett.getIntygsId(),
                    signaturBiljett.getIntygSignature().getSigningData(), null);
            response = JAXB.unmarshal(new StringReader(result), SignResponse.class);

        } catch (Exception ex) {
            signaturBiljett.setStatus(SignaturStatus.OKAND);
            redisTicketTracker.updateBiljett(signaturBiljett);
            throw new WebCertServiceException(WebCertServiceErrorCodeEnum.UNKNOWN_INTERNAL_PROBLEM, ex.getMessage());
        }

        // If we could init the authentication, we create a SignaturTicket, reusing
        // the mechanism already present for SITHS
        String orderRef = response.getSignResult();
        startAsyncNiasCollectPoller(orderRef, signaturBiljett.getTicketId());
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
