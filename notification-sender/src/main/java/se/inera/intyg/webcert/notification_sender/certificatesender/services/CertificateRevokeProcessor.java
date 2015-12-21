/*
 * Copyright (C) 2015 Inera AB (http://www.inera.se)
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

import static com.google.common.base.Preconditions.checkArgument;

import javax.xml.ws.WebServiceException;

import org.apache.camel.Body;
import org.apache.camel.Header;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import se.inera.ifv.insuranceprocess.healthreporting.revokemedicalcertificateresponder.v1.RevokeMedicalCertificateResponseType;
import se.inera.ifv.insuranceprocess.healthreporting.v2.ResultOfCall;
import se.inera.intyg.webcert.notification_sender.exception.PermanentException;
import se.inera.intyg.webcert.notification_sender.exception.TemporaryException;
import se.inera.intyg.webcert.common.common.Constants;
import se.inera.intyg.webcert.common.client.RevokeCertificateServiceClient;

/**
 * Created by eriklupander on 2015-05-21.
 */
public class CertificateRevokeProcessor {

    private static final Logger LOG = LoggerFactory.getLogger(CertificateSendProcessor.class);

    @Autowired
    private RevokeCertificateServiceClient revokeServiceClient;

    public void process(@Body String xmlBody, @Header(Constants.INTYGS_ID) String intygsId, @Header(Constants.LOGICAL_ADDRESS) String logicalAddress) throws Exception {

        checkArgument(!nullOrEmpty(intygsId), "Message of type %s does not have a %s header.", Constants.REVOKE_MESSAGE, Constants.INTYGS_ID);

        try {
            // Revoke the certificate
            RevokeMedicalCertificateResponseType response = revokeServiceClient.revokeCertificate(xmlBody, logicalAddress);

            // Take care of the response
            ResultOfCall resultOfCall = response.getResult();

            switch (resultOfCall.getResultCode()) {
                case OK:

                case INFO:
                    return;
                case ERROR:
                    switch (resultOfCall.getErrorId()) {
                        case APPLICATION_ERROR:
                        case TECHNICAL_ERROR:
                            LOG.error("Call to revoke intyg {} caused an error: {}, ErrorId: {}. Rethrowing as TemporaryException",
                                    intygsId, resultOfCall.getErrorText(), resultOfCall.getErrorId());
                            throw new TemporaryException(resultOfCall.getErrorText());
                        case TRANSFORMATION_ERROR:
                        case VALIDATION_ERROR:
                            LOG.error("Call to revoke intyg {} caused an error: {}, ErrorId: {}. Rethrowing as PermanentException",
                                    intygsId, resultOfCall.getErrorText(), resultOfCall.getErrorId());
                            throw new PermanentException(resultOfCall.getErrorText());
                        default:
                            throw new TemporaryException(resultOfCall.getErrorText());
                    }
                default:
                    throw new TemporaryException(resultOfCall.getErrorText());
            }

        } catch (WebServiceException e) {
            LOG.error("Call to revoke intyg {} caused an error: {}. Will retry",
                    intygsId, e.getMessage());
            throw new TemporaryException(e.getMessage());
        }
    }

    private boolean nullOrEmpty(String str) {
        return str == null || str.trim().length() == 0;
    }

}
