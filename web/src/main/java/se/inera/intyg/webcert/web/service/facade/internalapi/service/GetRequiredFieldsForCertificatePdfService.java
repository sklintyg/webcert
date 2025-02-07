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
package se.inera.intyg.webcert.web.service.facade.internalapi.service;

import java.util.Objects;
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
import se.inera.intyg.webcert.web.converter.util.IntygConverterUtil;
import se.inera.intyg.webcert.web.integration.ITIntegrationService;
import se.inera.intyg.webcert.web.service.intyg.IntygService;
import se.inera.intyg.webcert.web.service.intyg.dto.IntygContentHolder;
import se.inera.intyg.webcert.web.service.utkast.UtkastService;
import se.inera.intyg.webcert.web.web.controller.internalapi.dto.RequiredFieldsForCertificatePdf;

@Service
public class GetRequiredFieldsForCertificatePdfService {

    private static final Logger LOG = LoggerFactory.getLogger(GetRequiredFieldsForCertificatePdfService.class);
    private static final String INTYGSMOTTAGARE = "intygsmottagare";
    private final UtkastService utkastService;
    private final IntygService intygService;
    private final ITIntegrationService itIntegrationService;
    private static final boolean SHOULD_NOT_PDL_LOG = false;
    private static final boolean SHOULD_NOT_VALIDATE_ACCESS = false;


    @Autowired
    public GetRequiredFieldsForCertificatePdfService(UtkastService utkastService,
        IntygService intygService, ITIntegrationService itIntegrationService) {
        this.utkastService = utkastService;
        this.intygService = intygService;
        this.itIntegrationService = itIntegrationService;
    }

    public RequiredFieldsForCertificatePdf get(String certificateId) {
        final var draft = getCertificateFromWebcert(certificateId, SHOULD_NOT_PDL_LOG);
        if (draft == null) {
            LOG.debug("Retrieving Intyg '{}' from IntygService with pdlLog argument as '{}' and validateAccess as '{}'", certificateId,
                SHOULD_NOT_PDL_LOG, SHOULD_NOT_VALIDATE_ACCESS);
            final var intygContentHolder = intygService.fetchIntygData(
                certificateId,
                null,
                SHOULD_NOT_PDL_LOG,
                SHOULD_NOT_VALIDATE_ACCESS
            );
            return createRequiredFieldsFromIntygContentHolder(intygContentHolder);
        }

        if (isSignedButNotSent(draft)) {
            LOG.debug("Retrieve certificate info for '{}' from Intygstjansten", certificateId);
            final var certificateInfo = itIntegrationService.getCertificateInfo(certificateId);
            draft.setSkickadTillMottagareDatum(certificateInfo.getSentToRecipient());
            draft.setSkickadTillMottagare(getRecipient(certificateInfo));
        }

        return createRequiredFieldsFromDraft(draft);
    }

    private static RequiredFieldsForCertificatePdf createRequiredFieldsFromDraft(Utkast draft) {
        return RequiredFieldsForCertificatePdf.create(
            draft.getIntygTypeVersion(),
            draft.getIntygsTyp(),
            draft.getModel(),
            IntygConverterUtil.buildStatusesFromUtkast(draft),
            draft.getStatus()
        );
    }

    private static RequiredFieldsForCertificatePdf createRequiredFieldsFromIntygContentHolder(IntygContentHolder intygContentHolder) {
        return RequiredFieldsForCertificatePdf.create(
            Objects.requireNonNull(intygContentHolder.getUtlatande()).getTextVersion(),
            intygContentHolder.getUtlatande().getTyp(),
            intygContentHolder.getContents(),
            intygContentHolder.getStatuses(),
            UtkastStatus.SIGNED
        );
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

    private Utkast getCertificateFromWebcert(String certificateId, boolean pdlLog) {
        try {
            LOG.debug("Retrieving Utkast '{}' from UtkastService with pdlLog argument as '{}'", certificateId, pdlLog);
            return utkastService.getDraft(certificateId, pdlLog);
        } catch (WebCertServiceException ex) {
            if (ex.getErrorCode().equals(WebCertServiceErrorCodeEnum.DATA_NOT_FOUND)) {
                LOG.debug("Utkast with id '{}' doesn't exist in Webcert", certificateId);
                return null;
            }
            throw ex;
        }
    }
}
