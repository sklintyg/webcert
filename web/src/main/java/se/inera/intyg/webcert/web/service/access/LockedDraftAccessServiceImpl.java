/*
 * Copyright (C) 2020 Inera AB (http://www.inera.se)
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
import se.inera.intyg.common.af00213.support.Af00213EntryPoint;
import se.inera.intyg.common.af00251.support.AF00251EntryPoint;
import se.inera.intyg.common.ag114.support.Ag114EntryPoint;
import se.inera.intyg.common.ag7804.support.Ag7804EntryPoint;
import se.inera.intyg.common.db.support.DbModuleEntryPoint;
import se.inera.intyg.common.doi.support.DoiModuleEntryPoint;
import se.inera.intyg.common.fk7263.support.Fk7263EntryPoint;
import se.inera.intyg.common.lisjp.support.LisjpEntryPoint;
import se.inera.intyg.common.luae_fs.support.LuaefsEntryPoint;
import se.inera.intyg.common.luae_na.support.LuaenaEntryPoint;
import se.inera.intyg.common.luse.support.LuseEntryPoint;
import se.inera.intyg.common.support.model.common.internal.Vardenhet;
import se.inera.intyg.common.ts_bas.support.TsBasEntryPoint;
import se.inera.intyg.common.ts_diabetes.support.TsDiabetesEntryPoint;
import se.inera.intyg.common.tstrk1009.support.Tstrk1009EntryPoint;
import se.inera.intyg.common.tstrk1062.support.TsTrk1062EntryPoint;
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
            .checkPatientDeceased(false)
            .invalidCertificateTypeForDeceased(DbModuleEntryPoint.MODULE_ID)
            .excludeCertificateTypesForDeceased(
                DoiModuleEntryPoint.MODULE_ID,
                LisjpEntryPoint.MODULE_ID,
                Fk7263EntryPoint.MODULE_ID,
                LuaefsEntryPoint.MODULE_ID,
                LuaenaEntryPoint.MODULE_ID,
                LuseEntryPoint.MODULE_ID,
                Af00213EntryPoint.MODULE_ID,
                AF00251EntryPoint.MODULE_ID,
                Ag114EntryPoint.MODULE_ID,
                Ag7804EntryPoint.MODULE_ID,
                TsBasEntryPoint.MODULE_ID,
                TsDiabetesEntryPoint.MODULE_ID,
                Tstrk1009EntryPoint.MODULE_ID,
                TsTrk1062EntryPoint.MODULE_ID)
            .checkPatientSecrecy()
            .checkUnit(false, true)
            .evaluate();
    }

    private AccessServiceEvaluation getAccessServiceEvaluation() {
        return AccessServiceEvaluation.create(this.webCertUserService, this.patientDetailsResolver, this.utkastService);
    }

    private WebCertUser getUser() {
        return webCertUserService.getUser();
    }
}
