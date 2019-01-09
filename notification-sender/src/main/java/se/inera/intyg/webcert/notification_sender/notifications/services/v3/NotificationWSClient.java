/*
 * Copyright (C) 2019 Inera AB (http://www.inera.se)
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
package se.inera.intyg.webcert.notification_sender.notifications.services.v3;

// CHECKSTYLE:OFF LineLength
import org.apache.camel.Header;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import se.inera.intyg.common.support.common.enumerations.HandelsekodEnum;
import se.inera.intyg.infra.security.authorities.FeaturesHelper;
import se.inera.intyg.infra.security.common.model.AuthoritiesConstants;
import se.inera.intyg.webcert.common.sender.exception.PermanentException;
import se.inera.intyg.webcert.common.sender.exception.DiscardCandidateException;
import se.inera.intyg.webcert.common.sender.exception.TemporaryException;
import se.inera.intyg.webcert.notification_sender.notifications.routes.NotificationRouteHeaders;
import se.riv.clinicalprocess.healthcond.certificate.certificatestatusupdateforcareresponder.v3.CertificateStatusUpdateForCareResponderInterface;
import se.riv.clinicalprocess.healthcond.certificate.certificatestatusupdateforcareresponder.v3.CertificateStatusUpdateForCareResponseType;
import se.riv.clinicalprocess.healthcond.certificate.certificatestatusupdateforcareresponder.v3.CertificateStatusUpdateForCareType;
import se.riv.clinicalprocess.healthcond.certificate.v3.ErrorIdType;
import se.riv.clinicalprocess.healthcond.certificate.v3.ResultType;
// CHECKSTYLE:ON LineLength

public class NotificationWSClient {

    private static final Logger LOG = LoggerFactory.getLogger(NotificationWSClient.class);

    @Autowired
    private CertificateStatusUpdateForCareResponderInterface statusUpdateForCareClient;

    @Autowired
    private FeaturesHelper featuresHelper;

    public void sendStatusUpdate(CertificateStatusUpdateForCareType request,
            @Header(NotificationRouteHeaders.LOGISK_ADRESS) String logicalAddress)
            throws TemporaryException, DiscardCandidateException, PermanentException {

        LOG.debug("Sending status update with version 2 to '{}' for intyg '{}'", logicalAddress,
                request.getIntyg().getIntygsId().getExtension());

        CertificateStatusUpdateForCareResponseType response = null;

        try {
            response = statusUpdateForCareClient.certificateStatusUpdateForCare(logicalAddress, request);
        } catch (Exception e) {
            LOG.warn("Exception occured when sending status update: {}", e.getMessage());
            throw new TemporaryException(e);
        }

        ResultType result = response.getResult();
        switch (result.getResultCode()) {
        case ERROR:
            if (ErrorIdType.TECHNICAL_ERROR.equals(result.getErrorId())) {
                // Added ugly null check to make notification_sender testSendStatusUpdateErrorTechnical pass
                // The featuresHelper does not seem to load properly in the gradle tests
                if (featuresHelper != null && featuresHelper.isFeatureActive(AuthoritiesConstants.FEATURE_NOTIFICATION_DISCARD_FELB)) {
                    if (result.getResultText()
                            .startsWith("Certificate not found in COSMIC and ref field is missing, cannot store certificate. "
                            + "Possible race condition. Retry later when the certificate may have been stored in COSMIC.")
                            && (request.getHandelse().getHandelsekod().getCode().equals(HandelsekodEnum.ANDRAT.value())
                            || request.getHandelse().getHandelsekod().getCode().equals(HandelsekodEnum.SKAPAT.value()))) {
                        throw new DiscardCandidateException(
                                String.format("NotificationWSClient caught COSMIC-typB with error code: %s and message %s",
                                result.getErrorId(),
                                result.getResultText()));
                    }
                }
                throw new TemporaryException(String.format("NotificationWSClient failed with error code: %s and message %s",
                        result.getErrorId(),
                        result.getResultText()));
            } else {
                throw new PermanentException(String.format("NotificationWSClient failed with non-recoverable error code: %s and message %s",
                        result.getErrorId(),
                        result.getResultText()));
            }
        case INFO:
            LOG.info("NotificationWSClient got message:" + result.getResultText());
            break;
        case OK:
            break;
        }

    }
}
