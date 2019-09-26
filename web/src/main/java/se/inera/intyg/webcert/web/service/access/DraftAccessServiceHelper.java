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
import org.springframework.stereotype.Component;
import se.inera.intyg.schemas.contract.Personnummer;
import se.inera.intyg.webcert.persistence.utkast.model.Utkast;
import se.inera.intyg.webcert.web.util.UtkastUtil;
import se.inera.intyg.webcert.web.web.util.access.AccessResultExceptionHelper;

/**
 * @author Magnus Ekstrand on 2019-09-25.
 */
@Component
public final class DraftAccessServiceHelper {

    @Autowired
    private DraftAccessService draftAccessService;

    @Autowired
    private AccessResultExceptionHelper accessResultExceptionHelper;


    public void validateAccessToCreateUtkast(String intygsTyp, Personnummer personnummer) {
        final AccessResult accessResult = draftAccessService.allowToCreateDraft(
            intygsTyp,
            personnummer);

        accessResultExceptionHelper.throwExceptionIfDenied(accessResult);
    }

    public void validateAllowToReadUtkast(Utkast utkast, Personnummer personnummer) {
        final AccessResult accessResult = draftAccessService.allowToReadDraft(
            utkast.getIntygsTyp(),
            UtkastUtil.getVardenhet(utkast),
            personnummer);

        accessResultExceptionHelper.throwExceptionIfDenied(accessResult);
    }

    public void validateAllowToEditUtkast(Utkast utkast) {
        final AccessResult accessResult = draftAccessService.allowToEditDraft(
            utkast.getIntygsTyp(),
            UtkastUtil.getVardenhet(utkast),
            utkast.getPatientPersonnummer());

        accessResultExceptionHelper.throwExceptionIfDenied(accessResult);
    }

    public void validateAllowToDeleteUtkast(Utkast utkast) {
        final AccessResult accessResult = draftAccessService.allowToDeleteDraft(
            utkast.getIntygsTyp(),
            UtkastUtil.getVardenhet(utkast),
            utkast.getPatientPersonnummer());

        accessResultExceptionHelper.throwExceptionIfDenied(accessResult);
    }

    public void verifyAccessToForwardDraft(Utkast utkast) {
        final AccessResult accessResult = draftAccessService.allowToForwardDraft(
            utkast.getIntygsTyp(),
            UtkastUtil.getVardenhet(utkast),
            utkast.getPatientPersonnummer());

        accessResultExceptionHelper.throwExceptionIfDenied(accessResult);
    }

    public void verifyAccessToCopyFromCandidate(Utkast utkast) {
        // Verify access
        final AccessResult accessResult = draftAccessService.allowToCopyFromCandidate(
            utkast.getIntygsTyp(),
            utkast.getPatientPersonnummer());

        accessResultExceptionHelper.throwExceptionIfDenied(accessResult);
    }


}
