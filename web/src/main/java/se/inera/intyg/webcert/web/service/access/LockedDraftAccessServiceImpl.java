/*
 * Copyright (C) 2019 Inera AB (http://www.inera.se)
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

package se.inera.intyg.webcert.web.service.access;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import se.inera.intyg.common.db.support.DbModuleEntryPoint;
import se.inera.intyg.common.doi.support.DoiModuleEntryPoint;
import se.inera.intyg.common.support.model.common.internal.Vardenhet;
import se.inera.intyg.infra.security.common.model.AuthoritiesConstants;
import se.inera.intyg.schemas.contract.Personnummer;
import se.inera.intyg.webcert.web.service.patient.PatientDetailsResolver;
import se.inera.intyg.webcert.web.service.user.WebCertUserService;
import se.inera.intyg.webcert.web.service.user.dto.WebCertUser;
import se.inera.intyg.webcert.web.service.utkast.UtkastService;

/**
 * Implementation of LockedDraftAccessService.
 */
@Service
public class LockedDraftAccessServiceImpl implements LockedDraftAccessService {

    private final WebCertUserService webCertUserService;
    private final PatientDetailsResolver patientDetailsResolver;
    private final UtkastService utkastService;

    @Autowired
    public LockedDraftAccessServiceImpl(final WebCertUserService webCertUserService,
        final PatientDetailsResolver patientDetailsResolver,
        final UtkastService utkastService) {
        this.webCertUserService = webCertUserService;
        this.patientDetailsResolver = patientDetailsResolver;
        this.utkastService = utkastService;
    }

    @Override
    public AccessResult allowToRead(String certificateType, Vardenhet careUnit, Personnummer patient) {
        return getAccessServiceEvaluation().given(getUser(), certificateType)
            .feature(AuthoritiesConstants.FEATURE_HANTERA_INTYGSUTKAST)
            .privilege(AuthoritiesConstants.PRIVILEGE_SKRIVA_INTYG)
            .careUnit(careUnit)
            .patient(patient)
            .checkPatientSecrecy()
            .checkUnit(true, true)
            .evaluate();
    }

    @Override
    public AccessResult allowedToCopyLockedUtkast(String certificateType, Vardenhet careUnit, Personnummer patient) {
        return getAccessServiceEvaluation().given(getUser(), certificateType)
            .feature(AuthoritiesConstants.FEATURE_HANTERA_INTYGSUTKAST)
            .privilege(AuthoritiesConstants.PRIVILEGE_SKRIVA_INTYG)
            .privilege(AuthoritiesConstants.PRIVILEGE_KOPIERA_LAST_UTKAST)
            .careUnit(careUnit)
            .patient(patient)
            .checkPatientDeceased(false)
            .excludeCertificateTypesForDeceased(DoiModuleEntryPoint.MODULE_ID)
            .checkInactiveCareUnit(false)
            .checkPatientSecrecy()
            .checkUnique()
            .checkUnit(true, true)
            .evaluate();
    }

    @Override
    public AccessResult allowedToInvalidateLockedUtkast(String certificateType, Vardenhet careUnit, Personnummer patient) {
        return getAccessServiceEvaluation().given(getUser(), certificateType)
            .feature(AuthoritiesConstants.FEATURE_MAKULERA_INTYG)
            .privilege(AuthoritiesConstants.PRIVILEGE_MAKULERA_INTYG)
            .careUnit(careUnit)
            .patient(patient)
            .checkPatientDeceased(true)
            .checkInactiveCareUnit(true)
            .checkRenew(true)
            .checkPatientSecrecy()
            .checkUnit(false, false)
            .evaluate();
    }

    @Override
    public AccessResult allowToPrint(String certificateType, Vardenhet careUnit, Personnummer patient) {
        return getAccessServiceEvaluation().given(getUser(), certificateType)
            .feature(AuthoritiesConstants.FEATURE_UTSKRIFT)
            .privilege(AuthoritiesConstants.PRIVILEGE_VISA_INTYG)
            .careUnit(careUnit)
            .patient(patient)
            .checkPatientDeceased(true)
            .invalidCertificateTypeForDeceased(DbModuleEntryPoint.MODULE_ID)
            .checkInactiveCareUnit(true)
            .checkRenew(true)
            .checkPatientSecrecy()
            .checkUnit(false, false)
            .evaluate();
    }

    private AccessServiceEvaluation getAccessServiceEvaluation() {
        return AccessServiceEvaluation.create(this.webCertUserService, this.patientDetailsResolver, this.utkastService);
    }

    private WebCertUser getUser() {
        return webCertUserService.getUser();
    }
}
