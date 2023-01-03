/*
 * Copyright (C) 2023 Inera AB (http://www.inera.se)
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
package se.inera.intyg.webcert.notification_sender.certificatesender.services;

import javax.xml.ws.WebServiceException;
import org.apache.camel.Body;
import org.apache.camel.Header;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import se.inera.intyg.webcert.common.Constants;
import se.inera.intyg.webcert.common.client.SendCertificateServiceClient;
import se.inera.intyg.webcert.common.sender.exception.TemporaryException;
import se.riv.clinicalprocess.healthcond.certificate.sendCertificateToRecipient.v2.SendCertificateToRecipientResponseType;
import se.riv.clinicalprocess.healthcond.certificate.v3.ResultCodeType;
import se.riv.clinicalprocess.healthcond.certificate.v3.ResultType;

/**
 * Created by eriklupander on 2015-05-21.
 */
public class CertificateSendProcessor {

    private static final Logger LOG = LoggerFactory.getLogger(CertificateSendProcessor.class);

    @Autowired
    private SendCertificateServiceClient sendServiceClient;

    public void process(@Body String skickatAv,
        @Header(Constants.INTYGS_ID) String intygsId,
        @Header(Constants.PERSON_ID) String personId,
        @Header(Constants.RECIPIENT) String recipient,
        @Header(Constants.LOGICAL_ADDRESS) String logicalAddress) throws TemporaryException {

        SendCertificateToRecipientResponseType response;
        try {
            response = sendServiceClient.sendCertificate(intygsId, personId, skickatAv, recipient, logicalAddress);

            final ResultType result = response.getResult();
            final String resultText = result.getResultText();
            if (ResultCodeType.ERROR == result.getResultCode()) {
                LOG.warn("Error occured when trying to send intyg '{}'; {}", intygsId, resultText);
                throw new TemporaryException(resultText);
            } else {
                if (ResultCodeType.INFO.equals(result.getResultCode())) {
                    LOG.warn("Warning occured when trying to send intyg '{}'; {}. Will not requeue.", intygsId, resultText);
                }
            }

        } catch (WebServiceException e) {
            LOG.warn("Call to send intyg {} caused an error: {}. Will retry", intygsId, e.getMessage());
            throw new TemporaryException(e.getMessage());
        }
    }
}
