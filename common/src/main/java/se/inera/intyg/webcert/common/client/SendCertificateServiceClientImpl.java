/*
 * Copyright (C) 2016 Inera AB (http://www.inera.se)
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

import se.inera.intyg.common.support.model.common.internal.HoSPersonal;
import se.inera.intyg.webcert.common.client.converter.SendCertificateToRecipientTypeConverter;
import se.riv.clinicalprocess.healthcond.certificate.sendCertificateToRecipient.v1.*;

import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Exposes the SendCertificateToRecipient SOAP service.
 *
 * Created by eriklupander on 2015-06-03.
 */
@Component
public class SendCertificateServiceClientImpl implements SendCertificateServiceClient {

    @Autowired
    private SendCertificateToRecipientResponderInterface sendService;

    @Autowired
    private ObjectMapper objectMapper;

    @Override
    public SendCertificateToRecipientResponseType sendCertificate(String intygsId, String personId, String skickatAvJson, String recipient,
            String logicalAddress) {

        validateArgument(intygsId, "Cannot send certificate, argument 'intygsId' is null or empty.");
        validateArgument(personId, "Cannot send certificate, argument 'personId' is null or empty.");
        validateArgument(skickatAvJson, "Cannot send certificate, argument 'skickatAvJson' is null or empty.");
        validateArgument(recipient, "Cannot send certificate, argument 'recipient' is null or empty.");
        validateArgument(logicalAddress, "Cannot send certificate, argument 'logicalAddress' is null or empty.");

        HoSPersonal skickatAv = parseJson(skickatAvJson);

        SendCertificateToRecipientType request = SendCertificateToRecipientTypeConverter.convert(intygsId, personId, skickatAv, recipient);

        SendCertificateToRecipientResponseType response = sendService.sendCertificateToRecipient(logicalAddress, request);

        return response;
    }

    private void validateArgument(String arg, String msg) {
        if (arg == null || arg.trim().length() == 0) {
            throw new IllegalArgumentException(msg);
        }
    }

    private HoSPersonal parseJson(String skickatAvJson) {
        try {
            return objectMapper.readValue(skickatAvJson, HoSPersonal.class);
        } catch (Exception e) {
            throw new IllegalArgumentException("Cannot send certificate, argument 'skickatAvJson' is invalid: " + e.getMessage());
        }
    }
}
