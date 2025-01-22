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

import jakarta.persistence.OptimisticLockException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.stereotype.Service;
import se.inera.intyg.webcert.common.service.exception.WebCertServiceErrorCodeEnum;
import se.inera.intyg.webcert.common.service.exception.WebCertServiceException;
import se.inera.intyg.webcert.web.service.access.DraftAccessServiceHelper;
import se.inera.intyg.webcert.web.service.facade.CreateCertificateFromCandidateFacadeService;
import se.inera.intyg.webcert.web.service.facade.util.CandidateDataHelper;
import se.inera.intyg.webcert.web.service.monitoring.MonitoringLogService;
import se.inera.intyg.webcert.web.service.utkast.UtkastService;

@Service
public class CreateCertificateFromCandidateFacadeServiceImpl implements CreateCertificateFromCandidateFacadeService {

    private static final Logger LOG = LoggerFactory.getLogger(CreateCertificateFromCandidateFacadeServiceImpl.class);
    private final UtkastService utkastService;
    private final DraftAccessServiceHelper draftAccessServiceHelper;
    private final MonitoringLogService monitoringLogService;
    private final CandidateDataHelper candidateDataHelper;

    @Autowired
    public CreateCertificateFromCandidateFacadeServiceImpl(UtkastService utkastService, DraftAccessServiceHelper draftAccessServiceHelper,
        MonitoringLogService monitoringLogService, CandidateDataHelper candidateDataHelper) {
        this.utkastService = utkastService;
        this.draftAccessServiceHelper = draftAccessServiceHelper;
        this.monitoringLogService = monitoringLogService;
        this.candidateDataHelper = candidateDataHelper;
    }


    @Override
    public String createCertificateFromCandidate(String certificateId) {
        LOG.debug("Get certificate '{}' that will be used as template", certificateId);

        final var certificate = utkastService.getDraft(certificateId, false);
        final var candidateMetadata = candidateDataHelper.getCandidateMetadata(
            certificate.getIntygsTyp(), certificate.getIntygTypeVersion(),
            certificate.getPatientPersonnummer()
        ).get();
        var error = true;

        LOG.debug("Attempting to copy data from certificate with type '{}' and id '{}' to draft with type '{}' and id '{}'",
            candidateMetadata.getIntygType(), candidateMetadata.getIntygId(), certificate.getIntygsTyp(), certificateId);

        draftAccessServiceHelper.validateAllowToCopyFromCandidate(certificate);

        try {
            utkastService.updateDraftFromCandidate(candidateMetadata.getIntygId(), candidateMetadata.getIntygType(), certificate);
            if (certificate.getSkapadAv() != null) {
                monitoringLogService
                    .logUtkastCreatedTemplateAuto(certificateId, certificate.getIntygsTyp(), certificate.getSkapadAv().getHsaId(),
                        certificate.getEnhetsId(), candidateMetadata.getIntygId(), candidateMetadata.getIntygType());
            }
            error = false;
            return certificateId;

        } catch (OptimisticLockException | OptimisticLockingFailureException e) {
            monitoringLogService.logUtkastConcurrentlyEdited(certificateId, certificate.getIntygsTyp());
            throw new WebCertServiceException(WebCertServiceErrorCodeEnum.CONCURRENT_MODIFICATION, e.getMessage());
        } catch (WebCertServiceException e) {
            throw e;
        } catch (Exception e) {
            throw new WebCertServiceException(WebCertServiceErrorCodeEnum.MODULE_PROBLEM, e.getMessage());
        } finally {
            if (error) {
                LOG.error("Failed to copy data from certificate with type '{}' and id '{}' to draft with type '{}' and id '{}'.",
                    candidateMetadata.getIntygType(), candidateMetadata.getIntygId(), certificate.getIntygsTyp(), certificateId);
            }
        }
    }
}
