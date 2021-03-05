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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import se.inera.intyg.common.support.modules.registry.IntygModuleRegistry;
import se.inera.intyg.common.support.modules.registry.ModuleNotFoundException;
import se.inera.intyg.common.support.modules.support.api.ModuleApi;
import se.inera.intyg.common.support.modules.support.api.exception.ModuleException;
import se.inera.intyg.webcert.common.service.exception.WebCertServiceErrorCodeEnum;
import se.inera.intyg.webcert.common.service.exception.WebCertServiceException;
import se.inera.intyg.webcert.web.service.intyg.IntygService;
import se.inera.intyg.webcert.web.service.utkast.UtkastService;
import se.riv.clinicalprocess.healthcond.certificate.v3.Intyg;

@Service
public class GetCertificateService {

    private IntygModuleRegistry intygModuleRegistry;
    private UtkastService utkastService;
    private IntygService intygService;

    private static final Logger LOG = LoggerFactory.getLogger(GetCertificateService.class);

    @Autowired
    public GetCertificateService(IntygModuleRegistry intygModuleRegistry, UtkastService utkastService, IntygService intygService) {
        this.intygModuleRegistry = intygModuleRegistry;
        this.utkastService = utkastService;
        this.intygService = intygService;
    }

    /**
     * Get Certificate from Webcert and if it doesn't exist it retrieves it from Intygstjänst.
     */
    public Intyg getCertificate(String certificateId, String certificateType, String certificateVersion)
        throws ModuleNotFoundException, ModuleException, IOException {
        final var certificate = getCertificateFromWebcert(certificateId, certificateType, certificateVersion);
        if (certificate != null) {
            return certificate;
        }
        return getCertificateFromIntygstjanst(certificateId, certificateType, certificateVersion);
    }

    private Intyg getCertificateFromWebcert(String certificateId, String certificateType, String certificateVersion)
        throws ModuleNotFoundException, ModuleException, IOException {
        try {
            final var draft = utkastService.getDraft(certificateId, intygModuleRegistry.getModuleIdFromExternalId(certificateType), false);
            final var moduleApi = getModuleApi(certificateType, certificateVersion);
            final var utlatande = moduleApi.getUtlatandeFromJson(draft.getModel());
            return moduleApi.getIntygFromUtlatande(utlatande);
        } catch (WebCertServiceException e) {
            LOG.info("Could not find certificate {} of type {} in webcert's database. Will check intygstjanst...", certificateId,
                certificateType, e);
            return null;
        }
    }

    private Intyg getCertificateFromIntygstjanst(String certificateId, String certificateType, String certificateVersion)
        throws ModuleNotFoundException, ModuleException, WebCertServiceException {
        try {
            final var certificateContentHolder = intygService.fetchIntygDataForInternalUse(certificateId, true);
            final var moduleApi = getModuleApi(certificateType, certificateVersion);
            final var utlatande = certificateContentHolder.getUtlatande();
            return moduleApi.getIntygFromUtlatande(utlatande);
        } catch (WebCertServiceException e) {
            throw new WebCertServiceException(WebCertServiceErrorCodeEnum.DATA_NOT_FOUND,
                String.format("Could not find certificate id: %s of type %s in intygstjanst's database", certificateId,
                    certificateType), e);
        }
    }

    private ModuleApi getModuleApi(String certificateType, String certificateVersion) throws ModuleNotFoundException {
        return intygModuleRegistry.getModuleApi(intygModuleRegistry.getModuleIdFromExternalId(certificateType), certificateVersion);
    }
}
