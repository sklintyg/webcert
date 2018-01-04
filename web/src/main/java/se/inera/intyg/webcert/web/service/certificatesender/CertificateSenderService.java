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
package se.inera.intyg.webcert.web.service.certificatesender;

import se.inera.intyg.schemas.contract.Personnummer;

/**
 * Created by eriklupander on 2015-05-20.
 */
public interface CertificateSenderService {

    void storeCertificate(String intygsId, String intygsTyp, String jsonBody) throws CertificateSenderException;

    void sendCertificate(String intygsId, Personnummer personId, String jsonBody, String recipientId) throws CertificateSenderException;

    /**
     * See {@link CertificateSenderService#sendCertificate(String, Personnummer, String, String)}
     *
     * This version adds the possiblity to specify that the processing of the message shall be delayed by the externally
     * configured number of milliseconds. This is very useful when we're first calling storeCertificate and then
     * sendCertificate in the same operation.
     */
    void sendCertificate(String intygsId, Personnummer personId, String jsonBody, String recipientId, boolean delay)
            throws CertificateSenderException;

    void revokeCertificate(String intygsId, String xmlBody, String intygsTyp) throws CertificateSenderException;

    void sendMessageToRecipient(String intygsId, String xmlBody) throws CertificateSenderException;
}
