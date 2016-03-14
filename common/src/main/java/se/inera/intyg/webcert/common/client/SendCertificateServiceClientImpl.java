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

import se.riv.clinicalprocess.healthcond.certificate.sendCertificateToRecipient.v1.*;
import se.riv.clinicalprocess.healthcond.certificate.types.v2.*;

/**
 * Exposes the SendCertificateToRecipient SOAP service.
 *
 * Created by eriklupander on 2015-06-03.
 */
@Component
public class SendCertificateServiceClientImpl implements SendCertificateServiceClient {

    private static final String PERSON_ID_ROOT = "1.2.752.129.2.1.3.1";
    private static final String MOTTAGARE_CODE_SYSTEM = "769bb12b-bd9f-4203-a5cd-fd14f2eb3b80";

    @Autowired
    private SendCertificateToRecipientResponderInterface sendService;

    @Override
    public SendCertificateToRecipientResponseType sendCertificate(String intygsId, String personId, String recipient, String logicalAddress) {

        validateArgument(intygsId, "Cannot send certificate, argument 'intygsId' is null or empty.");
        validateArgument(personId, "Cannot send certificate, argument 'personId' is null or empty.");
        validateArgument(recipient, "Cannot send certificate, argument 'recipient' is null or empty.");
        validateArgument(logicalAddress, "Cannot send certificate, argument 'logicalAddress' is null or empty.");

        SendCertificateToRecipientType request = new SendCertificateToRecipientType();
        PersonId patientPersonId = new PersonId();
        patientPersonId.setRoot(PERSON_ID_ROOT);
        patientPersonId.setExtension(personId);
        request.setPatientPersonId(patientPersonId);
        IntygId intygId = new IntygId();
        intygId.setRoot("SE5565594230-B31");  // IT:s root since unit hsaId is not available
        intygId.setExtension(intygsId);
        request.setIntygsId(intygId);
        Part part = new Part();
        part.setCode(recipient);
        part.setCodeSystem(MOTTAGARE_CODE_SYSTEM);
        request.setMottagare(part);

        SendCertificateToRecipientResponseType response = sendService.sendCertificateToRecipient(logicalAddress, request);

        return response;
    }

    private void validateArgument(String arg, String msg) {
        if (arg == null || arg.trim().length() == 0) {
            throw new IllegalArgumentException(msg);
        }
    }
}
