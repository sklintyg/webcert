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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import se.inera.intyg.common.support.model.common.internal.Utlatande;
import se.inera.intyg.common.support.modules.registry.IntygModuleRegistry;
import se.inera.intyg.common.support.modules.registry.ModuleNotFoundException;
import se.inera.intyg.common.support.modules.support.api.ModuleApi;
import se.inera.intyg.webcert.common.service.exception.WebCertServiceErrorCodeEnum;
import se.inera.intyg.webcert.common.service.exception.WebCertServiceException;
import se.inera.intyg.webcert.web.service.intyg.IntygService;
import se.inera.intyg.webcert.web.service.utkast.UtkastService;
import se.riv.clinicalprocess.healthcond.certificate.v3.Intyg;

@Service
public class GetCertificateServiceImpl implements GetCertificateService {

    private final IntygModuleRegistry intygModuleRegistry;
    private final UtkastService utkastService;
    private final IntygService intygService;

    private static final Logger LOG = LoggerFactory.getLogger(GetCertificateServiceImpl.class);

    @Autowired
    public GetCertificateServiceImpl(IntygModuleRegistry intygModuleRegistry, UtkastService utkastService, IntygService intygService) {
        this.intygModuleRegistry = intygModuleRegistry;
        this.utkastService = utkastService;
        this.intygService = intygService;
    }

    @Override
    public Intyg getCertificateAsIntyg(String certificateId, String certificateType) {
        final var utlatande = getCertificateFromWebcert(certificateId, certificateType);
        if (utlatande != null) {
            return getCertificateFromUtlatande(utlatande);
        }

        return getCertificateFromUtlatande(
            getCertificateFromIntygstjanst(certificateId, certificateType)
        );
    }

    @Override
    public Utlatande getCertificateAsUtlatande(String certificateId, String certificateType) {
        final var utlatande = getCertificateFromWebcert(certificateId, certificateType);
        if (utlatande != null) {
            return utlatande;
        }

        return getCertificateFromIntygstjanst(certificateId, certificateType);
    }

    private Utlatande getCertificateFromWebcert(String certificateId, String certificateType) {
        try {
            final var draft = utkastService.getDraft(certificateId, certificateType, false);
            final var moduleApi = getModuleApi(certificateType, draft.getIntygTypeVersion());
            return moduleApi.getUtlatandeFromJson(draft.getModel());
        } catch (Exception e) {
            LOG.info("Could not find certificate {} of type {} in webcert's database. Will check intygstjanst...", certificateId,
                certificateType, e);
            return null;
        }
    }

    private Utlatande getCertificateFromIntygstjanst(String certificateId, String certificateType) {
        try {
            final var certificateContentHolder = intygService.fetchIntygDataForInternalUse(certificateId, true);
            return certificateContentHolder.getUtlatande();
        } catch (Exception ex) {
            throw new WebCertServiceException(
                WebCertServiceErrorCodeEnum.DATA_NOT_FOUND,
                String.format("Could not find certificate id: %s of type %s in intygstjanst's database",
                    certificateId,
                    certificateType
                ),
                ex
            );
        }
    }

    private Intyg getCertificateFromUtlatande(Utlatande utlatande) {
        try {
            final var moduleApi = getModuleApi(utlatande.getTyp(), utlatande.getTextVersion());
            return moduleApi.getIntygFromUtlatande(utlatande);
        } catch (Exception ex) {
            throw new WebCertServiceException(
                WebCertServiceErrorCodeEnum.DATA_NOT_FOUND,
                String.format("Could not convert utlatande with certificate id '%s' of type '%s' and version '%s' to Intyg",
                    utlatande.getId(),
                    utlatande.getTyp(),
                    utlatande.getTextVersion()
                ),
                ex
            );
        }
    }

    private ModuleApi getModuleApi(String certificateType, String certificateVersion) throws ModuleNotFoundException {
        return intygModuleRegistry.getModuleApi(certificateType, certificateVersion);
    }
}
