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

import se.inera.intyg.common.support.model.common.internal.Vardenhet;
import se.inera.intyg.schemas.contract.Personnummer;
import se.inera.intyg.webcert.persistence.utkast.model.Utkast;
import se.inera.intyg.webcert.web.util.UtkastUtil;
import se.inera.intyg.webcert.web.web.util.access.AccessResultExceptionHelper;

/**
 * Helper used to validate if logged in user has access to perfomed certain actions on a draft/utkast.
 */
@Component
public final class DraftAccessServiceHelper {

    @Autowired
    private DraftAccessService draftAccessService;

    @Autowired
    private AccessResultExceptionHelper accessResultExceptionHelper;

    /**
     * Check if logged in user is allowed to create utkast.
     * @param intygsTyp Type of utkast to validate
     * @param personnummer  Person number of patient
     * @return  true if allowed. false if denied.
     */
    public boolean isAllowedToCreateUtkast(String intygsTyp, Personnummer personnummer) {
        return evaluateAllowToCreateUtkast(intygsTyp, personnummer).isAllowed();
    }

    /**
     * Validate if logged in user has access to create utkast of passed type.
     *
     * If access is denied a {@link se.inera.intyg.webcert.common.service.exception.WebCertServiceException} is thrown
     * containing the reason for denying.
     * @param intygsTyp Type of utkast to validate
     * @param personnummer  Person number of patient
     */
    public void validateAllowToCreateUtkast(String intygsTyp, Personnummer personnummer) {
        final AccessResult accessResult = evaluateAllowToCreateUtkast(intygsTyp, personnummer);

        accessResultExceptionHelper.throwExceptionIfDenied(accessResult);
    }

    private AccessResult evaluateAllowToCreateUtkast(String intygsTyp, Personnummer personnummer) {
        return draftAccessService.allowToCreateDraft(intygsTyp, personnummer);
    }

    /**
     * Check if logged in user is allowed to read utkast.
     * @param utkast    Utkast to validate
     * @param personnummer  Person number of patient
     * @return  true if allowed. false if denied.
     */
    public boolean isAllowedToReadUtkast(Utkast utkast, Personnummer personnummer) {
        return evaluateAllowToReadUtkast(utkast, personnummer).isAllowed();
    }

    /**
     * Validate if logged in user has access to read utkast.
     *
     * If access is denied a {@link se.inera.intyg.webcert.common.service.exception.WebCertServiceException} is thrown
     * containing the reason for denying.
     *
     * @param utkast Utkast to validate
     * @param personnummer  Person number of patient
     */
    public void validateAllowToReadUtkast(Utkast utkast, Personnummer personnummer) {
        final AccessResult accessResult = evaluateAllowToReadUtkast(utkast, personnummer);

        accessResultExceptionHelper.throwExceptionIfDenied(accessResult);
    }

    private AccessResult evaluateAllowToReadUtkast(Utkast utkast, Personnummer personnummer) {
        return draftAccessService.allowToReadDraft(
            utkast.getIntygsTyp(),
            UtkastUtil.getVardenhet(utkast),
            personnummer);
    }

    /**
     * Check if logged in user is allowed to edit utkast.
     * @param utkast    Utkast to validate
     * @return  true if allowed. false if denied.
     */
    public boolean isAllowedToEditUtkast(Utkast utkast) {
        return evaluateAllowToEditUtkast(utkast).isAllowed();
    }

    /**
     * Check if logged in user is allowed to edit utkast.
     * @param intygsTyp    Type of draft
     * @param vardenhet    Care unit of draft
     * @param personnummer  Person number of patient
     * @return  true if allowed. false if denied.
     */
    public boolean isAllowedToEditUtkast(String intygsTyp, Vardenhet vardenhet, Personnummer personnummer) {
        return draftAccessService.allowToEditDraft(intygsTyp, vardenhet, personnummer).isAllowed();
    }

    /**
     * Validate if logged in user has access to edit utkast.
     *
     * If access is denied a {@link se.inera.intyg.webcert.common.service.exception.WebCertServiceException} is thrown
     * containing the reason for denying.
     *
     * @param utkast Utkast to validate
     */
    public void validateAllowToEditUtkast(Utkast utkast) {
        final AccessResult accessResult = evaluateAllowToEditUtkast(utkast);
        accessResultExceptionHelper.throwExceptionIfDenied(accessResult);
    }

    private AccessResult evaluateAllowToEditUtkast(Utkast utkast) {
        return draftAccessService.allowToEditDraft(
            utkast.getIntygsTyp(),
            UtkastUtil.getVardenhet(utkast),
            utkast.getPatientPersonnummer());
    }

    /**
     * Check if logged in user is allowed to delete utkast.
     * @param utkast    Utkast to validate
     * @return  true if allowed. false if denied.
     */
    public boolean isAllowedToDeleteUtkast(Utkast utkast) {
        return evaluateAllowToDeleteUtkast(utkast).isAllowed();
    }

    /**
     * Check if logged in user is allowed to delete utkast.
     * @param intygsTyp    Type of draft
     * @param vardenhet    Care unit of draft
     * @param personnummer  Person number of patient
     * @return  true if allowed. false if denied.
     */
    public boolean isAllowedToDeleteUtkast(String intygsTyp, Vardenhet vardenhet, Personnummer personnummer) {
        return draftAccessService.allowToDeleteDraft(intygsTyp, vardenhet, personnummer).isAllowed();
    }

    /**
     * Validate if logged in user has access to delete utkast.
     *
     * If access is denied a {@link se.inera.intyg.webcert.common.service.exception.WebCertServiceException} is thrown
     * containing the reason for denying.
     *
     * @param utkast Utkast to validate
     */
    public void validateAllowToDeleteUtkast(Utkast utkast) {
        final AccessResult accessResult = evaluateAllowToDeleteUtkast(utkast);

        accessResultExceptionHelper.throwExceptionIfDenied(accessResult);
    }

    private AccessResult evaluateAllowToDeleteUtkast(Utkast utkast) {
        return draftAccessService.allowToDeleteDraft(
            utkast.getIntygsTyp(),
            UtkastUtil.getVardenhet(utkast),
            utkast.getPatientPersonnummer());
    }

    /**
     * Check if logged in user is allowed to print utkast.
     * @param utkast    Utkast to validate
     * @return  true if allowed. false if denied.
     */
    public boolean isAllowedToPrintUtkast(Utkast utkast) {
        return evaluateAllowToPrintUtkast(utkast).isAllowed();
    }

    /**
     * Check if logged in user is allowed to print utkast.
     * @param intygsTyp    Type of draft
     * @param vardenhet    Care unit of draft
     * @param personnummer  Person number of patient
     * @return  true if allowed. false if denied.
     */
    public boolean isAllowedToPrintUtkast(String intygsTyp, Vardenhet vardenhet, Personnummer personnummer) {
        return draftAccessService.allowToPrintDraft(intygsTyp, vardenhet, personnummer).isAllowed();
    }

    /**
     * Validate if logged in user has access to print utkast.
     *
     * If access is denied a {@link se.inera.intyg.webcert.common.service.exception.WebCertServiceException} is thrown
     * containing the reason for denying.
     *
     * @param utkast Utkast to validate
     */
    public void validateAllowToPrintUtkast(Utkast utkast) {
        final AccessResult accessResult = evaluateAllowToPrintUtkast(utkast);

        accessResultExceptionHelper.throwExceptionIfDenied(accessResult);
    }

    private AccessResult evaluateAllowToPrintUtkast(Utkast utkast) {
        return draftAccessService.allowToPrintDraft(
            utkast.getIntygsTyp(),
            UtkastUtil.getVardenhet(utkast),
            utkast.getPatientPersonnummer());
    }


    /**
     * Check if logged in user is allowed to forward utkast.
     * @param utkast    Utkast to validate
     * @return  true if allowed. false if denied.
     */
    public boolean isAllowedToForwardUtkast(Utkast utkast) {
        return evaluateAllowToForwardUtkast(utkast).isAllowed();
    }

    /**
     * Check if logged in user is allowed to forward utkast.
     * @param intygsTyp    Type of draft
     * @param vardenhet    Care unit of draft
     * @param personnummer  Person number of patient
     * @return  true if allowed. false if denied.
     */
    public boolean isAllowedToForwardUtkast(String intygsTyp, Vardenhet vardenhet, Personnummer personnummer) {
        return draftAccessService.allowToForwardDraft(intygsTyp, vardenhet, personnummer).isAllowed();
    }

    /**
     * Validate if logged in user has access to forward utkast.
     *
     * If access is denied a {@link se.inera.intyg.webcert.common.service.exception.WebCertServiceException} is thrown
     * containing the reason for denying.
     *
     * @param utkast Utkast to validate
     */
    public void validateAllowToForwardDraft(Utkast utkast) {
        final AccessResult accessResult = evaluateAllowToForwardUtkast(utkast);

        accessResultExceptionHelper.throwExceptionIfDenied(accessResult);
    }

    private AccessResult evaluateAllowToForwardUtkast(Utkast utkast) {
        return draftAccessService.allowToForwardDraft(
            utkast.getIntygsTyp(),
            UtkastUtil.getVardenhet(utkast),
            utkast.getPatientPersonnummer());
    }

    /**
     * Check if logged in user is allowed to copy from utkast.
     * @param utkast    Utkast to validate
     * @return  true if allowed. false if denied.
     */
    public boolean isAllowedToCopyFromCandidate(Utkast utkast) {
        return evaluateAllowToCopyFromCandidate(utkast).isAllowed();
    }

    /**
     * Check if logged in user is allowed to copy from utkast.
     * @param intygsTyp    Type of draft
     * @param personnummer  Person number of patient
     * @return  true if allowed. false if denied.
     */
    public boolean isAllowedToCopyFromCandidate(String intygsTyp, Vardenhet vardenhet, Personnummer personnummer) {
        return draftAccessService.allowToCopyFromCandidate(intygsTyp, personnummer).isAllowed();
    }

    /**
     * Validate if logged in user has access to copy from utkast.
     *
     * If access is denied a {@link se.inera.intyg.webcert.common.service.exception.WebCertServiceException} is thrown
     * containing the reason for denying.
     *
     * @param utkast Utkast to validate
     */
    public void validateAllowToCopyFromCandidate(Utkast utkast) {
        final AccessResult accessResult = evaluateAllowToCopyFromCandidate(utkast);

        accessResultExceptionHelper.throwExceptionIfDenied(accessResult);
    }

    private AccessResult evaluateAllowToCopyFromCandidate(Utkast utkast) {
        return draftAccessService.allowToCopyFromCandidate(
            utkast.getIntygsTyp(),
            utkast.getPatientPersonnummer());
    }
}
