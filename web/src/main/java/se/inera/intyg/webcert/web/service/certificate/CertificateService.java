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
package se.inera.intyg.webcert.web.service.certificate;

import se.inera.intyg.infra.certificate.dto.CertificateListResponse;
import se.inera.intyg.webcert.web.web.controller.api.dto.QueryIntygParameter;

/**
 * Service that retrieves certificates.
 */
public interface CertificateService {

    /**
     * Retrieves certificates for a doctor on the logged in unit.
     *
     * @param queryParameter Parameters that certificates should be filtered according on.
     * @return response including a list of certificates and the total amount of certificates.
     */
    CertificateListResponse listCertificatesForDoctor(QueryIntygParameter queryParameter);
}
