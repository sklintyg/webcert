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

package se.inera.intyg.webcert.web.service.facade.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import se.inera.intyg.common.support.facade.model.Certificate;
import se.inera.intyg.webcert.persistence.utkast.model.Utkast;
import se.inera.intyg.webcert.web.service.facade.GetCertificateFacadeService;
import se.inera.intyg.webcert.web.service.facade.util.CertificateConverter;
import se.inera.intyg.webcert.web.service.utkast.UtkastService;
import se.inera.intyg.webcert.web.web.util.resourcelinks.ResourceLinkHelper;

@Service
public class GetCertificateServiceImpl implements GetCertificateFacadeService {

    private static final Logger LOG = LoggerFactory.getLogger(GetCertificateServiceImpl.class);

    private final UtkastService utkastService;

    private final ResourceLinkHelper resourceLinkHelper;

    private final CertificateConverter certificateConverter;

    @Autowired
    public GetCertificateServiceImpl(UtkastService utkastService, ResourceLinkHelper resourceLinkHelper,
        CertificateConverter certificateConverter) {
        this.utkastService = utkastService;
        this.resourceLinkHelper = resourceLinkHelper;
        this.certificateConverter = certificateConverter;
    }

    @Override
    public Certificate getCertificate(String certificateId, boolean pdlLog) {
        LOG.debug("Retrieving Utkast '{}' from UtkastService with pdlLog argument as '{}'", certificateId, pdlLog);
        final Utkast utkast = utkastService.getDraft(certificateId, pdlLog);

        LOG.debug("Converting Utkast to Certificate");
        final var certificate = certificateConverter.convert(utkast);

        LOG.debug("Decorating certificate with resource links");
        resourceLinkHelper.decorateCertificateWithValidActionLinks(certificate);

        return certificate;
    }
}
