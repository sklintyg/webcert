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

import java.util.Optional;
import javax.persistence.OptimisticLockException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.stereotype.Service;
import se.inera.intyg.common.support.model.common.internal.Patient;
import se.inera.intyg.common.support.modules.registry.IntygModuleRegistry;
import se.inera.intyg.common.support.modules.registry.ModuleNotFoundException;
import se.inera.intyg.common.support.modules.support.api.ModuleApi;
import se.inera.intyg.webcert.common.service.exception.WebCertServiceErrorCodeEnum;
import se.inera.intyg.webcert.common.service.exception.WebCertServiceException;
import se.inera.intyg.webcert.persistence.utkast.model.Utkast;
import se.inera.intyg.webcert.web.service.access.DraftAccessServiceHelper;
import se.inera.intyg.webcert.web.service.facade.CreateCertificateFromCandidateFacadeService;
import se.inera.intyg.webcert.web.service.monitoring.MonitoringLogService;
import se.inera.intyg.webcert.web.service.patient.PatientDetailsResolver;
import se.inera.intyg.webcert.web.service.utkast.UtkastCandidateServiceImpl;
import se.inera.intyg.webcert.web.service.utkast.UtkastService;
import se.inera.intyg.webcert.web.service.utkast.dto.UtkastCandidateMetaData;

@Service
public class CreateCertificateFromCandidateFacadeServiceImpl implements CreateCertificateFromCandidateFacadeService {

    private static final Logger LOG = LoggerFactory.getLogger(CreateCertificateFromCandidateFacadeServiceImpl.class);
    private final UtkastService utkastService;
    private final DraftAccessServiceHelper draftAccessServiceHelper;
    private final MonitoringLogService monitoringLogService;
    private final UtkastCandidateServiceImpl utkastCandidateService;
    private final IntygModuleRegistry moduleRegistry;
    private final PatientDetailsResolver patientDetailsResolver;

    @Autowired
    public CreateCertificateFromCandidateFacadeServiceImpl(UtkastService utkastService, DraftAccessServiceHelper draftAccessServiceHelper,
        MonitoringLogService monitoringLogService,
        UtkastCandidateServiceImpl utkastCandidateService,
        IntygModuleRegistry moduleRegistry, PatientDetailsResolver patientDetailsResolver) {
        this.utkastService = utkastService;
        this.draftAccessServiceHelper = draftAccessServiceHelper;
        this.monitoringLogService = monitoringLogService;
        this.utkastCandidateService = utkastCandidateService;
        this.moduleRegistry = moduleRegistry;
        this.patientDetailsResolver = patientDetailsResolver;
    }


    @Override
    public String createCertificateFromCandidate(String certificateId) {
        LOG.debug("Get certificate '{}' that will be used as template", certificateId);

        final var certificate = utkastService.getDraft(certificateId, false);
        final var candidateId = getCandidateId(certificate);
        final var candidate = utkastService.getDraft(candidateId, false);
        var error = true;

        LOG.debug("Attempting to copy data from certificate with type '{}' and id '{}' to draft with type '{}' and id '{}'",
            candidate.getIntygsTyp(), candidateId, certificate.getIntygsTyp(), certificateId);

        draftAccessServiceHelper.validateAllowToCopyFromCandidate(certificate);

        try {
            utkastService.updateDraftFromCandidate(candidateId, candidate.getIntygsTyp(), certificate);
            if (certificate.getSkapadAv() != null) {
                monitoringLogService
                    .logUtkastCreatedTemplateAuto(certificateId, certificate.getIntygsTyp(), certificate.getSkapadAv().getHsaId(),
                        certificate.getEnhetsId(), candidateId, candidate.getIntygsTyp());
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
                    candidate.getIntygsTyp(), candidateId, certificate.getIntygsTyp(), certificateId);
            }
        }
    }

    private String getCandidateId(Utkast certificate) {
        try {
            final ModuleApi moduleApi = moduleRegistry.getModuleApi(certificate.getIntygsTyp(), certificate.getIntygTypeVersion());
            Optional<UtkastCandidateMetaData> metaData = utkastCandidateService
                .getCandidateMetaData(moduleApi, certificate.getIntygsTyp(), certificate.getIntygTypeVersion(),
                    getPatientDataFromPU(certificate.getIntygsTyp(), certificate), false);
            return metaData.get().getIntygId();
        } catch (ModuleNotFoundException e) {
            throw new WebCertServiceException(WebCertServiceErrorCodeEnum.MODULE_PROBLEM, e.getMessage());
        }
    }

    private Patient getPatientDataFromPU(String certificateType, Utkast certificate) {
        Patient resolvedPatientData = patientDetailsResolver.resolvePatient(
            certificate.getPatientPersonnummer(), certificateType, certificate.getIntygTypeVersion());
        if (resolvedPatientData == null) {
            throw new WebCertServiceException(
                WebCertServiceErrorCodeEnum.PU_PROBLEM, "Could not resolve Patient in PU-service when opening draft.");
        }
        return resolvedPatientData;
    }
}
