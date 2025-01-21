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
package se.inera.intyg.webcert.web.integration;

import java.util.List;
import java.util.Set;
import se.inera.intyg.infra.certificate.dto.CertificateListResponse;
import se.inera.intyg.infra.intyginfo.dto.ItIntygInfo;
import se.inera.intyg.infra.message.dto.MessageFromIT;
import se.inera.intyg.webcert.web.web.controller.api.dto.QueryIntygParameter;

/**
 * Service to use when calling Intygstjanstens internal Rest APIs.
 */
public interface ITIntegrationService {

    /**
     * Get all messages for a certificate.
     *
     * @param certificateId Id of certificate
     * @return List of messages. If no messages exists for the certificate, then list will be empty.
     */
    List<MessageFromIT> findMessagesByCertificateId(String certificateId);

    /**
     * Get information about a certificate from intygstjänsten.
     *
     * @param certificateId Id of certificate
     * @return Info about the certificate. If the certificate doesn't exist the ItIntygInfo is empty.
     */
    ItIntygInfo getCertificateInfo(String certificateId);

    /**
     * Get all signed certificates for a specific doctor.
     *
     * @param queryParam Filter query including parameters to filter certificates.
     * @return response including a list of certificates and the total amount of certificates.
     */
    CertificateListResponse getCertificatesForDoctor(QueryIntygParameter queryParam, Set<String> types);
}
