/*
 * Copyright (C) 2021 Inera AB (http://www.inera.se)
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

import java.io.IOException;
import se.inera.intyg.common.support.modules.registry.ModuleNotFoundException;
import se.inera.intyg.common.support.modules.support.api.exception.ModuleException;
import se.inera.intyg.infra.certificate.dto.CertificateListResponse;
import se.inera.intyg.webcert.web.web.controller.api.dto.QueryIntygParameter;
import se.riv.clinicalprocess.healthcond.certificate.v3.Intyg;

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


    Intyg getCertificate(String certificateId, String certificateType, String certificateVersion)
        throws ModuleNotFoundException, ModuleException, IOException;
}
