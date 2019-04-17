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

import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import se.inera.intyg.common.db.support.DbModuleEntryPoint;
import se.inera.intyg.common.doi.support.DoiModuleEntryPoint;
import se.inera.intyg.infra.security.authorities.validation.AuthoritiesValidator;
import se.inera.intyg.infra.security.common.model.AuthoritiesConstants;
import se.inera.intyg.schemas.contract.Personnummer;
import se.inera.intyg.webcert.common.model.SekretessStatus;
import se.inera.intyg.webcert.web.service.patient.PatientDetailsResolver;
import se.inera.intyg.webcert.web.service.user.WebCertUserService;
import se.inera.intyg.webcert.web.service.user.dto.WebCertUser;
import se.inera.intyg.webcert.web.service.utkast.UtkastService;
import se.inera.intyg.webcert.web.service.utkast.dto.PreviousIntyg;

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
    public boolean allowToCreateDraft(String intygsTyp, Personnummer personnummer) {
        final WebCertUser user = webCertUserService.getUser();

        boolean hasAccess = isAuthorized(intygsTyp, user, AuthoritiesConstants.FEATURE_HANTERA_INTYGSUTKAST,
                AuthoritiesConstants.PRIVILEGE_SKRIVA_INTYG);

        if (hasAccess && !isDeceasedRuleValidForCreate(intygsTyp, personnummer)) {
            hasAccess = false;
        }

        if (hasAccess && !isInactiveUnitRuleValid(user)) {
            hasAccess = false;
        }

        // TODO: Enligt krav TBD.
        if (hasAccess && !isRenewRuleValid(user)) {
            hasAccess = false;
        }

        if (hasAccess && !isSekretessRuleValid(intygsTyp, user, personnummer)) {
            hasAccess = false;
        }

        if (hasAccess && !isUniqueUtkastRuleValid(intygsTyp, user, personnummer)) {
            hasAccess = false;
        }

        return hasAccess;
    }

    @Override
    public boolean allowToReadDraft(String intygsTyp, String enhetsId, Personnummer personnummer) {
        final WebCertUser user = webCertUserService.getUser();

        boolean hasAccess = isAuthorized(intygsTyp, user, AuthoritiesConstants.FEATURE_HANTERA_INTYGSUTKAST,
                AuthoritiesConstants.PRIVILEGE_SKRIVA_INTYG);

        if (hasAccess && !isSekretessRuleValid(intygsTyp, enhetsId, user, personnummer)) {
            hasAccess = false;
        }

        return hasAccess;
    }

    @Override
    public boolean allowToEditDraft(String intygsTyp, String enhetsId, Personnummer personnummer) {
        return allowedToHandleUtkast(intygsTyp, enhetsId, personnummer);
    }

    @Override
    public boolean allowToDeleteDraft(String intygsTyp, String enhetsId, Personnummer personnummer) {
        // TODO Fix so you cant print utkast if delete false and other unit
        final WebCertUser user = webCertUserService.getUser();

        boolean hasAccess = isAuthorized(intygsTyp, user, AuthoritiesConstants.FEATURE_HANTERA_INTYGSUTKAST,
                AuthoritiesConstants.PRIVILEGE_SKRIVA_INTYG);

        if (hasAccess && !webCertUserService.userIsLoggedInOnEnhetOrUnderenhet(enhetsId)) {
            if ((intygsTyp.equalsIgnoreCase(DbModuleEntryPoint.MODULE_ID)
                    || intygsTyp.equalsIgnoreCase(DoiModuleEntryPoint.MODULE_ID))
                    && isInactiveUnitRuleValid(user)
                    && isSekretessRuleValid(intygsTyp, enhetsId, user, personnummer)) {
                hasAccess = true;
            } else {
                hasAccess = false;
            }
        }

        return hasAccess;
    }

    @Override
    public boolean allowToSignDraft(String intygsTyp, String enhetsId, Personnummer personnummer) {
        return allowedToHandleUtkast(intygsTyp, enhetsId, personnummer);
    }

    @Override
    public boolean allowToPrintDraft(String intygsTyp, String enhetsId, Personnummer personnummer) {
        // TODO Handle print feature authority
        // TODO Fix so you cant print utkast if renewal false and other unit
        return allowedToHandleUtkast(intygsTyp, enhetsId, personnummer);
    }

    @Override
    public boolean allowToForwardDraft(String intygsTyp, String enhetsId, Personnummer personnummer) {
        return allowedToHandleUtkast(intygsTyp, enhetsId, personnummer);
    }

    private boolean allowedToHandleUtkast(String intygsTyp, String enhetsId, Personnummer personnummer) {
        final WebCertUser user = webCertUserService.getUser();

        boolean hasAccess = isAuthorized(intygsTyp, user, AuthoritiesConstants.FEATURE_HANTERA_INTYGSUTKAST,
                AuthoritiesConstants.PRIVILEGE_SKRIVA_INTYG);

        if (hasAccess && !webCertUserService.userIsLoggedInOnEnhetOrUnderenhet(enhetsId)) {
            hasAccess = false;
        }

        if (hasAccess && !isDeceasedRuleValidForEdit(intygsTyp, personnummer)) {
            hasAccess = false;
        }

        return hasAccess;
    }

    private boolean isAuthorized(String intygsTyp, WebCertUser user, String feature, String privilege) {
        final AuthoritiesValidator authoritiesValidator = new AuthoritiesValidator();
        return authoritiesValidator.given(user, intygsTyp).features(feature).privilege(privilege).isVerified();
    }

    // TODO: Refactor and move logic to intygstyp-implementation
    private boolean isDeceasedRuleValidForCreate(String intygsTyp, Personnummer personnummer) {
        boolean isDeceasedRuleValid = true;
        final boolean isAvliden = patientDetailsResolver.isAvliden(personnummer);
        if (!isAvliden || (isAvliden && intygsTyp.equalsIgnoreCase(DoiModuleEntryPoint.MODULE_ID))) {
            isDeceasedRuleValid = true;
        } else {
            isDeceasedRuleValid = false;
        }
        return isDeceasedRuleValid;
    }

    private boolean isDeceasedRuleValidForEdit(String intygsTyp, Personnummer personnummer) {
        boolean isDeceasedRuleValid = true;
        final boolean isAvliden = patientDetailsResolver.isAvliden(personnummer);
        if (isAvliden && intygsTyp.equalsIgnoreCase(DbModuleEntryPoint.MODULE_ID)) {
            isDeceasedRuleValid = false;
        }
        return isDeceasedRuleValid;
    }

    private boolean isInactiveUnitRuleValid(WebCertUser user) {
        boolean isInactiveUnitRuleValid = true;
        if (user.getParameters() != null && user.getParameters().isInactiveUnit()) {
            isInactiveUnitRuleValid = false;
        }
        return isInactiveUnitRuleValid;
    }

    private boolean isRenewRuleValid(WebCertUser user) {
        boolean isRenewRuleValid = true;
        if (user.getParameters() != null && !user.getParameters().isFornyaOk()) {
            isRenewRuleValid = false;
        }
        return isRenewRuleValid;
    }

    private boolean isSekretessRuleValid(String intygsTyp, WebCertUser user, Personnummer personnummer) {
        return isSekretessRuleValid(intygsTyp, null, user, personnummer);
    }

    private boolean isSekretessRuleValid(String intygsTyp, String enhetsId, WebCertUser user, Personnummer personnummer) {
        boolean isSekretessRuleValid = true;
        final SekretessStatus sekretessStatus = patientDetailsResolver.getSekretessStatus(personnummer);
        if (SekretessStatus.UNDEFINED.equals(sekretessStatus)) {
            // If sekretesstatus cannot be fetched, the user cannot be given authority.
            isSekretessRuleValid = false;
        } else if (SekretessStatus.TRUE.equals(sekretessStatus)) {
            final AuthoritiesValidator authoritiesValidator = new AuthoritiesValidator();
            isSekretessRuleValid = authoritiesValidator.given(user, intygsTyp)
                    .privilege(AuthoritiesConstants.PRIVILEGE_HANTERA_SEKRETESSMARKERAD_PATIENT)
                    .isVerified();

            if (enhetsId != null && webCertUserService.userIsLoggedInOnEnhetOrUnderenhet(enhetsId)) {
                isSekretessRuleValid = true;
            }
        }
        return isSekretessRuleValid;
    }

    private boolean isUniqueUtkastRuleValid(String intygsTyp, WebCertUser user, Personnummer personnummer) {
        boolean isUniqueUtkastRuleValid = true;
        if (isAnyUniqueFeatureEnabled(intygsTyp, user)) {
            final Map<String, Map<String, PreviousIntyg>> intygstypToStringToBoolean = utkastService
                    .checkIfPersonHasExistingIntyg(personnummer, user);

            final PreviousIntyg utkastExists = intygstypToStringToBoolean.get("utkast").get(intygsTyp);
            final PreviousIntyg intygExists = intygstypToStringToBoolean.get("intyg").get(intygsTyp);

            if (utkastExists != null && utkastExists.isSameVardgivare()) {
                if (isUniqueUtkastFeatureEnabled(intygsTyp, user)) {
                    isUniqueUtkastRuleValid = false;
                }
            } else {
                if (intygExists != null) {
                    if (isUniqueFeatureEnabled(intygsTyp, user)) {
                        isUniqueUtkastRuleValid = false;
                    } else if (intygExists.isSameVardgivare() && isUniqueIntygFeatureEnabled(intygsTyp, user)) {
                        isUniqueUtkastRuleValid = false;
                    }
                }
            }
        }
        return isUniqueUtkastRuleValid;
    }

    private boolean isUniqueIntygFeatureEnabled(String intygsTyp, WebCertUser user) {
        final AuthoritiesValidator authoritiesValidator = new AuthoritiesValidator();
        return authoritiesValidator.given(user, intygsTyp)
                .features(AuthoritiesConstants.FEATURE_UNIKT_INTYG_INOM_VG).isVerified();
    }

    private boolean isUniqueFeatureEnabled(String intygsTyp, WebCertUser user) {
        final AuthoritiesValidator authoritiesValidator = new AuthoritiesValidator();
        return authoritiesValidator.given(user, intygsTyp)
                .features(AuthoritiesConstants.FEATURE_UNIKT_INTYG).isVerified();
    }

    private boolean isUniqueUtkastFeatureEnabled(String intygsTyp, WebCertUser user) {
        final AuthoritiesValidator authoritiesValidator = new AuthoritiesValidator();
        return authoritiesValidator.given(user, intygsTyp)
                .features(AuthoritiesConstants.FEATURE_UNIKT_UTKAST_INOM_VG).isVerified();
    }

    private boolean isAnyUniqueFeatureEnabled(String intygsTyp, WebCertUser user) {
        final AuthoritiesValidator authoritiesValidator = new AuthoritiesValidator();
        return authoritiesValidator.given(user, intygsTyp)
                .features(AuthoritiesConstants.FEATURE_UNIKT_INTYG, AuthoritiesConstants.FEATURE_UNIKT_INTYG_INOM_VG,
                        AuthoritiesConstants.FEATURE_UNIKT_UTKAST_INOM_VG)
                .isVerified();
    }
}
