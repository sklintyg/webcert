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
package se.inera.intyg.webcert.web.service.facade.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import se.inera.intyg.common.services.texts.IntygTextsService;
import se.inera.intyg.common.support.model.common.internal.HoSPersonal;
import se.inera.intyg.common.support.modules.registry.IntygModuleRegistry;
import se.inera.intyg.common.support.modules.registry.ModuleNotFoundException;
import se.inera.intyg.schemas.contract.Personnummer;
import se.inera.intyg.webcert.web.converter.util.IntygConverterUtil;
import se.inera.intyg.webcert.web.service.access.AccessResultCode;
import se.inera.intyg.webcert.web.service.access.DraftAccessServiceHelper;
import se.inera.intyg.webcert.web.service.facade.CreateCertificateFacadeService;
import se.inera.intyg.webcert.web.service.patient.PatientDetailsResolver;
import se.inera.intyg.webcert.web.service.user.WebCertUserService;
import se.inera.intyg.webcert.web.service.user.dto.WebCertUser;
import se.inera.intyg.webcert.web.service.utkast.UtkastService;
import se.inera.intyg.webcert.web.service.utkast.dto.CreateNewDraftRequest;
import se.inera.intyg.webcert.web.web.util.access.AccessResultExceptionHelper;

@Service
public class CreateCertificateFacadeServiceImpl implements CreateCertificateFacadeService {

    private static final Logger LOG = LoggerFactory.getLogger(CreateCertificateFacadeServiceImpl.class);

    private final DraftAccessServiceHelper draftAccessServiceHelper;
    private final IntygModuleRegistry intygModuleRegistry;
    private final AccessResultExceptionHelper accessResultExceptionHelper;
    private final UtkastService utkastService;
    private final IntygTextsService intygTextsService;
    private final WebCertUserService webCertUserService;
    private final PatientDetailsResolver patientDetailsResolver;

    @Autowired
    public CreateCertificateFacadeServiceImpl(DraftAccessServiceHelper draftAccessServiceHelper,
        IntygModuleRegistry intygModuleRegistry, AccessResultExceptionHelper accessResultExceptionHelper,
        UtkastService utkastService, IntygTextsService intygTextsService, WebCertUserService webCertUserService,
        PatientDetailsResolver patientDetailsResolver) {
        this.draftAccessServiceHelper = draftAccessServiceHelper;
        this.intygModuleRegistry = intygModuleRegistry;
        this.accessResultExceptionHelper = accessResultExceptionHelper;
        this.utkastService = utkastService;
        this.intygTextsService = intygTextsService;
        this.webCertUserService = webCertUserService;
        this.patientDetailsResolver = patientDetailsResolver;
    }

    @Override
    public String create(String certificateType, String patientId) throws CreateCertificateException {
        final var request = convertRequest(certificateType, patientId);

        try {
            if (intygModuleRegistry.getIntygModule(certificateType).isDeprecated()) {
                LOG.error("Request for deprecated module {}", certificateType);
                throw new CreateCertificateException("Request for deprecated module");
            }
        } catch (ModuleNotFoundException e) {
            LOG.error("Request for unknown module {}", certificateType);
            throw new CreateCertificateException("Request for unknown module");
        }

        LOG.debug("Attempting to create certificate of type '{}'", certificateType);

        final var actionResult = draftAccessServiceHelper.evaluateAllowToCreateUtkast(certificateType, getSSN(patientId));

        if (actionResult.isDenied()) {
            if (actionResult.getCode() == AccessResultCode.UNIQUE_DRAFT
                || actionResult.getCode() == AccessResultCode.UNIQUE_CERTIFICATE) {
                throw new CreateCertificateException("Certificate already exists");
            } else {
                accessResultExceptionHelper.throwException(actionResult);
            }
        }

        final var draft = utkastService.createNewDraft(request);
        LOG.debug("Created new certificate of type '{}' with id '{}'", certificateType, draft.getIntygsId());

        return draft.getIntygsId();
    }

    private CreateNewDraftRequest convertRequest(String certificateType, String patientId) throws CreateCertificateException {
        final var request = new CreateNewDraftRequest();
        final var latestVersion = intygTextsService.getLatestVersion(certificateType);
        final var patient = patientDetailsResolver.resolvePatient(getSSN(patientId), certificateType, latestVersion);
        final var staff = getStaff();
        request.setIntygType(certificateType);
        if (patient == null) {
            throw (new CreateCertificateException("Patient does not exist"));
        } else {
            request.setPatient(patient);
        }
        request.setIntygTypeVersion(latestVersion);
        request.setHosPerson(staff);
        return request;
    }

    private Personnummer getSSN(String patientId) throws CreateCertificateException {
        return formatPatientId(patientId);
    }

    private Personnummer formatPatientId(String personId) throws CreateCertificateException {
        return Personnummer.createPersonnummer(personId).orElseThrow(() -> new CreateCertificateException("Invalid patient id"));
    }

    private HoSPersonal getStaff() {
        WebCertUser user = webCertUserService.getUser();
        return IntygConverterUtil.buildHosPersonalFromWebCertUser(user, null);
    }
}
