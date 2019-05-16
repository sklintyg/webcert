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

import java.util.ArrayList;
import java.util.Arrays;
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

public final class AccessServiceEvaluation {
    private static final String DRAFT = "utkast";
    private static final String CERTIFICATE = "intyg";

    private final WebCertUserService webCertUserService;
    private final PatientDetailsResolver patientDetailsResolver;
    private final UtkastService utkastService;

    private WebCertUser user;
    private String certificateType;
    private List<String> privileges = new ArrayList<>();
    private List<String> features = new ArrayList<>();
    private Vardenhet careUnit;
    private Personnummer patient;

    private boolean checkPatientDeceased;
    private boolean checkInactiveCareUnit;
    private boolean checkRenew;
    private boolean checkPatientSecrecy;
    private boolean checkUnique;
    private boolean checkUnit;
    private boolean allowSJF;
    private boolean isReadOnlyOperation;
    private boolean allowRenewForSameUnit;
    private boolean allowInactiveForSameUnit;
    private boolean allowDeceasedForSameUnit;

    private List<String> excludeRenewCertificateTypes = new ArrayList<>();
    private List<String> excludeUnitCertificateTypes = new ArrayList<>();
    private List<String> excludeDeceasedCertificateTypes = new ArrayList<>();
    private List<String> excludeInactiveCertificateTypes = new ArrayList<>();

    private List<String> invalidDeceasedCertificateTypes = new ArrayList<>();

    private AccessServiceEvaluation(WebCertUserService webCertUserService,
            PatientDetailsResolver patientDetailsResolver,
            UtkastService utkastService) {
        this.webCertUserService = webCertUserService;
        this.patientDetailsResolver = patientDetailsResolver;
        this.utkastService = utkastService;
    }

    public static AccessServiceEvaluation create(WebCertUserService webCertUserService,
            PatientDetailsResolver patientDetailsResolver,
            UtkastService utkastService) {
        return new AccessServiceEvaluation(webCertUserService, patientDetailsResolver, utkastService);
    }

    public AccessServiceEvaluation given(WebCertUser user, String certificateType) {
        this.user = user;
        this.certificateType = certificateType;
        return this;
    }

    public AccessServiceEvaluation privilege(String privilege) {
        this.privileges.add(privilege);
        return this;
    }

    public AccessServiceEvaluation privilegeIf(String privilege, boolean addPrivilege) {
        if (addPrivilege) {
            this.privileges.add(privilege);
        }
        return this;
    }

    public AccessServiceEvaluation feature(String feature) {
        this.features.add(feature);
        return this;
    }

    public AccessServiceEvaluation featureIf(String feature, boolean addFeature) {
        if (addFeature) {
            this.features.add(feature);
        }
        return this;
    }

    public AccessServiceEvaluation careUnit(Vardenhet careUnit) {
        this.careUnit = careUnit;
        return this;
    }

    public AccessServiceEvaluation patient(Personnummer personnummer) {
        this.patient = personnummer;
        return this;
    }

    public AccessServiceEvaluation checkPatientDeceased(boolean allowForSameUnit) {
        this.checkPatientDeceased = true;
        this.allowDeceasedForSameUnit = allowForSameUnit;
        return this;
    }

    public AccessServiceEvaluation excludeCertificateTypesForDeceased(String... moduleId) {
        this.excludeDeceasedCertificateTypes.addAll(Arrays.asList(moduleId));
        return this;
    }

    public AccessServiceEvaluation invalidCertificateTypeForDeceased(String... moduleId) {
        this.invalidDeceasedCertificateTypes.addAll(Arrays.asList(moduleId));
        return this;
    }

    public AccessServiceEvaluation checkInactiveCareUnit(boolean allowForSameUnit) {
        this.checkInactiveCareUnit = true;
        this.allowInactiveForSameUnit = allowForSameUnit;
        return this;
    }

    public AccessServiceEvaluation excludeCertificateTypesForInactive(String... moduleId) {
        this.excludeInactiveCertificateTypes.addAll(Arrays.asList(moduleId));
        return this;
    }

    public AccessServiceEvaluation checkPatientSecrecy() {
        this.checkPatientSecrecy = true;
        return this;
    }

    public AccessServiceEvaluation checkRenew(boolean allowForSameUnit) {
        this.checkRenew = true;
        this.allowRenewForSameUnit = allowForSameUnit;
        return this;
    }

    public AccessServiceEvaluation excludeCertificateTypesForRenew(String... moduleId) {
        this.excludeRenewCertificateTypes.addAll(Arrays.asList(moduleId));
        return this;
    }

    public AccessServiceEvaluation checkUnique() {
        this.checkUnique = true;
        return this;
    }

    public AccessServiceEvaluation checkUnit(boolean allowSJF, boolean isReadOnlyOperation) {
        this.allowSJF = allowSJF;
        this.isReadOnlyOperation = isReadOnlyOperation;
        this.checkUnit = true;
        return this;
    }

    public AccessServiceEvaluation excludeCertificateTypesForUnit(String... moduleId) {
        this.excludeUnitCertificateTypes.addAll(Arrays.asList(moduleId));
        return this;
    }

    public AccessResult evaluate() {
        Optional<AccessResult> accessResult = isAuthorized(certificateType, user, features, privileges);

        if (checkPatientDeceased && !excludeDeceasedCertificateTypes.contains(certificateType) && !accessResult.isPresent()) {
            accessResult = isDeceasedRuleValid(user, certificateType, careUnit.getEnhetsid(), patient, allowDeceasedForSameUnit,
                    invalidDeceasedCertificateTypes);
        }

        if (checkInactiveCareUnit && !excludeInactiveCertificateTypes.contains(certificateType) && !accessResult.isPresent()) {
            accessResult = isInactiveUnitRuleValid(user, certificateType, careUnit.getEnhetsid(), allowInactiveForSameUnit);
        }

        if (checkRenew && !excludeRenewCertificateTypes.contains(certificateType) && !accessResult.isPresent()) {
            accessResult = isRenewRuleValid(user, certificateType, careUnit.getEnhetsid(), allowRenewForSameUnit);
        }

        if (checkPatientSecrecy && !accessResult.isPresent()) {
            accessResult = isSekretessRuleValid(certificateType, careUnit.getEnhetsid(), user, patient);
        }

        if (checkUnit && !excludeUnitCertificateTypes.contains(certificateType) && !accessResult.isPresent()) {
            accessResult = isUnitRuleValid(certificateType, careUnit, user, allowSJF, isReadOnlyOperation);
        }

        if (checkUnique && !accessResult.isPresent()) {
            accessResult = isUniqueUtkastRuleValid(certificateType, user, patient);
        }

        return accessResult.isPresent() ? accessResult.get() : AccessResult.noProblem();
    }

    private Optional<AccessResult> isAuthorized(String intygsTyp, WebCertUser user, List<String> features, List<String> privilege) {
        final AuthExpectationSpecification authExpectationSpecification = getAuthExpectationSpecification(
                intygsTyp,
                user,
                features,
                privilege);

        if (authExpectationSpecification.isVerified()) {
            return Optional.empty();
        }

        return Optional.of(AccessResult.create(AccessResultCode.AUTHORIZATION_VALIDATION, authExpectationSpecification.error()));
    }

    private AuthExpectationSpecification getAuthExpectationSpecification(String intygsTyp, WebCertUser user, List<String> features,
            List<String> privileges) {
        final AuthoritiesValidator authoritiesValidator = new AuthoritiesValidator();

        final AuthExpectationSpecification authExpectationSpecification = authoritiesValidator.given(user, intygsTyp);

        for (String privilege : privileges) {
            authExpectationSpecification.privilege(privilege);
        }

        for (String feature : features) {
            authExpectationSpecification.features(feature);
        }

        return authExpectationSpecification;
    }

    private Optional<AccessResult> isDeceasedRuleValid(WebCertUser user, String intygsTyp, String enhetsId, Personnummer personnummer,
            boolean allowDeceasedForSameUnit, List<String> invalidDeceasedCertificateTypes) {

        if (patientDetailsResolver.isAvliden(personnummer)) {

            if (invalidDeceasedCertificateTypes.contains(intygsTyp)) {
                return Optional.of(AccessResult.create(AccessResultCode.DECEASED_PATIENT, "Patienten avliden"));
            }

            if (isUserLoggedInOnDifferentUnit(enhetsId) || !allowDeceasedForSameUnit) {
                return Optional.of(AccessResult.create(AccessResultCode.DECEASED_PATIENT, "Patienten avliden"));
            }

            if (enhetsId == null && isAuthorized(intygsTyp, user, Arrays.asList(AuthoritiesConstants.FEATURE_HANTERA_INTYGSUTKAST_AVLIDEN),
                    Collections.emptyList()).isPresent()) {
                return Optional.of(AccessResult.create(AccessResultCode.DECEASED_PATIENT, "Patienten avliden"));
            }
        }

        return Optional.empty();
    }

    private Optional<AccessResult> isInactiveUnitRuleValid(WebCertUser user, String intygsTyp, String enhetsId,
            boolean allowInactiveForSameUnit) {
        if (user.getParameters() != null && user.getParameters().isInactiveUnit()) {
            if (isUserLoggedInOnDifferentUnit(enhetsId) || !allowInactiveForSameUnit) {
                return Optional.of(AccessResult.create(AccessResultCode.INACTIVE_UNIT, "Parameter inactive unit"));
            }
        }
        return Optional.empty();
    }

    private Optional<AccessResult> isRenewRuleValid(WebCertUser user, String intygsTyp, String enhetsId, boolean allowRenewForSameUnit) {

        if (user.getParameters() != null && !user.getParameters().isFornyaOk()
                && (isUserLoggedInOnDifferentUnit(enhetsId) || !allowRenewForSameUnit)) {
            return Optional.of(AccessResult.create(AccessResultCode.RENEW_FALSE, "Parameter renewOK is false"));
        }

        return Optional.empty();
    }

    private Optional<AccessResult> isSekretessRuleValid(String intygsTyp, String enhetsId, WebCertUser user, Personnummer personnummer) {
        final SekretessStatus sekretessStatus = patientDetailsResolver.getSekretessStatus(personnummer);
        if (SekretessStatus.UNDEFINED.equals(sekretessStatus)) {
            return Optional.of(AccessResult.create(AccessResultCode.PU_PROBLEM, "PU-Service not available to resolve sekretess"));
        }

        if (SekretessStatus.TRUE.equals(sekretessStatus)) {
            final Optional<AccessResult> accessResult = isAuthorized(intygsTyp, user, Collections.emptyList(),
                    Arrays.asList(AuthoritiesConstants.PRIVILEGE_HANTERA_SEKRETESSMARKERAD_PATIENT));
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

    private Optional<AccessResult> isUnitRuleValid(String intygsTyp, Vardenhet vardenhet, WebCertUser user, boolean allowSJF,
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

        if (!webCertUserService.isAuthorizedForUnit(vardenhet.getVardgivare().getVardgivarid(), vardenhet.getEnhetsid(),
                isReadOnlyOperation)) {
            return Optional.of(AccessResult.create(AccessResultCode.AUTHORIZATION_DIFFERENT_UNIT,
                    "User is logged in on a different unit than the draft/certificate"));
        }

        return Optional.empty();
    }

    private boolean isUserLoggedInOnDifferentUnit(String enhetsId) {
        return enhetsId != null && !webCertUserService.userIsLoggedInOnEnhetOrUnderenhet(enhetsId);
    }

    private Optional<AccessResult> isUniqueUtkastRuleValid(String intygsTyp, WebCertUser user, Personnummer personnummer) {
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
