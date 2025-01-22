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
package se.inera.intyg.webcert.web.service.access;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import se.inera.intyg.common.support.model.common.internal.Utlatande;
import se.inera.intyg.common.support.model.common.internal.Vardenhet;
import se.inera.intyg.schemas.contract.Personnummer;
import se.inera.intyg.webcert.web.web.util.access.AccessResultExceptionHelper;

@Component
public final class CertificateAccessServiceHelper {

    private final CertificateAccessService certificateAccessService;
    private final AccessResultExceptionHelper accessResultExceptionHelper;

    @Autowired
    public CertificateAccessServiceHelper(CertificateAccessService certificateAccessService,
        AccessResultExceptionHelper accessResultExceptionHelper) {
        this.certificateAccessService = certificateAccessService;
        this.accessResultExceptionHelper = accessResultExceptionHelper;
    }

    public boolean isAllowToRenew(Utlatande certificate) {
        return evaluateAccessToRenew(certificate).isAllowed();
    }

    public boolean isAllowToRenew(AccessEvaluationParameters accessEvaluationParameters) {
        return certificateAccessService.allowToRenew(accessEvaluationParameters).isAllowed();
    }

    public void validateAccessToRenew(Utlatande utlatande) {
        final AccessResult accessResult = evaluateAccessToRenew(utlatande);

        accessResultExceptionHelper.throwExceptionIfDenied(accessResult);
    }

    private AccessResult evaluateAccessToRenew(Utlatande utlatande) {
        return certificateAccessService.allowToRenew(
            AccessEvaluationParameters.create(utlatande.getTyp(),
                utlatande.getTextVersion(),
                getVardenhet(utlatande),
                getPersonnummer(utlatande),
                utlatande.getGrundData().isTestIntyg()));
    }

    public boolean isAllowToAnswerComplementQuestion(Utlatande certificate, boolean newCertificate) {
        return evaluateAccessToAnswerComplementQuestion(certificate, newCertificate).isAllowed();
    }

    public boolean isAllowToAnswerComplementQuestion(AccessEvaluationParameters accessEvaluationParameters, boolean newCertificate) {
        return certificateAccessService.allowToAnswerComplementQuestion(accessEvaluationParameters, newCertificate).isAllowed();
    }

    public void validateAccessToAnswerComplementQuestion(Utlatande utlatande, boolean newCertificate) {
        final AccessResult accessResult = evaluateAccessToAnswerComplementQuestion(utlatande, newCertificate);

        accessResultExceptionHelper.throwExceptionIfDenied(accessResult);
    }

    private AccessResult evaluateAccessToAnswerComplementQuestion(Utlatande utlatande, boolean newCertificate) {
        return certificateAccessService.allowToAnswerComplementQuestion(
            AccessEvaluationParameters.create(utlatande.getTyp(),
                utlatande.getTextVersion(),
                getVardenhet(utlatande),
                getPersonnummer(utlatande),
                utlatande.getGrundData().isTestIntyg()),
            newCertificate);
    }

    public boolean isAllowToReplace(Utlatande certificate) {
        return evaluateAccessToReplace(certificate).isAllowed();
    }

    public boolean isAllowToReplace(AccessEvaluationParameters accessEvaluationParameters) {
        return certificateAccessService.allowToReplace(accessEvaluationParameters).isAllowed();
    }

    public void validateAccessToReplace(Utlatande utlatande) {
        final AccessResult accessResult = evaluateAccessToReplace(utlatande);

        accessResultExceptionHelper.throwExceptionIfDenied(accessResult);
    }

    private AccessResult evaluateAccessToReplace(Utlatande utlatande) {
        return certificateAccessService.allowToReplace(
            AccessEvaluationParameters.create(utlatande.getTyp(),
                utlatande.getTextVersion(),
                getVardenhet(utlatande),
                getPersonnummer(utlatande),
                utlatande.getGrundData().isTestIntyg()));
    }

    public boolean isAllowToInvalidate(Utlatande certificate) {
        return evaluateAccessToInvalidate(certificate).isAllowed();
    }

    public boolean isAllowToInvalidate(AccessEvaluationParameters accessEvaluationParameters) {
        return certificateAccessService.allowToInvalidate(accessEvaluationParameters).isAllowed();
    }

    public void validateAccessToInvalidate(Utlatande utlatande) {
        final AccessResult accessResult = evaluateAccessToInvalidate(utlatande);

        accessResultExceptionHelper.throwExceptionIfDenied(accessResult);
    }

    private AccessResult evaluateAccessToInvalidate(Utlatande utlatande) {
        return certificateAccessService.allowToInvalidate(
            AccessEvaluationParameters.create(utlatande.getTyp(),
                utlatande.getTextVersion(),
                getVardenhet(utlatande),
                getPersonnummer(utlatande),
                utlatande.getGrundData().isTestIntyg()));
    }

    public boolean isAllowToSend(Utlatande certificate) {
        return evaluateAccessToSend(certificate).isAllowed();
    }

    public boolean isAllowToSend(AccessEvaluationParameters accessEvaluationParameters) {
        return certificateAccessService.allowToSend(accessEvaluationParameters).isAllowed();
    }

    public void validateAccessToSend(Utlatande utlatande) {
        final AccessResult accessResult = evaluateAccessToSend(utlatande);

        accessResultExceptionHelper.throwExceptionIfDenied(accessResult);
    }

    private AccessResult evaluateAccessToSend(Utlatande utlatande) {
        return certificateAccessService.allowToSend(
            AccessEvaluationParameters.create(utlatande.getTyp(),
                utlatande.getTextVersion(),
                getVardenhet(utlatande),
                getPersonnummer(utlatande),
                utlatande.getGrundData().isTestIntyg()));
    }

    public boolean isAllowToApproveReceivers(Utlatande certificate) {
        return evaluateAccessToApproveReceivers(certificate).isAllowed();
    }

    public boolean isAllowToApproveReceivers(AccessEvaluationParameters accessEvaluationParameters) {
        return validateAccessToApproveReceivers(accessEvaluationParameters).isAllowed();
    }

    public AccessResult validateAccessToApproveReceivers(AccessEvaluationParameters accessEvaluationParameters) {
        return certificateAccessService.allowToApproveReceivers(accessEvaluationParameters);
    }

    public void validateAccessToApproveReceivers(Utlatande utlatande) {
        final AccessResult accessResult = evaluateAccessToApproveReceivers(utlatande);
        accessResultExceptionHelper.throwExceptionIfDenied(accessResult);
    }

    private AccessResult evaluateAccessToApproveReceivers(Utlatande utlatande) {
        return certificateAccessService.allowToApproveReceivers(
            AccessEvaluationParameters.create(utlatande.getTyp(),
                utlatande.getTextVersion(),
                getVardenhet(utlatande),
                getPersonnummer(utlatande),
                utlatande.getGrundData().isTestIntyg()));
    }

    public boolean isAllowToPrint(Utlatande certificate, boolean isEmployer) {
        return evaluateAccessToPrint(certificate, isEmployer).isAllowed();
    }

    public boolean isAllowToPrint(AccessEvaluationParameters accessEvaluationParameters, boolean isEmployer) {
        return certificateAccessService.allowToPrint(accessEvaluationParameters, isEmployer).isAllowed();
    }

    public void validateAccessToPrint(Utlatande utlatande, boolean isEmployer) {
        final AccessResult accessResult = evaluateAccessToPrint(utlatande, isEmployer);

        accessResultExceptionHelper.throwExceptionIfDenied(accessResult);
    }

    private AccessResult evaluateAccessToPrint(Utlatande utlatande, boolean isEmployer) {
        return certificateAccessService.allowToPrint(
            AccessEvaluationParameters.create(utlatande.getTyp(),
                utlatande.getTextVersion(),
                getVardenhet(utlatande),
                getPersonnummer(utlatande),
                utlatande.getGrundData().isTestIntyg()),
            isEmployer);
    }

    public boolean isAllowToRead(Utlatande certificate) {
        return evaluateAccessToRead(certificate).isAllowed();
    }

    public boolean isAllowToRead(AccessEvaluationParameters accessEvaluationParameters) {
        return certificateAccessService.allowToRead(accessEvaluationParameters).isAllowed();
    }

    public void validateAccessToRead(Utlatande utlatande) {
        final AccessResult accessResult = evaluateAccessToRead(utlatande);

        accessResultExceptionHelper.throwExceptionIfDenied(accessResult);
    }

    private AccessResult evaluateAccessToRead(Utlatande utlatande) {
        return certificateAccessService.allowToRead(
            AccessEvaluationParameters.create(utlatande.getTyp(),
                utlatande.getTextVersion(),
                getVardenhet(utlatande),
                getPersonnummer(utlatande),
                utlatande.getGrundData().isTestIntyg()));
    }

    public boolean isAllowToForwardQuestions(Utlatande certificate) {
        return evaluateAccessToForwardQuestions(certificate).isAllowed();
    }

    public boolean isAllowToForwardQuestions(AccessEvaluationParameters accessEvaluationParameters) {
        return certificateAccessService.allowToForwardQuestions(accessEvaluationParameters).isAllowed();
    }

    public void validateAccessToForwardQuestions(Utlatande utlatande) {
        final AccessResult accessResult = evaluateAccessToForwardQuestions(utlatande);

        accessResultExceptionHelper.throwExceptionIfDenied(accessResult);
    }

    private AccessResult evaluateAccessToForwardQuestions(Utlatande utlatande) {
        return certificateAccessService.allowToForwardQuestions(
            AccessEvaluationParameters.create(utlatande.getTyp(),
                utlatande.getTextVersion(),
                getVardenhet(utlatande),
                getPersonnummer(utlatande),
                utlatande.getGrundData().isTestIntyg()));
    }

    public boolean isAllowToCreateQuestion(Utlatande certificate) {
        return evaluateAccessToCreateQuestion(certificate).isAllowed();
    }

    public boolean isAllowToCreateQuestion(AccessEvaluationParameters accessEvaluationParameters) {
        return certificateAccessService.allowToCreateQuestion(accessEvaluationParameters).isAllowed();
    }

    public void validateAccessToCreateQuestion(Utlatande utlatande) {
        final AccessResult accessResult = evaluateAccessToCreateQuestion(utlatande);

        accessResultExceptionHelper.throwExceptionIfDenied(accessResult);
    }

    private AccessResult evaluateAccessToCreateQuestion(Utlatande utlatande) {
        return certificateAccessService.allowToCreateQuestion(
            AccessEvaluationParameters.create(utlatande.getTyp(),
                utlatande.getTextVersion(),
                getVardenhet(utlatande),
                getPersonnummer(utlatande),
                utlatande.getGrundData().isTestIntyg()));
    }

    public boolean isAllowToSetComplementAsHandled(Utlatande certificate) {
        return evaluateAccessToSetComplementAsHandled(certificate).isAllowed();
    }

    public boolean isAllowToSetComplementAsHandled(AccessEvaluationParameters accessEvaluationParameters) {
        return certificateAccessService.allowToSetComplementAsHandled(accessEvaluationParameters).isAllowed();
    }

    public void validateAccessToSetComplementAsHandled(Utlatande utlatande) {
        final AccessResult accessResult = evaluateAccessToSetComplementAsHandled(utlatande);

        accessResultExceptionHelper.throwExceptionIfDenied(accessResult);
    }

    private AccessResult evaluateAccessToSetComplementAsHandled(Utlatande utlatande) {
        return certificateAccessService.allowToSetComplementAsHandled(
            AccessEvaluationParameters.create(utlatande.getTyp(),
                utlatande.getTextVersion(),
                getVardenhet(utlatande),
                getPersonnummer(utlatande),
                utlatande.getGrundData().isTestIntyg()));
    }

    public boolean isAllowToSetQuestionAsHandled(Utlatande certificate) {
        return evaluateAccessToSetQuestionAsHandled(certificate).isAllowed();
    }

    public boolean isAllowToSetQuestionAsHandled(AccessEvaluationParameters accessEvaluationParameters) {
        return certificateAccessService.allowToSetQuestionAsHandled(accessEvaluationParameters).isAllowed();
    }

    public void validateAccessToSetQuestionAsHandled(Utlatande utlatande) {
        final AccessResult accessResult = evaluateAccessToSetQuestionAsHandled(utlatande);

        accessResultExceptionHelper.throwExceptionIfDenied(accessResult);
    }

    private AccessResult evaluateAccessToSetQuestionAsHandled(Utlatande utlatande) {
        return certificateAccessService.allowToSetQuestionAsHandled(
            AccessEvaluationParameters.create(utlatande.getTyp(),
                utlatande.getTextVersion(),
                getVardenhet(utlatande),
                getPersonnummer(utlatande),
                utlatande.getGrundData().isTestIntyg()));
    }

    public boolean isAllowToAnswerAdminQuestion(Utlatande certificate) {
        return evaluateAccessToAnswerAdminQuestion(certificate).isAllowed();
    }

    public boolean isAllowToAnswerAdminQuestion(AccessEvaluationParameters accessEvaluationParameters) {
        return certificateAccessService.allowToAnswerAdminQuestion(accessEvaluationParameters).isAllowed();
    }

    public void validateAccessToAnswerAdminQuestion(Utlatande utlatande) {
        final AccessResult accessResult = evaluateAccessToAnswerAdminQuestion(utlatande);

        accessResultExceptionHelper.throwExceptionIfDenied(accessResult);
    }

    private AccessResult evaluateAccessToAnswerAdminQuestion(Utlatande utlatande) {
        return certificateAccessService.allowToAnswerAdminQuestion(
            AccessEvaluationParameters.create(utlatande.getTyp(),
                utlatande.getTextVersion(),
                getVardenhet(utlatande),
                getPersonnummer(utlatande),
                utlatande.getGrundData().isTestIntyg()));
    }

    public boolean isAllowToReadQuestions(Utlatande certificate) {
        return evaluateAccessToReadQuestions(certificate).isAllowed();
    }

    public boolean isAllowToReadQuestions(AccessEvaluationParameters accessEvaluationParameters) {
        return certificateAccessService.allowToReadQuestions(accessEvaluationParameters).isAllowed();
    }

    public void validateAccessToReadQuestions(Utlatande utlatande) {
        final AccessResult accessResult = evaluateAccessToReadQuestions(utlatande);

        accessResultExceptionHelper.throwExceptionIfDenied(accessResult);
    }

    private AccessResult evaluateAccessToReadQuestions(Utlatande utlatande) {
        return certificateAccessService.allowToReadQuestions(
            AccessEvaluationParameters.create(utlatande.getTyp(),
                utlatande.getTextVersion(),
                getVardenhet(utlatande),
                getPersonnummer(utlatande),
                utlatande.getGrundData().isTestIntyg()));
    }

    public boolean isAllowToCreateDraftFromSignedTemplate(Utlatande certificate) {
        return evaluateAccessToCreateDraftFromSignedTemplate(certificate).isAllowed();
    }

    public boolean isAllowToCreateDraftFromSignedTemplate(AccessEvaluationParameters accessEvaluationParameters) {
        return certificateAccessService.allowToCreateDraftFromSignedTemplate(accessEvaluationParameters).isAllowed();
    }

    public void validateAllowCreateDraftFromSignedTemplate(Utlatande utlatande) {
        final AccessResult accessResult = evaluateAccessToCreateDraftFromSignedTemplate(utlatande);

        accessResultExceptionHelper.throwExceptionIfDenied(accessResult);
    }

    private AccessResult evaluateAccessToCreateDraftFromSignedTemplate(Utlatande utlatande) {
        return certificateAccessService.allowToCreateDraftFromSignedTemplate(
            AccessEvaluationParameters.create(utlatande.getTyp(),
                utlatande.getTextVersion(),
                getVardenhet(utlatande),
                getPersonnummer(utlatande),
                utlatande.getGrundData().isTestIntyg()));
    }

    private Vardenhet getVardenhet(Utlatande utlatande) {
        return utlatande.getGrundData().getSkapadAv().getVardenhet();
    }

    private Personnummer getPersonnummer(Utlatande utlatande) {
        return utlatande.getGrundData().getPatient().getPersonId();
    }
}
