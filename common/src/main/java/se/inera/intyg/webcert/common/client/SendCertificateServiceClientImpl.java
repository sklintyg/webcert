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

package se.inera.intyg.webcert.common.client;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import se.inera.intyg.clinicalprocess.healthcond.certificate.sendcertificatetorecipient.v1.SendCertificateToRecipientResponderInterface;
import se.inera.intyg.clinicalprocess.healthcond.certificate.sendcertificatetorecipient.v1.SendCertificateToRecipientResponseType;
import se.inera.intyg.clinicalprocess.healthcond.certificate.sendcertificatetorecipient.v1.SendCertificateToRecipientType;

/**
 * Exposes the SendCertificateToRecipient SOAP service.
 *
 * Created by eriklupander on 2015-06-03.
 */
@Component
public class SendCertificateServiceClientImpl implements SendCertificateServiceClient {

    @Autowired
    private SendCertificateToRecipientResponderInterface sendService;

    @Override
    public SendCertificateToRecipientResponseType sendCertificate(String intygsId, String personId, String recipient, String logicalAddress) {

        validateArgument(intygsId, "Cannot send certificate, argument 'intygsId' is null or empty.");
        validateArgument(personId, "Cannot send certificate, argument 'personId' is null or empty.");
        validateArgument(recipient, "Cannot send certificate, argument 'recipient' is null or empty.");
        validateArgument(logicalAddress, "Cannot send certificate, argument 'logicalAddress' is null or empty.");

        SendCertificateToRecipientType request = new SendCertificateToRecipientType();
        request.setUtlatandeId(intygsId);
        request.setPersonId(personId);
        request.setMottagareId(recipient);

        SendCertificateToRecipientResponseType response = sendService.sendCertificateToRecipient(logicalAddress, request);

        return response;
    }

    private void validateArgument(String arg, String msg) {
        if (arg == null || arg.trim().length() == 0) {
            throw new IllegalArgumentException(msg);
        }
    }
}
