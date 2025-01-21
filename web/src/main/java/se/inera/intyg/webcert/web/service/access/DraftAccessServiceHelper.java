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
import se.inera.intyg.schemas.contract.Personnummer;
import se.inera.intyg.webcert.persistence.utkast.model.Utkast;
import se.inera.intyg.webcert.web.util.UtkastUtil;
import se.inera.intyg.webcert.web.web.util.access.AccessResultExceptionHelper;

@Component
public final class DraftAccessServiceHelper {

    private final DraftAccessService draftAccessService;
    private final AccessResultExceptionHelper accessResultExceptionHelper;

    @Autowired
    public DraftAccessServiceHelper(DraftAccessService draftAccessService,
        AccessResultExceptionHelper accessResultExceptionHelper) {
        this.draftAccessService = draftAccessService;
        this.accessResultExceptionHelper = accessResultExceptionHelper;
    }

    public boolean isAllowedToCreateUtkast(String intygsTyp, Personnummer personnummer) {
        return evaluateAllowToCreateUtkast(intygsTyp, personnummer).isAllowed();
    }

    public void validateAllowToCreateUtkast(String intygsTyp, Personnummer personnummer) {
        final AccessResult accessResult = evaluateAllowToCreateUtkast(intygsTyp, personnummer);

        accessResultExceptionHelper.throwExceptionIfDenied(accessResult);
    }

    public AccessResult evaluateAllowToCreateUtkast(String intygsTyp, Personnummer personnummer) {
        return draftAccessService.allowToCreateDraft(
            AccessEvaluationParameters.create(intygsTyp, personnummer)
        );
    }

    public boolean isAllowedToReadUtkast(Utkast utkast) {
        return evaluateAllowToReadUtkast(utkast).isAllowed();
    }

    public void validateAllowToReadUtkast(Utkast utkast) {
        final AccessResult accessResult = evaluateAllowToReadUtkast(utkast);

        accessResultExceptionHelper.throwExceptionIfDenied(accessResult);
    }

    private AccessResult evaluateAllowToReadUtkast(Utkast utkast) {
        return draftAccessService.allowToReadDraft(
            AccessEvaluationParameters.create(
                utkast.getIntygsTyp(),
                utkast.getIntygTypeVersion(),
                UtkastUtil.getVardenhet(utkast),
                utkast.getPatientPersonnummer(),
                utkast.isTestIntyg()
            )
        );
    }

    public boolean isAllowToEditUtkast(AccessEvaluationParameters accessEvaluationParameters) {
        return draftAccessService.allowToEditDraft(accessEvaluationParameters).isAllowed();
    }

    public boolean isAllowToEditUtkast(Utkast draft) {
        return evaluateAllowToEditUtkast(draft).isAllowed();
    }

    public void validateAllowToEditUtkast(Utkast utkast) {
        final AccessResult accessResult = evaluateAllowToEditUtkast(utkast);
        accessResultExceptionHelper.throwExceptionIfDenied(accessResult);
    }

    private AccessResult evaluateAllowToEditUtkast(Utkast utkast) {
        return draftAccessService.allowToEditDraft(
            AccessEvaluationParameters.create(
                utkast.getIntygsTyp(),
                utkast.getIntygTypeVersion(),
                UtkastUtil.getVardenhet(utkast),
                utkast.getPatientPersonnummer(),
                utkast.isTestIntyg()
            )
        );
    }

    public boolean isAllowToDeleteUtkast(AccessEvaluationParameters accessEvaluationParameters) {
        return draftAccessService.allowToDeleteDraft(accessEvaluationParameters).isAllowed();
    }

    public boolean isAllowToDeleteUtkast(Utkast draft) {
        return evaluateAllowToDeleteUtkast(draft).isAllowed();
    }

    public void validateAllowToDeleteUtkast(Utkast utkast) {
        final AccessResult accessResult = evaluateAllowToDeleteUtkast(utkast);

        accessResultExceptionHelper.throwExceptionIfDenied(accessResult);
    }

    private AccessResult evaluateAllowToDeleteUtkast(Utkast utkast) {
        return draftAccessService.allowToDeleteDraft(
            AccessEvaluationParameters.create(
                utkast.getIntygsTyp(),
                utkast.getIntygTypeVersion(),
                UtkastUtil.getVardenhet(utkast),
                utkast.getPatientPersonnummer(),
                utkast.isTestIntyg()
            )
        );
    }

    public boolean isAllowToPrintUtkast(AccessEvaluationParameters accessEvaluationParameters) {
        return draftAccessService.allowToPrintDraft(accessEvaluationParameters).isAllowed();
    }

    public boolean isAllowToPrintUtkast(Utkast draft) {
        return evaluateAllowToPrintUtkast(draft).isAllowed();
    }

    public void validateAllowToPrintUtkast(Utkast utkast) {
        final AccessResult accessResult = evaluateAllowToPrintUtkast(utkast);

        accessResultExceptionHelper.throwExceptionIfDenied(accessResult);
    }

    private AccessResult evaluateAllowToPrintUtkast(Utkast utkast) {
        return draftAccessService.allowToPrintDraft(
            AccessEvaluationParameters.create(
                utkast.getIntygsTyp(),
                utkast.getIntygTypeVersion(),
                UtkastUtil.getVardenhet(utkast),
                utkast.getPatientPersonnummer(),
                utkast.isTestIntyg()
            )
        );
    }

    public boolean isAllowedToForwardUtkast(Utkast utkast) {
        return evaluateAllowToForwardUtkast(utkast).isAllowed();
    }

    public boolean isAllowedToForwardUtkast(AccessEvaluationParameters accessEvaluationParameters) {
        return draftAccessService.allowToForwardDraft(accessEvaluationParameters).isAllowed();
    }

    public void validateAllowToForwardDraft(Utkast utkast) {
        final AccessResult accessResult = evaluateAllowToForwardUtkast(utkast);

        accessResultExceptionHelper.throwExceptionIfDenied(accessResult);
    }

    private AccessResult evaluateAllowToForwardUtkast(Utkast utkast) {
        return draftAccessService.allowToForwardDraft(
            AccessEvaluationParameters.create(
                utkast.getIntygsTyp(),
                utkast.getIntygTypeVersion(),
                UtkastUtil.getVardenhet(utkast),
                utkast.getPatientPersonnummer(),
                utkast.isTestIntyg()
            )
        );
    }

    public boolean isAllowedToReadyForSign(Utkast utkast) {
        return evaluateAllowToReadyForSign(utkast).isAllowed();
    }

    public boolean isAllowedToReadyForSign(AccessEvaluationParameters accessEvaluationParameters) {
        return draftAccessService.allowToReadyForSign(accessEvaluationParameters).isAllowed();
    }

    public void validateAllowToReadyForSign(Utkast utkast) {
        final AccessResult accessResult = evaluateAllowToReadyForSign(utkast);

        accessResultExceptionHelper.throwExceptionIfDenied(accessResult);
    }

    private AccessResult evaluateAllowToReadyForSign(Utkast utkast) {
        return draftAccessService.allowToReadyForSign(
            AccessEvaluationParameters.create(
                utkast.getIntygsTyp(),
                utkast.getIntygTypeVersion(),
                UtkastUtil.getVardenhet(utkast),
                utkast.getPatientPersonnummer(),
                utkast.isTestIntyg()
            )
        );
    }

    public boolean isAllowedToCopyFromCandidate(Utkast utkast) {
        return evaluateAllowToCopyFromCandidate(utkast).isAllowed();
    }

    public boolean isAllowedToCopyFromCandidate(AccessEvaluationParameters accessEvaluationParameters) {
        return draftAccessService.allowToCopyFromCandidate(accessEvaluationParameters).isAllowed();
    }

    public void validateAllowToCopyFromCandidate(Utkast utkast) {
        final AccessResult accessResult = evaluateAllowToCopyFromCandidate(utkast);

        accessResultExceptionHelper.throwExceptionIfDenied(accessResult);
    }

    private AccessResult evaluateAllowToCopyFromCandidate(Utkast utkast) {
        return draftAccessService.allowToCopyFromCandidate(
            AccessEvaluationParameters.create(
                utkast.getIntygsTyp(),
                utkast.getIntygTypeVersion(),
                UtkastUtil.getVardenhet(utkast),
                utkast.getPatientPersonnummer(),
                utkast.isTestIntyg()
            )
        );
    }

    public boolean isAllowToSign(AccessEvaluationParameters accessEvaluationParameters, String certificateId) {
        return draftAccessService.allowToSignDraft(
            accessEvaluationParameters.getCertificateType(),
            accessEvaluationParameters.getCertificateTypeVersion(),
            accessEvaluationParameters.getUnit(),
            accessEvaluationParameters.getPatient(),
            certificateId
        ).isAllowed();
    }

    public boolean isAllowToSignWithConfirmation(AccessEvaluationParameters accessEvaluationParameters) {
        return draftAccessService.allowToSignWithConfirmation(accessEvaluationParameters).isAllowed();
    }
}
