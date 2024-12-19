/*
 * Copyright (C) 2024 Inera AB (http://www.inera.se)
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
import se.inera.intyg.common.ag7804.support.Ag7804EntryPoint;
import se.inera.intyg.common.db.support.DbModuleEntryPoint;
import se.inera.intyg.common.doi.support.DoiModuleEntryPoint;
import se.inera.intyg.common.fk7263.support.Fk7263EntryPoint;
import se.inera.intyg.common.lisjp.support.LisjpEntryPoint;
import se.inera.intyg.common.luae_fs.support.LuaefsEntryPoint;
import se.inera.intyg.common.luae_na.support.LuaenaEntryPoint;
import se.inera.intyg.common.luse.support.LuseEntryPoint;
import se.inera.intyg.common.services.texts.IntygTextsService;
import se.inera.intyg.infra.security.common.model.AuthoritiesConstants;
import se.inera.intyg.infra.security.common.model.UserOriginType;
import se.inera.intyg.webcert.web.service.patient.PatientDetailsResolver;
import se.inera.intyg.webcert.web.service.user.WebCertUserService;
import se.inera.intyg.webcert.web.service.user.dto.WebCertUser;
import se.inera.intyg.webcert.web.service.utkast.UtkastService;

@Service
public class CertificateAccessServiceImpl implements CertificateAccessService {

    private final WebCertUserService webCertUserService;
    private final PatientDetailsResolver patientDetailsResolver;
    private final UtkastService utkastService;
    private final IntygTextsService intygTextsService;

    @Autowired
    public CertificateAccessServiceImpl(final WebCertUserService webCertUserService,
        final PatientDetailsResolver patientDetailsResolver,
        final UtkastService utkastService, IntygTextsService intygTextsService) {
        this.webCertUserService = webCertUserService;
        this.patientDetailsResolver = patientDetailsResolver;
        this.utkastService = utkastService;
        this.intygTextsService = intygTextsService;
    }

    @Override
    public AccessResult allowToRead(AccessEvaluationParameters accessEvaluationParameters) {
        return getAccessServiceEvaluation().given(getUser(), accessEvaluationParameters.getCertificateType())
            .feature(AuthoritiesConstants.FEATURE_HANTERA_INTYGSUTKAST)
            .privilege(AuthoritiesConstants.PRIVILEGE_VISA_INTYG)
            .careUnit(accessEvaluationParameters.getUnit())
            .patient(accessEvaluationParameters.getPatient())
            .checkPatientSecrecy()
            .checkUnit(true, true)
            .evaluate();
    }

    @Override
    public AccessResult allowToReplace(AccessEvaluationParameters accessEvaluationParameters) {
        return getAccessServiceEvaluation().given(getUser(), accessEvaluationParameters.getCertificateType())
            .privilege(AuthoritiesConstants.PRIVILEGE_ERSATTA_INTYG)
            .blockFeatureIf(AuthoritiesConstants.FEATURE_ENABLE_BLOCK_ORIGIN_NORMAL,
                getUser().getOrigin().equalsIgnoreCase(UserOriginType.NORMAL.name()))
            .checkLatestCertificateTypeVersion(accessEvaluationParameters.getCertificateTypeVersion())
            .careUnit(accessEvaluationParameters.getUnit())
            .patient(accessEvaluationParameters.getPatient())
            .excludeCertificateTypesForDeceased(DbModuleEntryPoint.MODULE_ID, DoiModuleEntryPoint.MODULE_ID)
            .checkPatientDeceased(false)
            .checkInactiveCareUnit(false)
            .excludeCertificateTypesForRenew(DbModuleEntryPoint.MODULE_ID, DoiModuleEntryPoint.MODULE_ID)
            .checkRenew(true)
            .checkPatientSecrecy()
            .excludeCertificateTypesForUnit(DbModuleEntryPoint.MODULE_ID, DoiModuleEntryPoint.MODULE_ID)
            .checkUnit(false, false)
            .checkSubscription()
            .checkInactiveCertificateType()
            .evaluate();
    }

    @Override
    public AccessResult allowToRenew(AccessEvaluationParameters accessEvaluationParameters) {
        return getAccessServiceEvaluation().given(getUser(), accessEvaluationParameters.getCertificateType())
            .feature(AuthoritiesConstants.FEATURE_FORNYA_INTYG)
            .blockFeatureIf(AuthoritiesConstants.FEATURE_ENABLE_BLOCK_ORIGIN_NORMAL,
                getUser().getOrigin().equalsIgnoreCase(UserOriginType.NORMAL.name()))
            .checkLatestCertificateTypeVersion(accessEvaluationParameters.getCertificateTypeVersion())
            .privilege(AuthoritiesConstants.PRIVILEGE_FORNYA_INTYG)
            .careUnit(accessEvaluationParameters.getUnit())
            .patient(accessEvaluationParameters.getPatient())
            .checkPatientDeceased(false)
            .checkInactiveCareUnit(false)
            .checkRenew(false)
            .checkPatientSecrecy()
            .checkUnit(true, true)
            .checkSubscription()
            .checkInactiveCertificateType()
            .evaluate();
    }

    @Override
    public AccessResult allowToPrint(AccessEvaluationParameters accessEvaluationParameters, boolean isEmployer) {
        return getAccessServiceEvaluation().given(getUser(), accessEvaluationParameters.getCertificateType())
            .featureIf(AuthoritiesConstants.FEATURE_ARBETSGIVARUTSKRIFT, isEmployer)
            .featureIf(AuthoritiesConstants.FEATURE_UTSKRIFT, !isEmployer)
            .privilege(AuthoritiesConstants.PRIVILEGE_VISA_INTYG)
            .careUnit(accessEvaluationParameters.getUnit())
            .patient(accessEvaluationParameters.getPatient())
            .checkPatientSecrecy()
            .checkUnit(false, true)
            .evaluate();
    }

    @Override
    public AccessResult allowToInvalidate(AccessEvaluationParameters accessEvaluationParameters) {
        return getAccessServiceEvaluation().given(getUser(), accessEvaluationParameters.getCertificateType())
            .feature(AuthoritiesConstants.FEATURE_MAKULERA_INTYG)
            .privilege(AuthoritiesConstants.PRIVILEGE_MAKULERA_INTYG)
            .careUnit(accessEvaluationParameters.getUnit())
            .patient(accessEvaluationParameters.getPatient())
            .checkPatientDeceased(true)
            .checkInactiveCareUnit(true)
            .checkRenew(true)
            .checkPatientSecrecy()
            .checkUnit(false, false)
            .evaluate();
    }

    @Override
    public AccessResult allowToSend(AccessEvaluationParameters accessEvaluationParameters) {
        return getAccessServiceEvaluation().given(getUser(), accessEvaluationParameters.getCertificateType())
            .feature(AuthoritiesConstants.FEATURE_SKICKA_INTYG)
            .checkLatestCertificateTypeVersion(accessEvaluationParameters.getCertificateTypeVersion())
            .careUnit(accessEvaluationParameters.getUnit())
            .patient(accessEvaluationParameters.getPatient())
            .checkPatientDeceased(true)
            .checkPatientTestIndicator(false)
            .checkTestCertificate(accessEvaluationParameters.isTestCertificate())
            .checkInactiveCareUnit(true)
            .checkRenew(true)
            .checkPatientSecrecy()
            .checkUnit(false, false)
            .checkSubscription()
            .evaluate();
    }

    @Override
    public AccessResult allowToApproveReceivers(AccessEvaluationParameters accessEvaluationParameters) {
        return getAccessServiceEvaluation().given(getUser(), accessEvaluationParameters.getCertificateType())
            .privilege(AuthoritiesConstants.PRIVILEGE_GODKANNA_MOTTAGARE)
            .checkLatestCertificateTypeVersion(accessEvaluationParameters.getCertificateTypeVersion())
            .careUnit(accessEvaluationParameters.getUnit())
            .patient(accessEvaluationParameters.getPatient())
            .checkPatientDeceased(false)
            .checkPatientTestIndicator(false)
            .checkTestCertificate(accessEvaluationParameters.isTestCertificate())
            .checkInactiveCareUnit(true)
            .checkRenew(true)
            .checkPatientSecrecy()
            .checkUnit(false, false)
            .checkSubscription()
            .evaluate();
    }

    @Override
    public AccessResult allowToCreateQuestion(AccessEvaluationParameters accessEvaluationParameters) {
        return getAccessServiceEvaluation().given(getUser(), accessEvaluationParameters.getCertificateType())
            .feature(AuthoritiesConstants.FEATURE_HANTERA_FRAGOR)
            .privilege(AuthoritiesConstants.PRIVILEGE_SKAPA_NYFRAGA)
            .careUnit(accessEvaluationParameters.getUnit())
            .patient(accessEvaluationParameters.getPatient())
            .checkPatientDeceased(true)
            .checkPatientTestIndicator(false)
            .checkTestCertificate(accessEvaluationParameters.isTestCertificate())
            .checkInactiveCareUnit(true)
            .checkRenew(true)
            .checkPatientSecrecy()
            .checkUnit(false, false)
            .checkSubscription()
            .evaluate();
    }

    @Override
    public AccessResult allowToAnswerComplementQuestion(AccessEvaluationParameters accessEvaluationParameters,
        boolean newCertificate) {
        return getAccessServiceEvaluation().given(getUser(), accessEvaluationParameters.getCertificateType())
            .feature(AuthoritiesConstants.FEATURE_HANTERA_FRAGOR)
            .privilege(AuthoritiesConstants.PRIVILEGE_BESVARA_KOMPLETTERINGSFRAGA)
            .privilegeIf(AuthoritiesConstants.PRIVILEGE_SVARA_MED_NYTT_INTYG, newCertificate)
            .blockFeatureIf(AuthoritiesConstants.FEATURE_ENABLE_BLOCK_ORIGIN_NORMAL,
                getUser().getOrigin().equalsIgnoreCase(UserOriginType.NORMAL.name()))
            .checkLatestCertificateTypeVersion(accessEvaluationParameters.getCertificateTypeVersion())
            .careUnit(accessEvaluationParameters.getUnit())
            .patient(accessEvaluationParameters.getPatient())
            .checkPatientDeceased(true)
            .checkPatientTestIndicator(false)
            .checkTestCertificate(accessEvaluationParameters.isTestCertificate())
            .checkInactiveCareUnit(true)
            .checkRenew(true)
            .checkPatientSecrecy()
            .checkUnit(false, false)
            .checkSubscription()
            .evaluate();
    }

    @Override
    public AccessResult allowToCreateDraftFromSignedTemplate(AccessEvaluationParameters accessEvaluationParameters) {
        return getAccessServiceEvaluation().given(getUser(), getCertificateTypeToCreate(accessEvaluationParameters.getCertificateType()))
            .feature(AuthoritiesConstants.FEATURE_HANTERA_INTYGSUTKAST)
            .blockFeatureIf(AuthoritiesConstants.FEATURE_ENABLE_BLOCK_ORIGIN_NORMAL,
                getUser().getOrigin().equalsIgnoreCase(UserOriginType.NORMAL.name()))
            .privilege(AuthoritiesConstants.PRIVILEGE_SKRIVA_INTYG)
            .checkLatestCertificateTypeVersion(accessEvaluationParameters.getCertificateTypeVersion())
            .careUnit(accessEvaluationParameters.getUnit())
            .patient(accessEvaluationParameters.getPatient())
            .checkPatientDeceased(false)
            .excludeCertificateTypesForDeceased(DoiModuleEntryPoint.MODULE_ID)
            .checkInactiveCareUnit(false)
            .checkRenew(false)
            .checkPatientSecrecy()
            .checkUnique()
            .checkSubscription()
            .checkInactiveCertificateType()
            .evaluate();
    }

    /**
     * Currently not possible to do this mapping without hardcoding.
     *
     * @param sourceCertificateType Certificate type that will be used as template to create a new draft.
     * @return Certificate type of the new draft that potentially can be created.
     */
    private String getCertificateTypeToCreate(String sourceCertificateType) {
        switch (sourceCertificateType) {
            case DbModuleEntryPoint.MODULE_ID:
                return DoiModuleEntryPoint.MODULE_ID;
            case LisjpEntryPoint.MODULE_ID:
                return Ag7804EntryPoint.MODULE_ID;
            default:
                return sourceCertificateType;
        }
    }

    @Override
    public AccessResult allowToAnswerAdminQuestion(AccessEvaluationParameters accessEvaluationParameters) {
        return getAccessServiceEvaluation().given(getUser(), accessEvaluationParameters.getCertificateType())
            .feature(AuthoritiesConstants.FEATURE_HANTERA_FRAGOR)
            .privilege(AuthoritiesConstants.PRIVILEGE_BESVARA_FRAGA)
            .careUnit(accessEvaluationParameters.getUnit())
            .patient(accessEvaluationParameters.getPatient())
            .checkPatientDeceased(true)
            .checkPatientTestIndicator(false)
            .checkTestCertificate(accessEvaluationParameters.isTestCertificate())
            .checkInactiveCareUnit(true)
            .checkRenew(true)
            .checkPatientSecrecy()
            .checkUnit(false, false)
            .checkSubscription()
            .evaluate();
    }

    @Override
    public AccessResult allowToReadQuestions(AccessEvaluationParameters accessEvaluationParameters) {
        return getAccessServiceEvaluation().given(getUser(), accessEvaluationParameters.getCertificateType())
            .feature(AuthoritiesConstants.FEATURE_HANTERA_FRAGOR)
            .privilege(AuthoritiesConstants.PRIVILEGE_LASA_FRAGA)
            .careUnit(accessEvaluationParameters.getUnit())
            .patient(accessEvaluationParameters.getPatient())
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
    public AccessResult allowToForwardQuestions(AccessEvaluationParameters accessEvaluationParameters) {
        return getAccessServiceEvaluation().given(getUser(), accessEvaluationParameters.getCertificateType())
            .feature(AuthoritiesConstants.FEATURE_HANTERA_FRAGOR)
            .privilege(AuthoritiesConstants.PRIVILEGE_VIDAREBEFORDRA_FRAGASVAR)
            .careUnit(accessEvaluationParameters.getUnit())
            .patient(accessEvaluationParameters.getPatient())
            .checkPatientDeceased(true)
            .checkPatientTestIndicator(false)
            .checkTestCertificate(accessEvaluationParameters.isTestCertificate())
            .checkInactiveCareUnit(true)
            .checkRenew(true)
            .checkPatientSecrecy()
            .checkUnit(false, false)
            .checkSubscription()
            .evaluate();
    }

    @Override
    public AccessResult allowToSetComplementAsHandled(AccessEvaluationParameters accessEvaluationParameters) {
        return getAccessServiceEvaluation().given(getUser(), accessEvaluationParameters.getCertificateType())
            .feature(AuthoritiesConstants.FEATURE_HANTERA_FRAGOR)
            .privilege(AuthoritiesConstants.PRIVILEGE_MARKERA_KOMPLETTERING_SOM_HANTERAD)
            .careUnit(accessEvaluationParameters.getUnit())
            .patient(accessEvaluationParameters.getPatient())
            .checkPatientDeceased(true)
            .checkPatientTestIndicator(false)
            .checkTestCertificate(accessEvaluationParameters.isTestCertificate())
            .checkInactiveCareUnit(true)
            .checkRenew(true)
            .checkPatientSecrecy()
            .checkUnit(false, false)
            .checkSubscription()
            .evaluate();
    }

    @Override
    public AccessResult allowToSetQuestionAsHandled(AccessEvaluationParameters accessEvaluationParameters) {
        return getAccessServiceEvaluation().given(getUser(), accessEvaluationParameters.getCertificateType())
            .feature(AuthoritiesConstants.FEATURE_HANTERA_FRAGOR)
            .privilege(AuthoritiesConstants.PRIVILEGE_MARKERA_FRAGA_SOM_HANTERAD)
            .careUnit(accessEvaluationParameters.getUnit())
            .patient(accessEvaluationParameters.getPatient())
            .checkPatientDeceased(true)
            .checkPatientTestIndicator(false)
            .checkTestCertificate(accessEvaluationParameters.isTestCertificate())
            .checkInactiveCareUnit(true)
            .checkRenew(true)
            .checkPatientSecrecy()
            .checkUnit(false, false)
            .checkSubscription()
            .evaluate();
    }

    private AccessServiceEvaluation getAccessServiceEvaluation() {
        return AccessServiceEvaluation.create(webCertUserService, patientDetailsResolver, utkastService, intygTextsService);
    }

    private WebCertUser getUser() {
        return webCertUserService.getUser();
    }
}
