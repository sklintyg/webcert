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
package se.inera.intyg.webcert.web.service.signatur;

import se.inera.intyg.infra.xmldsig.model.SignatureType;
import se.inera.intyg.webcert.web.service.user.dto.WebCertUser;
import se.inera.intyg.webcert.web.service.signatur.dto.SignaturTicket;

public interface SignaturService {

    /**
     * This method is used when signing using other methods than NetId.
     *
     * @param intygId intygid
     * @param version version
     * @return SignatureTicket
     */
    SignaturTicket serverSignature(String intygId, long version);

    /**
     * This method is used when signing using NetId.
     */
    SignaturTicket clientSignature(String biljettId, String rawSignatur);

    /**
     * This method is used by the GRP collect mechanism implemented in {se.inera.intyg.webcert.web.service.signatur.grp.GrpPoller}
     * to finalize signing operations after GRP collect has completed with success status.
     *
     * Since this doesn't run in the context of a HTTP request there is no Principal to fetch from the executing
     * ThreadLocal, hence the webCertUser parameter.
     */
    SignaturTicket clientGrpSignature(String biljettId, String rawSignatur, WebCertUser webCertUser);

    /**
     * Checks the status of the signing ticket.
     *
     * @return The signing ticket corresponding to the supplied biljettId or a blank ticket no ticket was found.
     */
    SignaturTicket ticketStatus(String biljettId);

    /**
     * This method is used to generate a signing ticket based on the payload of an Intyg.
     *
     * @param intygId The id of the draft to generate signing ticket for
     * @param version version
     */
    SignaturTicket createDraftHash(String intygId, long version);

    /**
     * This method is used by the NIAS collect mechanism to finalize NetID Access Server XMLDSig signatures.
     *
     * @param transactionId
     * @param signatureType
     * @param certificate
     *@param webCertUser  @return
     */
    SignaturTicket clientNiasSignature(String transactionId, SignatureType signatureType, String certificate, WebCertUser webCertUser);
}
