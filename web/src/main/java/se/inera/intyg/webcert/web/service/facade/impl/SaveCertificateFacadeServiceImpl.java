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
package se.inera.intyg.webcert.web.service.facade.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import se.inera.intyg.common.support.facade.model.Certificate;
import se.inera.intyg.common.support.modules.registry.IntygModuleRegistry;
import se.inera.intyg.webcert.persistence.utkast.model.Utkast;
import se.inera.intyg.webcert.web.service.facade.SaveCertificateFacadeService;
import se.inera.intyg.webcert.web.service.utkast.UtkastService;

@Service("saveCertificateFacadeServiceWC")
public class SaveCertificateFacadeServiceImpl implements SaveCertificateFacadeService {

    private static final Logger LOG = LoggerFactory.getLogger(SaveCertificateFacadeServiceImpl.class);

    private final UtkastService utkastService;
    private final IntygModuleRegistry moduleRegistry;

    @Autowired
    public SaveCertificateFacadeServiceImpl(UtkastService utkastService, IntygModuleRegistry moduleRegistry) {
        this.utkastService = utkastService;
        this.moduleRegistry = moduleRegistry;
    }

    @Override
    public long saveCertificate(Certificate certificate, boolean pdlLog) {
        LOG.debug("Retrieve current certificate '{}' to update", certificate.getMetadata().getId());
        final Utkast currentCertificate = utkastService.getDraft(certificate.getMetadata().getId(), false);

        LOG.debug("Save certificate '{}' with version '{}'", certificate.getMetadata().getId(), certificate.getMetadata().getVersion());
        final var saveDraftResponse = utkastService.saveDraft(
            certificate.getMetadata().getId(),
            certificate.getMetadata().getVersion(),
            getJsonFromCertificate(certificate, currentCertificate.getModel()),
            pdlLog
        );

        LOG.debug("Return new version '{}' of save certificate '{}'", saveDraftResponse.getVersion(), certificate.getMetadata().getId());
        return saveDraftResponse.getVersion();
    }

    private String getJsonFromCertificate(Certificate certificate, String currentModel) {
        try {
            final var moduleApi = moduleRegistry.getModuleApi(
                certificate.getMetadata().getType(),
                certificate.getMetadata().getTypeVersion()
            );

            return moduleApi.getJsonFromCertificate(certificate, currentModel);
        } catch (Exception ex) {
            throw new IllegalStateException(ex);
        }
    }
}
