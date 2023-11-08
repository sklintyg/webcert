/*
 * Copyright (C) 2023 Inera AB (http://www.inera.se)
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
package se.inera.intyg.webcert.web.service.facade.internalapi.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import se.inera.intyg.common.support.model.UtkastStatus;
import se.inera.intyg.infra.intyginfo.dto.IntygInfoEventType;
import se.inera.intyg.infra.intyginfo.dto.ItIntygInfo;
import se.inera.intyg.webcert.common.service.exception.WebCertServiceErrorCodeEnum;
import se.inera.intyg.webcert.common.service.exception.WebCertServiceException;
import se.inera.intyg.webcert.persistence.utkast.model.Utkast;
import se.inera.intyg.webcert.web.integration.ITIntegrationService;
import se.inera.intyg.webcert.web.service.access.DraftAccessServiceHelper;
import se.inera.intyg.webcert.web.service.facade.CertificateTextVersionFacadeService;
import se.inera.intyg.webcert.web.service.intyg.IntygService;
import se.inera.intyg.webcert.web.service.utkast.UtkastService;
import se.inera.intyg.webcert.web.web.controller.internalapi.dto.GetUtkastResponse;

@Service
public class GetUtkastFacadeService {

    private static final Logger LOG = LoggerFactory.getLogger(GetUtkastFacadeService.class);
    private static final String INTYGSMOTTAGARE = "intygsmottagare";

    private final UtkastService utkastService;

    private final IntygService intygService;

    private final DraftAccessServiceHelper draftAccessServiceHelper;

    private final ITIntegrationService itIntegrationService;

    private final CertificateTextVersionFacadeService certificateTextVersionFacadeService;

    @Autowired
    public GetUtkastFacadeService(UtkastService utkastService,
        IntygService intygService,
        DraftAccessServiceHelper draftAccessServiceHelper, ITIntegrationService itIntegrationService,
        CertificateTextVersionFacadeService certificateTextVersionFacadeService) {
        this.utkastService = utkastService;
        this.intygService = intygService;
        this.draftAccessServiceHelper = draftAccessServiceHelper;
        this.itIntegrationService = itIntegrationService;
        this.certificateTextVersionFacadeService = certificateTextVersionFacadeService;
    }

    public GetUtkastResponse get(String certificateId, boolean pdlLog, boolean validateAccess) {
        final var utkast = getCertificateFromWebcert(certificateId, pdlLog, validateAccess);
        if (utkast == null) {
            LOG.debug("Retrieving Intyg '{}' from IntygService with pdlLog argument as '{}'", certificateId, pdlLog);
            final var intygContentHolder = intygService.fetchIntygData(certificateId, null, pdlLog, validateAccess);
            return GetUtkastResponse.create(intygContentHolder);
        }

        if (isSignedButNotSent(utkast)) {
            LOG.debug("Retrieve certificate info for '{}' from Intygstjansten", certificateId);
            final var certificateInfo = itIntegrationService.getCertificateInfo(certificateId);
            utkast.setSkickadTillMottagareDatum(certificateInfo.getSentToRecipient());
            utkast.setSkickadTillMottagare(getRecipient(certificateInfo));
        }
        return GetUtkastResponse.create(utkast);
    }

    private boolean isSignedButNotSent(Utkast utkast) {
        return utkast.getStatus() == UtkastStatus.SIGNED && utkast.getSkickadTillMottagareDatum() == null;
    }

    private String getRecipient(ItIntygInfo certificateInfo) {
        return (String) certificateInfo.getEvents().stream()
            .filter(intygInfoEvent -> intygInfoEvent.getType() == IntygInfoEventType.IS006)
            .findFirst()
            .map(intygInfoEvent -> intygInfoEvent.getData().get(INTYGSMOTTAGARE))
            .orElse(null);
    }

    private Utkast getCertificateFromWebcert(String certificateId, boolean pdlLog, boolean validateAccess) {
        try {
            LOG.debug("Retrieving Utkast '{}' from UtkastService with pdlLog argument as '{}'", certificateId, pdlLog);
            final var utkast = utkastService.getDraft(certificateId, pdlLog);
            if (validateAccess) {
                draftAccessServiceHelper.validateAllowToReadUtkast(utkast);
            }
            return certificateTextVersionFacadeService.upgradeToLatestMinorTextVersion(utkast);
        } catch (WebCertServiceException ex) {
            if (ex.getErrorCode().equals(WebCertServiceErrorCodeEnum.DATA_NOT_FOUND)) {
                LOG.debug("Utkast with id '{}' doesn't exist in Webcert", certificateId);
                return null;
            }
            throw ex;
        }
    }
}
