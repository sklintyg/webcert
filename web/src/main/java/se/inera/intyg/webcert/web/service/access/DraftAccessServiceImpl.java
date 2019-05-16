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

@Service
public class DraftAccessServiceImpl implements DraftAccessService {
    private final WebCertUserService webCertUserService;
    private final PatientDetailsResolver patientDetailsResolver;
    private final UtkastService utkastService;

    @Autowired
    public DraftAccessServiceImpl(final WebCertUserService webCertUserService,
            final PatientDetailsResolver patientDetailsResolver,
            final UtkastService utkastService) {
        this.webCertUserService = webCertUserService;
        this.patientDetailsResolver = patientDetailsResolver;
        this.utkastService = utkastService;
    }

    @Override
    public AccessResult allowToCreateDraft(String intygsTyp, Personnummer personnummer) {
        return getAccessServiceEvaluation().given(getUser(), intygsTyp)
                .feature(AuthoritiesConstants.FEATURE_HANTERA_INTYGSUTKAST)
                .privilege(AuthoritiesConstants.PRIVILEGE_SKRIVA_INTYG)
                .patient(personnummer)
                .checkPatientDeceased(false)
                .excludeCertificateTypesForDeceased(DoiModuleEntryPoint.MODULE_ID)
                .checkInactiveCareUnit(false)
                .checkRenew(false)
                .checkPatientSecrecy()
                .checkUnique()
                .evaluate();
    }

    @Override
    public AccessResult allowToReadDraft(String intygsTyp, Vardenhet vardenhet, Personnummer personnummer) {
        return getAccessServiceEvaluation().given(getUser(), intygsTyp)
                .feature(AuthoritiesConstants.FEATURE_HANTERA_INTYGSUTKAST)
                .privilege(AuthoritiesConstants.PRIVILEGE_SKRIVA_INTYG)
                .careUnit(vardenhet)
                .patient(personnummer)
                .checkPatientSecrecy()
                .evaluate();
    }

    @Override
    public AccessResult allowToEditDraft(String intygsTyp, Vardenhet vardenhet, Personnummer personnummer) {
        return getAccessServiceEvaluation().given(getUser(), intygsTyp)
                .feature(AuthoritiesConstants.FEATURE_HANTERA_INTYGSUTKAST)
                .privilege(AuthoritiesConstants.PRIVILEGE_SKRIVA_INTYG)
                .careUnit(vardenhet)
                .patient(personnummer)
                .checkPatientDeceased(true)
                .invalidCertificateTypeForDeceased(DbModuleEntryPoint.MODULE_ID)
                .checkInactiveCareUnit(true)
                .checkRenew(true)
                .checkPatientSecrecy()
                .checkUnit(false, false)
                .evaluate();
    }

    @Override
    public AccessResult allowToDeleteDraft(String intygsTyp, Vardenhet vardenhet, Personnummer personnummer) {
        return getAccessServiceEvaluation().given(getUser(), intygsTyp)
                .feature(AuthoritiesConstants.FEATURE_HANTERA_INTYGSUTKAST)
                .privilege(AuthoritiesConstants.PRIVILEGE_SKRIVA_INTYG)
                .careUnit(vardenhet)
                .patient(personnummer)
                .checkPatientDeceased(true)
                .excludeCertificateTypesForDeceased(DbModuleEntryPoint.MODULE_ID, DoiModuleEntryPoint.MODULE_ID)
                .checkInactiveCareUnit(true)
                .checkRenew(true)
                .excludeCertificateTypesForRenew(DbModuleEntryPoint.MODULE_ID, DoiModuleEntryPoint.MODULE_ID)
                .checkPatientSecrecy()
                .checkUnit(false, false)
                .excludeCertificateTypesForUnit(DbModuleEntryPoint.MODULE_ID, DoiModuleEntryPoint.MODULE_ID)
                .evaluate();
    }

    @Override
    public AccessResult allowToSignDraft(String intygsTyp, Vardenhet vardenhet, Personnummer personnummer) {
        // TODO Handle unique rule
        // Additional constraints for specific types of intyg.
        // Personnummer patientPersonnummer = utkast.getPatientPersonnummer();
        // Map<String, Map<String, PreviousIntyg>> intygstypToPreviousIntyg = utkastService
        // .checkIfPersonHasExistingIntyg(patientPersonnummer, user);
        // Optional<WebCertServiceErrorCodeEnum> uniqueErrorCode = AuthoritiesHelperUtil.validateIntygMustBeUnique(
        // user,
        // utkast.getIntygsTyp(),
        // intygstypToPreviousIntyg,
        // utkast.getSkapad());
        // if (uniqueErrorCode.isPresent()) {
        // LOG.warn("Utkast '{}' av typ {} kan inte signeras då det redan existerar ett signerat intyg för samma personnummer",
        // intygId, utkast.getIntygsTyp());
        // throw new WebCertServiceException(WebCertServiceErrorCodeEnum.INTYG_FROM_OTHER_VARDGIVARE_EXISTS,
        // "An intyg already exists, application rules forbide signing another");
        // }

        return getAccessServiceEvaluation().given(getUser(), intygsTyp)
                .feature(AuthoritiesConstants.FEATURE_HANTERA_INTYGSUTKAST)
                .privilege(AuthoritiesConstants.PRIVILEGE_SKRIVA_INTYG)
                .careUnit(vardenhet)
                .patient(personnummer)
                .checkPatientDeceased(true)
                .invalidCertificateTypeForDeceased(DbModuleEntryPoint.MODULE_ID)
                .checkInactiveCareUnit(true)
                .checkRenew(true)
                .checkPatientSecrecy()
                .checkUnit(false, false)
                .evaluate();
    }

    @Override
    public AccessResult allowToPrintDraft(String intygsTyp, Vardenhet vardenhet, Personnummer personnummer) {
        return getAccessServiceEvaluation().given(getUser(), intygsTyp)
                .feature(AuthoritiesConstants.FEATURE_UTSKRIFT)
                .privilege(AuthoritiesConstants.PRIVILEGE_VISA_INTYG)
                .careUnit(vardenhet)
                .patient(personnummer)
                .checkPatientDeceased(true)
                .invalidCertificateTypeForDeceased(DbModuleEntryPoint.MODULE_ID)
                .checkInactiveCareUnit(true)
                .checkRenew(true)
                .checkPatientSecrecy()
                .checkUnit(false, false)
                .evaluate();
    }

    @Override
    public AccessResult allowToForwardDraft(String intygsTyp, Vardenhet vardenhet, Personnummer personnummer) {
        return getAccessServiceEvaluation().given(getUser(), intygsTyp)
                .feature(AuthoritiesConstants.FEATURE_HANTERA_INTYGSUTKAST)
                .privilege(AuthoritiesConstants.PRIVILEGE_SKRIVA_INTYG)
                .careUnit(vardenhet)
                .patient(personnummer)
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
