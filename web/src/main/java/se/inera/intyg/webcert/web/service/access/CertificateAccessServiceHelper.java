/*
 * Copyright (C) 2021 Inera AB (http://www.inera.se)
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

    @Autowired
    private CertificateAccessService certificateAccessService;

    @Autowired
    private AccessResultExceptionHelper accessResultExceptionHelper;

    public void validateAccessToRenewCertificate(Utlatande utlatande) {
        final AccessResult accessResult = certificateAccessService.allowToRenew(
            AccessEvaluationParameters.create(utlatande.getTyp(),
                utlatande.getTextVersion(),
                getVardenhet(utlatande),
                getPersonnummer(utlatande),
                utlatande.getGrundData().isTestIntyg()));

        accessResultExceptionHelper.throwExceptionIfDenied(accessResult);
    }

    public void validateAccessToComplementCertificate(Utlatande utlatande) {
        final AccessResult accessResult = certificateAccessService.allowToAnswerComplementQuestion(
            AccessEvaluationParameters.create(utlatande.getTyp(),
                utlatande.getTextVersion(),
                getVardenhet(utlatande),
                getPersonnummer(utlatande),
                utlatande.getGrundData().isTestIntyg()),
            true);

        accessResultExceptionHelper.throwExceptionIfDenied(accessResult);
    }

    public void validateAccessToReplaceCertificate(Utlatande utlatande) {
        final AccessResult accessResult = certificateAccessService.allowToReplace(
            AccessEvaluationParameters.create(utlatande.getTyp(),
                utlatande.getTextVersion(),
                getVardenhet(utlatande),
                getPersonnummer(utlatande),
                utlatande.getGrundData().isTestIntyg()));

        accessResultExceptionHelper.throwExceptionIfDenied(accessResult);
    }

    public void validateAllowCreateDraftFromSignedTemplate(Utlatande utlatande) {
        final AccessResult accessResult = certificateAccessService.allowToCreateDraftFromSignedTemplate(
            AccessEvaluationParameters.create(utlatande.getTyp(),
                utlatande.getTextVersion(),
                getVardenhet(utlatande),
                getPersonnummer(utlatande),
                utlatande.getGrundData().isTestIntyg()));

        accessResultExceptionHelper.throwExceptionIfDenied(accessResult);
    }

    private Vardenhet getVardenhet(Utlatande utlatande) {
        return utlatande.getGrundData().getSkapadAv().getVardenhet();
    }

    private Personnummer getPersonnummer(Utlatande utlatande) {
        return utlatande.getGrundData().getPatient().getPersonId();
    }
}
