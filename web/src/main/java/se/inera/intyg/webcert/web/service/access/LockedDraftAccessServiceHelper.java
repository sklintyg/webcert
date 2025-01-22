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
import se.inera.intyg.webcert.persistence.utkast.model.Utkast;
import se.inera.intyg.webcert.web.util.UtkastUtil;
import se.inera.intyg.webcert.web.web.util.access.AccessResultExceptionHelper;

@Component
public final class LockedDraftAccessServiceHelper {

    private final LockedDraftAccessService lockedDraftAccessService;
    private final AccessResultExceptionHelper accessResultExceptionHelper;

    @Autowired
    public LockedDraftAccessServiceHelper(LockedDraftAccessService lockedDraftAccessService,
        AccessResultExceptionHelper accessResultExceptionHelper) {
        this.lockedDraftAccessService = lockedDraftAccessService;
        this.accessResultExceptionHelper = accessResultExceptionHelper;
    }

    public boolean isAllowToRead(Utkast draft) {
        return evaluateAllowToRead(draft).isAllowed();
    }

    public boolean isAllowToRead(AccessEvaluationParameters accessEvaluationParameters) {
        return lockedDraftAccessService.allowToRead(accessEvaluationParameters).isAllowed();
    }

    public void validateAccessToRead(Utkast draft) {
        final AccessResult accessResult = evaluateAllowToRead(draft);

        accessResultExceptionHelper.throwExceptionIfDenied(accessResult);
    }

    private AccessResult evaluateAllowToRead(Utkast draft) {
        return lockedDraftAccessService.allowToRead(
            AccessEvaluationParameters.create(
                draft.getIntygsTyp(),
                draft.getIntygTypeVersion(),
                UtkastUtil.getVardenhet(draft),
                draft.getPatientPersonnummer(),
                draft.isTestIntyg()
            )
        );
    }

    public boolean isAllowToCopy(Utkast draft) {
        return evaluateAllowToCopy(draft).isAllowed();
    }

    public boolean isAllowToCopy(AccessEvaluationParameters accessEvaluationParameters) {
        return lockedDraftAccessService.allowToCopy(accessEvaluationParameters).isAllowed();
    }

    public void validateAccessToCopy(Utkast utkast) {
        final AccessResult accessResult = evaluateAllowToCopy(utkast);

        accessResultExceptionHelper.throwExceptionIfDenied(accessResult);
    }

    private AccessResult evaluateAllowToCopy(Utkast utkast) {
        return lockedDraftAccessService.allowToCopy(
            AccessEvaluationParameters.create(
                utkast.getIntygsTyp(),
                utkast.getIntygTypeVersion(),
                UtkastUtil.getVardenhet(utkast),
                utkast.getPatientPersonnummer(),
                utkast.isTestIntyg()
            )
        );
    }

    public boolean isAllowToInvalidate(Utkast draft) {
        return evaluateAllowToInvalidate(draft).isAllowed();
    }

    public boolean isAllowToInvalidate(AccessEvaluationParameters accessEvaluationParameters) {
        return lockedDraftAccessService.allowToInvalidate(accessEvaluationParameters).isAllowed();
    }

    public void validateAccessToInvalidate(Utkast utkast) {
        final AccessResult accessResult = evaluateAllowToInvalidate(utkast);

        accessResultExceptionHelper.throwExceptionIfDenied(accessResult);
    }

    private AccessResult evaluateAllowToInvalidate(Utkast utkast) {
        return lockedDraftAccessService.allowToInvalidate(
            AccessEvaluationParameters.create(
                utkast.getIntygsTyp(),
                utkast.getIntygTypeVersion(),
                UtkastUtil.getVardenhet(utkast),
                utkast.getPatientPersonnummer(),
                utkast.isTestIntyg()
            )
        );
    }

    public boolean isAllowToPrint(Utkast draft) {
        return evaluateAllowToPrint(draft).isAllowed();
    }

    public boolean isAllowToPrint(AccessEvaluationParameters accessEvaluationParameters) {
        return lockedDraftAccessService.allowToPrint(accessEvaluationParameters).isAllowed();
    }

    public void validateAccessToPrint(Utkast utkast) {
        final AccessResult accessResult = evaluateAllowToPrint(utkast);

        accessResultExceptionHelper.throwExceptionIfDenied(accessResult);
    }

    private AccessResult evaluateAllowToPrint(Utkast utkast) {
        return lockedDraftAccessService.allowToPrint(
            AccessEvaluationParameters.create(
                utkast.getIntygsTyp(),
                utkast.getIntygTypeVersion(),
                UtkastUtil.getVardenhet(utkast),
                utkast.getPatientPersonnummer(),
                utkast.isTestIntyg()
            )
        );
    }
}
