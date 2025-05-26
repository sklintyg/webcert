/*
 * Copyright (C) 2025 Inera AB (http://www.inera.se)
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
package se.inera.intyg.webcert.web.service.underskrift.grp;

import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.Instant;
import java.util.concurrent.TimeUnit;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.context.annotation.Scope;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import se.inera.intyg.webcert.web.service.underskrift.UnderskriftService;
import se.inera.intyg.webcert.web.service.underskrift.grp.dto.GrpCollectResponse;
import se.inera.intyg.webcert.web.service.underskrift.model.SignaturStatus;
import se.inera.intyg.webcert.web.service.underskrift.tracker.RedisTicketTracker;
import se.inera.intyg.webcert.web.service.user.dto.WebCertUser;

@Slf4j
@Component(value = "grpCollectPoller")
@Scope(value = "prototype")
@Profile("grp-rest-api")
public class GrpRestCollectPollerImpl implements GrpCollectPoller {

    @Value("${cgi.grp.polling.interval:2000}")
    private long pollingInterval;
    @Value("${cgi.grp.polling.timeout:240000}")
    private long grpPollingTimeout;

    private final RedisTicketTracker redisTicketTracker;
    private final UnderskriftService underskriftService;
    private final GrpRestClient grpRestClient;

    private String refId;
    private String transactionId;
    private SecurityContext securityContext;

    public static final String STATUS_FAILED = "FAILED";
    public static final String STATUS_PENDING = "PENDING";
    public static final String STATUS_COMPLETE = "COMPLETE";
    public static final String STATUS_CANCELLED = "CANCELLED";

    public GrpRestCollectPollerImpl(RedisTicketTracker redisTicketTracker, @Qualifier("signAggregator") UnderskriftService underskriftService,
        GrpRestClient grpRestClient) {
        this.redisTicketTracker = redisTicketTracker;
        this.underskriftService = underskriftService;
        this.grpRestClient = grpRestClient;
    }

    @Override
    public void run() {
        try {
            applySecurityContextToThreadLocal();
            final var webCertUser = (WebCertUser) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
            final var pollingTimeout = Instant.now().plus(Duration.ofMillis(grpPollingTimeout));
            sleepMilliseconds(pollingInterval);

            while (Instant.now().isBefore(pollingTimeout)) {
                final var collectResponse = grpRestClient.collect(refId, transactionId);

                if (collectResponse == null) {
                    return;
                }

                final var progressStatus = collectResponse.getProgressStatus();
                log.info("Grp collect response received for transactionId '{}' with progressStatus '{}', subStatus '{}' and message '{}'.",
                    transactionId, progressStatus.getStatus(), progressStatus.getSubstatus(), progressStatus.getMessage());

                switch (progressStatus.getStatus()) {
                    case STATUS_COMPLETE:
                        handleStatusComplete(collectResponse, webCertUser);
                        return;
                    case STATUS_PENDING:
                        redisTicketTracker.updateStatus(transactionId, SignaturStatus.VANTA_SIGN);
                        break;
                    case STATUS_FAILED:
                        redisTicketTracker.updateStatus(transactionId, SignaturStatus.OKAND);
                        return;
                    case STATUS_CANCELLED:
                        redisTicketTracker.updateStatus(transactionId, SignaturStatus.AVBRUTEN);
                        return;
                }

                sleepMilliseconds(pollingInterval);
            }

        } finally {
            SecurityContextHolder.clearContext();
        }
    }

    private void handleStatusComplete(GrpCollectResponse collectResponse, WebCertUser webCertUser) {
        final var personId = collectResponse.getUserInfo().getTin().replace("-", "");
        final var userId = webCertUser.getPersonId().replace("-", "");

        if (!personId.equals(userId)) {
            throw new IllegalStateException(
                "Grp sign processing aborted. PersonId from collect COMPLETE response does not match userId from the security context.");
        }

        final var signature = collectResponse.getValidationInfo().getSignature();
        underskriftService.grpSignature(transactionId, signature.getBytes(StandardCharsets.UTF_8));
        log.info("Signature was successfully persisted and ticket updated.");
    }

    private void applySecurityContextToThreadLocal() {
        if (securityContext == null) {
            throw new IllegalStateException("SecurityContext is not set. GRP poller thread cannot be started.");
        }
        SecurityContextHolder.setContext(securityContext);
    }

    private void sleepMilliseconds(long ms) {
        try {
            TimeUnit.MILLISECONDS.sleep(ms);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.warn("Sleep was interrupted during while polling Grp.", e);
        }
    }

    @Override
    public void setRefId(String refId) {
        this.refId = refId;
    }

    @Override
    public void setTransactionId(String transactionId) {
        this.transactionId = transactionId;
    }

    @Override
    public void setSecurityContext(SecurityContext securityContext) {
        this.securityContext = securityContext;
    }
}
