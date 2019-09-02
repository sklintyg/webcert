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
import se.inera.intyg.common.fk7263.support.Fk7263EntryPoint;
import se.inera.intyg.common.lisjp.support.LisjpEntryPoint;
import se.inera.intyg.common.luae_fs.support.LuaefsEntryPoint;
import se.inera.intyg.common.luae_na.support.LuaenaEntryPoint;
import se.inera.intyg.common.luse.support.LuseEntryPoint;
import se.inera.intyg.common.support.model.common.internal.Vardenhet;
import se.inera.intyg.infra.security.common.model.AuthoritiesConstants;
import se.inera.intyg.schemas.contract.Personnummer;
import se.inera.intyg.webcert.web.service.patient.PatientDetailsResolver;
import se.inera.intyg.webcert.web.service.user.WebCertUserService;
import se.inera.intyg.webcert.web.service.user.dto.WebCertUser;
import se.inera.intyg.webcert.web.service.utkast.UtkastService;

/**
 * Implementation of CertificateAccessService.
 */
@Service
public class CertificateAccessServiceImpl implements CertificateAccessService {

    private final WebCertUserService webCertUserService;
    private final PatientDetailsResolver patientDetailsResolver;
    private final UtkastService utkastService;

    @Autowired
    public CertificateAccessServiceImpl(final WebCertUserService webCertUserService,
            final PatientDetailsResolver patientDetailsResolver,
            final UtkastService utkastService) {
        this.webCertUserService = webCertUserService;
        this.patientDetailsResolver = patientDetailsResolver;
        this.utkastService = utkastService;
    }

    @Override
    public AccessResult allowToRead(String certificateType, Vardenhet vardenhet, Personnummer personnummer) {
        return getAccessServiceEvaluation().given(getUser(), certificateType)
                .feature(AuthoritiesConstants.FEATURE_HANTERA_INTYGSUTKAST)
                .careUnit(vardenhet)
                .patient(personnummer)
                .checkPatientSecrecy()
                .checkUnit(true, true)
                .evaluate();
    }

    @Override
    public AccessResult allowToReplace(String certificateType, Vardenhet vardenhet, Personnummer personnummer) {
        return getAccessServiceEvaluation().given(getUser(), certificateType)
                .privilege(AuthoritiesConstants.PRIVILEGE_ERSATTA_INTYG)
                .careUnit(vardenhet)
                .patient(personnummer)
                .excludeCertificateTypesForDeceased(DbModuleEntryPoint.MODULE_ID, DoiModuleEntryPoint.MODULE_ID)
                .checkPatientDeceased(false)
                .checkInactiveCareUnit(false)
                .excludeCertificateTypesForRenew(DbModuleEntryPoint.MODULE_ID, DoiModuleEntryPoint.MODULE_ID)
                .checkRenew(true)
                .checkPatientSecrecy()
                .excludeCertificateTypesForUnit(DbModuleEntryPoint.MODULE_ID, DoiModuleEntryPoint.MODULE_ID)
                .checkUnit(false, false)
                .evaluate();
    }

    @Override
    public AccessResult allowToRenew(String certificateType, Vardenhet careUnit, Personnummer patient) {
        return getAccessServiceEvaluation().given(getUser(), certificateType)
                .feature(AuthoritiesConstants.FEATURE_FORNYA_INTYG)
                .privilege(AuthoritiesConstants.PRIVILEGE_FORNYA_INTYG)
                .careUnit(careUnit)
                .patient(patient)
                .checkPatientDeceased(false)
                .checkInactiveCareUnit(false)
                .checkRenew(false)
                .checkPatientSecrecy()
                .checkUnit(true, true)
                .evaluate();
    }

    @Override
    public AccessResult allowToPrint(String certificateType, Vardenhet careUnit, Personnummer patient, boolean isEmployer) {
        return getAccessServiceEvaluation().given(getUser(), certificateType)
                .featureIf(AuthoritiesConstants.FEATURE_ARBETSGIVARUTSKRIFT, isEmployer)
                .featureIf(AuthoritiesConstants.FEATURE_UTSKRIFT, !isEmployer)
                .privilege(AuthoritiesConstants.PRIVILEGE_VISA_INTYG)
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
    public AccessResult allowToInvalidate(String certificateType, Vardenhet careUnit, Personnummer patient) {
        return getAccessServiceEvaluation().given(getUser(), certificateType)
                .feature(AuthoritiesConstants.FEATURE_MAKULERA_INTYG)
                .privilege(AuthoritiesConstants.PRIVILEGE_MAKULERA_INTYG)
                .careUnit(careUnit)
                .patient(patient)
                .checkPatientDeceased(true)
                .checkInactiveCareUnit(true)
                .checkRenew(true)
                .checkPatientSecrecy()
                .checkUnit(true, false)
                .evaluate();
    }

    @Override
    public AccessResult allowToSend(String certificateType, Vardenhet careUnit, Personnummer patient) {
        return getAccessServiceEvaluation().given(getUser(), certificateType)
                .feature(AuthoritiesConstants.FEATURE_SKICKA_INTYG)
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
    public AccessResult allowToCreateQuestion(String certificateType, Vardenhet careUnit, Personnummer patient) {
        return getAccessServiceEvaluation().given(getUser(), certificateType)
                .feature(AuthoritiesConstants.FEATURE_HANTERA_FRAGOR)
                .privilege(AuthoritiesConstants.PRIVILEGE_SKAPA_NYFRAGA)
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
    public AccessResult allowToAnswerComplementQuestion(String certificateType, Vardenhet careUnit, Personnummer patient,
            boolean newCertificate) {
        return getAccessServiceEvaluation().given(getUser(), certificateType)
                .feature(AuthoritiesConstants.FEATURE_HANTERA_FRAGOR)
                .privilege(AuthoritiesConstants.PRIVILEGE_BESVARA_KOMPLETTERINGSFRAGA)
                .privilegeIf(AuthoritiesConstants.PRIVILEGE_SVARA_MED_NYTT_INTYG, newCertificate)
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
    public AccessResult allowToAnswerAdminQuestion(String certificateType, Vardenhet careUnit, Personnummer patient) {
        return getAccessServiceEvaluation().given(getUser(), certificateType)
                .feature(AuthoritiesConstants.FEATURE_HANTERA_FRAGOR)
                .privilege(AuthoritiesConstants.PRIVILEGE_BESVARA_FRAGA)
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
    public AccessResult allowToReadQuestions(String certificateType, Vardenhet careUnit, Personnummer patient) {
        return getAccessServiceEvaluation().given(getUser(), certificateType)
                .feature(AuthoritiesConstants.FEATURE_HANTERA_FRAGOR)
                .privilege(AuthoritiesConstants.PRIVILEGE_LASA_FRAGA)
                .careUnit(careUnit)
                .patient(patient)
                .checkPatientDeceased(true)
                .excludeCertificateTypesForDeceased(LisjpEntryPoint.MODULE_ID, Fk7263EntryPoint.MODULE_ID, LuaefsEntryPoint.MODULE_ID,
                        LuaenaEntryPoint.MODULE_ID, LuseEntryPoint.MODULE_ID)
                .checkInactiveCareUnit(true)
                .excludeCertificateTypesForInactive(LisjpEntryPoint.MODULE_ID, Fk7263EntryPoint.MODULE_ID, LuaefsEntryPoint.MODULE_ID,
                        LuaenaEntryPoint.MODULE_ID, LuseEntryPoint.MODULE_ID)
                .checkRenew(true)
                .excludeCertificateTypesForRenew(LisjpEntryPoint.MODULE_ID, Fk7263EntryPoint.MODULE_ID, LuaefsEntryPoint.MODULE_ID,
                        LuaenaEntryPoint.MODULE_ID, LuseEntryPoint.MODULE_ID)
                .checkPatientSecrecy()
                .checkUnit(true, true)
                .evaluate();
    }

    @Override
    public AccessResult allowToForwardQuestions(String certificateType, Vardenhet careUnit, Personnummer patient) {
        return getAccessServiceEvaluation().given(getUser(), certificateType)
                .feature(AuthoritiesConstants.FEATURE_HANTERA_FRAGOR)
                .privilege(AuthoritiesConstants.PRIVILEGE_VIDAREBEFORDRA_FRAGASVAR)
                .careUnit(careUnit)
                .patient(patient)
                .checkPatientDeceased(true)
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
