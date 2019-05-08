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

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import se.inera.intyg.common.support.model.common.internal.Vardenhet;
import se.inera.intyg.infra.security.authorities.validation.AuthExpectationSpecification;
import se.inera.intyg.infra.security.authorities.validation.AuthoritiesValidator;
import se.inera.intyg.infra.security.common.model.AuthoritiesConstants;
import se.inera.intyg.infra.security.common.model.UserOriginType;
import se.inera.intyg.schemas.contract.Personnummer;
import se.inera.intyg.webcert.common.model.SekretessStatus;
import se.inera.intyg.webcert.web.service.patient.PatientDetailsResolver;
import se.inera.intyg.webcert.web.service.user.WebCertUserService;
import se.inera.intyg.webcert.web.service.user.dto.WebCertUser;
import se.inera.intyg.webcert.web.service.utkast.UtkastService;
import se.inera.intyg.webcert.web.service.utkast.dto.PreviousIntyg;

public abstract class AccessServiceImpl {
    private static final String DRAFT = "utkast";
    private static final String CERTIFICATE = "intyg";

    private final WebCertUserService webCertUserService;
    private final PatientDetailsResolver patientDetailsResolver;
    private final UtkastService utkastService;

    public AccessServiceImpl(WebCertUserService webCertUserService, PatientDetailsResolver patientDetailsResolver,
            UtkastService utkastService) {
        this.webCertUserService = webCertUserService;
        this.patientDetailsResolver = patientDetailsResolver;
        this.utkastService = utkastService;
    }

    protected WebCertUser getUser() {
        return webCertUserService.getUser();
    }

    boolean isUserLoggedInOnDifferentUnit(String enhetsId) {
        return enhetsId != null && !webCertUserService.userIsLoggedInOnEnhetOrUnderenhet(enhetsId);
    }

    Optional<AccessResult> isUnitRuleValid(String intygsTyp, Vardenhet vardenhet, WebCertUser user, boolean allowSJF,
            boolean isReadOnlyOperation) {

        if (allowSJF && user.getParameters() != null && user.getParameters().isSjf()) {
            return Optional.empty();
        }

        if (user.getOrigin().equals(UserOriginType.DJUPINTEGRATION.name())) {
            final String vardgivarId = vardenhet.getVardgivare().getVardgivarid();
            if (isReadOnlyOperation && vardgivarId != null && !user.getValdVardgivare().getId().equals(vardgivarId)) {
                return Optional.of(AccessResult.create(AccessResultCode.AUTHORIZATION_DIFFERENT_UNIT,
                        "User is logged in on a different unit than the draft/certificate"));
            }
        }

        if (user.getOrigin().equals(UserOriginType.READONLY.name()) && isUserLoggedInOnDifferentUnit(vardenhet.getEnhetsid())) {
            return Optional.of(AccessResult.create(AccessResultCode.AUTHORIZATION_DIFFERENT_UNIT,
                    "User is logged in on a different unit than the draft/certificate"));
        }

        if (!user.getIdsOfSelectedVardenhet().contains(vardenhet.getEnhetsid())) {
            return Optional.of(AccessResult.create(AccessResultCode.AUTHORIZATION_DIFFERENT_UNIT,
                    "User is logged in on a different unit than the draft/certificate"));
        }

        return Optional.empty();
    }

    Optional<AccessResult> isSJFRuleValid(String intygsTyp, Vardenhet vardenhet, WebCertUser user, boolean isReadOnlyOperation) {
        if (user.getParameters() == null || !user.getParameters().isSjf()) {
            if (!webCertUserService.isAuthorizedForUnit(vardenhet.getVardgivare().getVardgivarid(), vardenhet.getEnhetsid(),
                    isReadOnlyOperation)) {
                return Optional.of(AccessResult.create(AccessResultCode.AUTHORIZATION_DIFFERENT_UNIT,
                        "User is logged in on a different unit than the draft/certificate"));
            }
        }

        return Optional.empty();
    }

    private Optional<AccessResult> isAuthorized(String intygsTyp, WebCertUser user, String privilege) {
        return isAuthorized(intygsTyp, user, privilege, null);
    }

    Optional<AccessResult> isAuthorized(String intygsTyp, WebCertUser user, String feature, String privilege) {
        final AuthExpectationSpecification authExpectationSpecification = getAuthExpectationSpecification(intygsTyp, user, feature,
                privilege);

        if (authExpectationSpecification.isVerified()) {
            return Optional.empty();
        }

        return Optional.of(AccessResult.create(AccessResultCode.AUTHORIZATION_VALIDATION, authExpectationSpecification.error()));
    }

    private AuthExpectationSpecification getAuthExpectationSpecification(String intygsTyp, WebCertUser user, String feature,
            String privilege) {
        final AuthoritiesValidator authoritiesValidator = new AuthoritiesValidator();

        if (privilege == null) {
            return authoritiesValidator.given(user, intygsTyp).features(feature);
        }

        if (feature == null) {
            return authoritiesValidator.given(user, intygsTyp).privilege(privilege);
        }

        return authoritiesValidator.given(user, intygsTyp).features(feature).privilege(privilege);
    }

    Optional<AccessResult> isDeceasedRuleValid(WebCertUser user, String intygsTyp, String enhetsId, Personnummer personnummer,
            List<String> invalidIntygsTyp) {
        return isDeceasedRuleValid(user, intygsTyp, enhetsId, personnummer, invalidIntygsTyp, Collections.emptyList());
    }

    Optional<AccessResult> isDeceasedRuleValid(WebCertUser user, String intygsTyp, String enhetsId, Personnummer personnummer,
            List<String> invalidIntygsTyp, List<String> excludeIntygsTyp) {

        if (patientDetailsResolver.isAvliden(personnummer)) {

            if (excludeIntygsTyp.contains(intygsTyp)) {
                return Optional.empty();
            }

            if (invalidIntygsTyp.contains(intygsTyp)) {
                return Optional.of(AccessResult.create(AccessResultCode.DECEASED_PATIENT, "Patienten avliden"));
            }

            if (isUserLoggedInOnDifferentUnit(enhetsId)) {
                return Optional.of(AccessResult.create(AccessResultCode.DECEASED_PATIENT, "Patienten avliden"));
            }

            if (enhetsId == null && isAuthorized(intygsTyp, user, AuthoritiesConstants.FEATURE_HANTERA_INTYGSUTKAST_AVLIDEN).isPresent()) {
                return Optional.of(AccessResult.create(AccessResultCode.DECEASED_PATIENT, "Patienten avliden"));
            }
        }

        return Optional.empty();
    }

    Optional<AccessResult> isRenewRuleValid(WebCertUser user, String intygsTyp, String enhetsId, List<String> excludeIntygsTyper) {
        return isRenewRuleValid(user, intygsTyp, enhetsId, excludeIntygsTyper, false);
    }

    Optional<AccessResult> isRenewRuleValid(WebCertUser user, String intygsTyp, String enhetsId, List<String> excludeIntygsTyper,
            boolean skipLoggedInUnit) {

        if (excludeIntygsTyper.contains("ALL") || excludeIntygsTyper.contains(intygsTyp)) {
            return Optional.empty();
        }

        if (user.getParameters() != null && !user.getParameters().isFornyaOk()
                && (skipLoggedInUnit || isUserLoggedInOnDifferentUnit(enhetsId))) {
            return Optional.of(AccessResult.create(AccessResultCode.RENEW_FALSE, "Parameter renewOK is false"));
        }

        return Optional.empty();
    }

    Optional<AccessResult> isRenewRuleValidCreate(WebCertUser user, String intygsTyp) {
        if (user.getParameters() != null && !user.getParameters().isFornyaOk()) {
            return Optional.of(AccessResult.create(AccessResultCode.RENEW_FALSE, "Parameter renewOK is false"));
        }

        return Optional.empty();
    }

    Optional<AccessResult> isInactiveUnitRuleValidCreate(WebCertUser user) {

        if (user.getParameters() != null && user.getParameters().isInactiveUnit()) {
            return Optional.of(AccessResult.create(AccessResultCode.INACTIVE_UNIT, "Parameter inactive unit"));
        }

        return Optional.empty();
    }

    Optional<AccessResult> isInactiveUnitRuleValid(WebCertUser user, String intygsTyp, String enhetsId) {
        return isInactiveUnitRuleValid(user, intygsTyp, enhetsId, Collections.emptyList());
    }

    Optional<AccessResult> isInactiveUnitRuleValid(WebCertUser user, String intygsTyp, String enhetsId, List<String> excludeTypes) {
        if (user.getParameters() != null && user.getParameters().isInactiveUnit()) {

            if (excludeTypes.contains(intygsTyp)) {
                return Optional.empty();
            }

            if (isUserLoggedInOnDifferentUnit(enhetsId)) {
                return Optional.of(AccessResult.create(AccessResultCode.INACTIVE_UNIT, "Parameter inactive unit"));
            }
        }
        return Optional.empty();
    }

    Optional<AccessResult> isSekretessRuleValid(String intygsTyp, String enhetsId, WebCertUser user, Personnummer personnummer) {
        final SekretessStatus sekretessStatus = patientDetailsResolver.getSekretessStatus(personnummer);
        if (SekretessStatus.UNDEFINED.equals(sekretessStatus)) {
            return Optional.of(AccessResult.create(AccessResultCode.PU_PROBLEM, "PU-Service not available to resolve sekretess"));
        }

        if (SekretessStatus.TRUE.equals(sekretessStatus)) {
            final Optional<AccessResult> accessResult = isAuthorized(intygsTyp, user, null,
                    AuthoritiesConstants.PRIVILEGE_HANTERA_SEKRETESSMARKERAD_PATIENT);
            if (accessResult.isPresent()) {
                return Optional.of(AccessResult.create(AccessResultCode.AUTHORIZATION_SEKRETESS,
                        "User missing required privilege or cannot handle sekretessmarkerad patient"));
            }
            if (isUserLoggedInOnDifferentUnit(enhetsId)) {
                return Optional.of(AccessResult.create(AccessResultCode.AUTHORIZATION_SEKRETESS_UNIT,
                        "User not logged in on same unit as draft/intyg unit for sekretessmarkerad patient."));
            }
        }

        return Optional.empty();
    }

    Optional<AccessResult> isUniqueUtkastRuleValid(String intygsTyp, WebCertUser user, Personnummer personnummer) {
        if (isAnyUniqueFeatureEnabled(intygsTyp, user)) {
            final Map<String, Map<String, PreviousIntyg>> intygstypToStringToBoolean = utkastService
                    .checkIfPersonHasExistingIntyg(personnummer, user);

            final PreviousIntyg utkastExists = intygstypToStringToBoolean.get(DRAFT).get(intygsTyp);
            final PreviousIntyg intygExists = intygstypToStringToBoolean.get(CERTIFICATE).get(intygsTyp);

            if (utkastExists != null && utkastExists.isSameVardgivare()) {
                if (isUniqueUtkastFeatureEnabled(intygsTyp, user)) {
                    return Optional.of(AccessResult.create(AccessResultCode.UNIQUE_DRAFT,
                            "Already exists drafts for this patient"));
                }
            } else {
                if (intygExists != null) {
                    if (isUniqueFeatureEnabled(intygsTyp, user)) {
                        return Optional.of(
                                AccessResult.create(AccessResultCode.UNIQUE_CERTIFICATE,
                                        "Already exists certificates for this patient"));
                    }

                    if (intygExists.isSameVardgivare() && isUniqueIntygFeatureEnabled(intygsTyp, user)) {
                        return Optional.of(AccessResult.create(AccessResultCode.UNIQUE_CERTIFICATE,
                                "Already exists certificates for this care provider on this patient"));
                    }
                }
            }
        }
        return Optional.empty();
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
