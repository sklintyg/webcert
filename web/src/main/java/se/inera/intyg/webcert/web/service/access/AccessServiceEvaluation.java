/*
 * Copyright (C) 2020 Inera AB (http://www.inera.se)
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
import java.util.StringJoiner;

import javax.validation.constraints.NotNull;

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

/**
 * Implementation used to evaluate access criterias. Set the criterias that will be considered and then call evaluate(). Make sure to set
 * basic information as user, certificateType, careUnit and patient first.
 */
public final class AccessServiceEvaluation {

    private static final String DRAFT = "utkast";
    private static final String CERTIFICATE = "intyg";

    private final WebCertUserService webCertUserService;
    private final PatientDetailsResolver patientDetailsResolver;
    private final UtkastService utkastService;

    private WebCertUser user;
    private String certificateType;
    private String certificateId;
    private List<String> privileges = new ArrayList<>();
    private List<String> features = new ArrayList<>();
    private Vardenhet careUnit;
    private Personnummer patient;

    private boolean checkPatientDeceased;
    private boolean checkInactiveCareUnit;
    private boolean checkRenew;
    private boolean checkPatientSecrecy;
    private boolean checkPatientTestIndicator;
    private boolean checkUnique;
    private boolean checkUniqueOnlyCertificate;
    private boolean checkUnit;
    private boolean allowSJF;
    private boolean isReadOnlyOperation;
    private boolean allowRenewForSameUnit;
    private boolean allowInactiveForSameUnit;
    private boolean allowDeceasedForSameUnit;
    private boolean checkTestCertificate;
    private boolean isTestCertificate;

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

    /**
     * Creates a new instance of AccessServiceEvaluation with injected services and helpers.
     *
     * @param webCertUserService Service to fetch and evaluate the current user.
     * @param patientDetailsResolver Service to fetch and evaluate the current patient.
     * @param utkastService Service to fetch drafts and certificates when evaluating certain rules.
     * @return An AccessServiceEvaluation to be used once and then thrown away.
     */
    public static AccessServiceEvaluation create(@NotNull WebCertUserService webCertUserService,
        @NotNull PatientDetailsResolver patientDetailsResolver,
        @NotNull UtkastService utkastService) {
        return new AccessServiceEvaluation(webCertUserService, patientDetailsResolver, utkastService);
    }

    /**
     * Specifies which user and certificate type this evaluation will be done for. If called more than once, the old values will be
     * overridden.
     *
     * @param user Current user.
     * @param certificateType Certificate type being evaluated.
     * @return AccessServiceEvaluation
     */
    public AccessServiceEvaluation given(@NotNull WebCertUser user, @NotNull String certificateType) {
        this.user = user;
        this.certificateType = certificateType;
        return this;
    }

    /**
     * Add a privilege to consider. This method can be called multiple times.
     *
     * @param privilege Privilege to consider
     * @return AccessServiceEvaluation
     */
    public AccessServiceEvaluation privilege(@NotNull String privilege) {
        this.privileges.add(privilege);
        return this;
    }

    /**
     * Add a privilege to consider IF the addPrivilege is true. This method can be called multiple times.
     *
     * @param privilege Privilege to consider
     * @param addPrivilege Only add privilege if true.
     * @return AccessServiceEvaluation
     */
    public AccessServiceEvaluation privilegeIf(@NotNull String privilege, @NotNull boolean addPrivilege) {
        if (addPrivilege) {
            this.privileges.add(privilege);
        }
        return this;
    }

    /**
     * Add a feature to consider. This method can be called multiple times.
     *
     * @param feature feature to consider.
     * @return AccessServiceEvaluation
     */
    public AccessServiceEvaluation feature(@NotNull String feature) {
        this.features.add(feature);
        return this;
    }

    /**
     * Add a feature to consider IF the addFeature is true. This method can be called multiple times.
     *
     * @param feature feature to consider.
     * @param addFeature Only add feature if true.
     */
    public AccessServiceEvaluation featureIf(@NotNull String feature, @NotNull boolean addFeature) {
        if (addFeature) {
            this.features.add(feature);
        }
        return this;
    }

    /**
     * Set certificate id. If called more than once, the old values will be overridden.
     *
     * @param certificateId Certificate id for current certificate.
     * @return AccessServiceEvaluation
     */
    public AccessServiceEvaluation certificateId(@NotNull String certificateId) {
        this.certificateId = certificateId;
        return this;
    }

    /**
     * Set care unit to consider. If called more than once, the old values will be overridden.
     *
     * @param careUnit Care unit to consider.
     * @return AccessServiceEvaluation
     */
    public AccessServiceEvaluation careUnit(@NotNull Vardenhet careUnit) {
        this.careUnit = careUnit;
        return this;
    }

    /**
     * Set patient to consider. If called more than once, the old values will be overridden.
     *
     * @param personnummer Patient to consider.
     * @return AccessServiceEvaluation
     */
    public AccessServiceEvaluation patient(@NotNull Personnummer personnummer) {
        this.patient = personnummer;
        return this;
    }

    /**
     * Consider if patient is deceased when evaluating. If called more than once, the old values will be overridden.
     *
     * @param allowForSameUnit Allow handling (if all other criterias are met) when user is on the same unit.
     * @return AccessServiceEvaluation
     */
    public AccessServiceEvaluation checkPatientDeceased(boolean allowForSameUnit) {
        this.checkPatientDeceased = true;
        this.allowDeceasedForSameUnit = allowForSameUnit;
        return this;
    }

    /**
     * Set certificate types that should be excluded from checkPatientDeceased. This means that the certificate types will be allowed. This
     * method can be called multiple times.
     *
     * @param certificateType Certificate types to exclude
     * @return AccessServiceEvaluation
     */
    public AccessServiceEvaluation excludeCertificateTypesForDeceased(String... certificateType) {
        this.excludeDeceasedCertificateTypes.addAll(Arrays.asList(certificateType));
        return this;
    }

    /**
     * Set certificate types that should always be invalid when checkPatientDeceased. This means that the certificate types will never be
     * allowed if patient is deceased. This method can be called multiple times.
     *
     * @param certificateType Certificate types that are invalid
     * @return AccessServiceEvaluation
     */
    public AccessServiceEvaluation invalidCertificateTypeForDeceased(String... certificateType) {
        this.invalidDeceasedCertificateTypes.addAll(Arrays.asList(certificateType));
        return this;
    }

    /**
     * Consider if patient has testIndicator flag when evaluating.
     * @return AccessServiceEvaluation
     */
    public AccessServiceEvaluation checkPatientTestIndicator() {
        this.checkPatientTestIndicator = true;
        return this;
    }

    /**
     * Consider if certificate is flagged as a test certificate.
     * @param isTestCertificate If certificate is a test certificate or not.
     * @return AccessServiceEvaluation
     */
    public AccessServiceEvaluation checkTestCertificate(boolean isTestCertificate) {
        this.checkTestCertificate = isTestCertificate;
        this.isTestCertificate = isTestCertificate;
        return this;
    }

    /**
     * Consider if parameter inactiveUnit when evaluating. If called more than once, the old values will be overridden.
     *
     * @param allowForSameUnit Allow handling (if all other criterias are met) when use ris on the same unit.
     * @return AccessServiceEvaluation
     */
    public AccessServiceEvaluation checkInactiveCareUnit(boolean allowForSameUnit) {
        this.checkInactiveCareUnit = true;
        this.allowInactiveForSameUnit = allowForSameUnit;
        return this;
    }

    /**
     * Set certificate types that should be excluded from checkInactiveCareUnit. This means that the certificate types will be allowed. This
     * method can be called multiple times.
     *
     * @param certificateType Certificate types to exclude
     * @return AccessServiceEvaluation
     */
    public AccessServiceEvaluation excludeCertificateTypesForInactive(String... certificateType) {
        this.excludeInactiveCertificateTypes.addAll(Arrays.asList(certificateType));
        return this;
    }

    /**
     * Consider if patient sekretess when evaluating. If called more than once, the old values will be overridden.
     *
     * @return AccessServiceEvaluation
     */
    public AccessServiceEvaluation checkPatientSecrecy() {
        this.checkPatientSecrecy = true;
        return this;
    }

    /**
     * Consider if parameter renewOk when evaluating. If called more than once, the old values will be overridden.
     *
     * @param allowForSameUnit Allow handling (if all other criterias are met) when user is on the same unit.
     * @return AccessServiceEvaluation
     */
    public AccessServiceEvaluation checkRenew(boolean allowForSameUnit) {
        this.checkRenew = true;
        this.allowRenewForSameUnit = allowForSameUnit;
        return this;
    }

    /**
     * Set certificate types that should be excluded from checkRenew. This means that the certificate types will be allowed. This method can
     * be called multiple times.
     *
     * @param certificateType Certificate types to exclude
     * @return AccessServiceEvaluation
     */
    public AccessServiceEvaluation excludeCertificateTypesForRenew(String... certificateType) {
        this.excludeRenewCertificateTypes.addAll(Arrays.asList(certificateType));
        return this;
    }

    /**
     * Consider unique draft/certificate rules when evaluating. If called more than once, the old values will be overridden.
     *
     * @return AccessServiceEvaluation
     */
    public AccessServiceEvaluation checkUnique() {
        this.checkUnique = true;
        this.checkUniqueOnlyCertificate = false;
        return this;
    }

    /**
     * Consider unique draft/certificate rules when evaluating. If called more than once, the old values will be overridden.
     *
     * @return AccessServiceEvaluation
     */
    public AccessServiceEvaluation checkUnique(boolean onlyCertificates) {
        this.checkUnique = true;
        this.checkUniqueOnlyCertificate = onlyCertificates;
        return this;
    }

    /**
     * Consider logged in unit when evaluating. If called more than once, the old values will be overridden.
     *
     * @param allowSJF If Sammanhållen journalföring should be considered when evaluating.
     * @param isReadOnlyOperation If the operation only includes read only.
     */
    public AccessServiceEvaluation checkUnit(boolean allowSJF, boolean isReadOnlyOperation) {
        this.allowSJF = allowSJF;
        this.isReadOnlyOperation = isReadOnlyOperation;
        this.checkUnit = true;
        return this;
    }

    /**
     * Set certificate types that should be excluded from checkUnit. This means that the certificate types will be allowed. This method can
     * be called multiple times.
     *
     * @param certificateType Certificate types to exclude
     * @return AccessServiceEvaluation
     */
    public AccessServiceEvaluation excludeCertificateTypesForUnit(String... certificateType) {
        this.excludeUnitCertificateTypes.addAll(Arrays.asList(certificateType));
        return this;
    }

    /**
     * Evaluate criterias and returns an AccessResult.
     *
     * @return AccessResult
     */
    public AccessResult evaluate() {
        Optional<AccessResult> accessResult = isAuthorized(certificateType, user, features, privileges);

        if (checkPatientDeceased && !excludeDeceasedCertificateTypes.contains(certificateType) && !accessResult.isPresent()) {
            accessResult = isDeceasedRuleValid(user, certificateType, careUnit.getEnhetsid(), patient, allowDeceasedForSameUnit,
                invalidDeceasedCertificateTypes);
        }

        if (checkPatientTestIndicator && !accessResult.isPresent()) {
            accessResult = isPatientTestIndicated(patient);
        }

        if (checkTestCertificate && !accessResult.isPresent()) {
            accessResult = isTestCertificate(isTestCertificate);
        }

        if (checkInactiveCareUnit && !excludeInactiveCertificateTypes.contains(certificateType) && !accessResult.isPresent()) {
            accessResult = isInactiveUnitRuleValid(user, careUnit.getEnhetsid(), allowInactiveForSameUnit);
        }

        if (checkRenew && !excludeRenewCertificateTypes.contains(certificateType) && !accessResult.isPresent()) {
            accessResult = isRenewRuleValid(user, careUnit.getEnhetsid(), allowRenewForSameUnit);
        }

        if (checkPatientSecrecy && !accessResult.isPresent()) {
            accessResult = isSekretessRuleValid(certificateType, careUnit.getEnhetsid(), user, patient);
        }

        if (checkUnit && !excludeUnitCertificateTypes.contains(certificateType) && !accessResult.isPresent()) {
            accessResult = isUnitRuleValid(careUnit, user, allowSJF, isReadOnlyOperation);
        }

        if (checkUnique && !accessResult.isPresent()) {
            accessResult = isUniqueUtkastRuleValid(certificateType, user, patient, checkUniqueOnlyCertificate, certificateId);
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

            String errorMessage = "Patienten avliden";
            if (invalidDeceasedCertificateTypes.contains(intygsTyp)) {
                return Optional.of(AccessResult.create(AccessResultCode.DECEASED_PATIENT, createMessage(errorMessage)));
            }

            if (isUserLoggedInOnDifferentUnit(enhetsId) || !allowDeceasedForSameUnit) {
                return Optional.of(AccessResult.create(AccessResultCode.DECEASED_PATIENT, createMessage(errorMessage)));
            }

            if (enhetsId == null && isAuthorized(intygsTyp, user, Arrays.asList(AuthoritiesConstants.FEATURE_HANTERA_INTYGSUTKAST_AVLIDEN),
                Collections.emptyList()).isPresent()) {
                return Optional.of(AccessResult.create(AccessResultCode.DECEASED_PATIENT, createMessage(errorMessage)));
            }
        }

        return Optional.empty();
    }

    private Optional<AccessResult> isPatientTestIndicated(Personnummer patient) {
        if (patientDetailsResolver.isTestIndicator(patient)) {
            return Optional.of(AccessResult.create(AccessResultCode.TEST_INDICATED_PATIENT, createMessage("Patient has Test Indicator")));
        }
        return Optional.empty();
    }

    private Optional<AccessResult> isTestCertificate(boolean isTestCertificate) {
        if (isTestCertificate) {
            return Optional.of(AccessResult.create(AccessResultCode.TEST_CERTIFICATE,
                createMessage("Certificate is flagged as test certificate")));
        }
        return Optional.empty();
    }

    private Optional<AccessResult> isInactiveUnitRuleValid(WebCertUser user, String enhetsId,
        boolean allowInactiveForSameUnit) {
        if (user.getParameters() != null && user.getParameters().isInactiveUnit()) {
            if (isUserLoggedInOnDifferentUnit(enhetsId) || !allowInactiveForSameUnit) {
                return Optional.of(AccessResult.create(AccessResultCode.INACTIVE_UNIT, createMessage("Parameter inactive unit")));
            }
        }
        return Optional.empty();
    }

    private Optional<AccessResult> isRenewRuleValid(WebCertUser user, String enhetsId, boolean allowRenewForSameUnit) {
        if (user.getParameters() != null && !user.getParameters().isFornyaOk()
            && (isUserLoggedInOnDifferentUnit(enhetsId) || !allowRenewForSameUnit)) {
            return Optional.of(AccessResult.create(AccessResultCode.RENEW_FALSE, createMessage("Parameter renewOK is false")));
        }

        return Optional.empty();
    }

    private Optional<AccessResult> isSekretessRuleValid(String intygsTyp, String enhetsId, WebCertUser user, Personnummer personnummer) {
        final SekretessStatus sekretessStatus = patientDetailsResolver.getSekretessStatus(personnummer);
        if (SekretessStatus.UNDEFINED.equals(sekretessStatus)) {
            return Optional
                .of(AccessResult.create(AccessResultCode.PU_PROBLEM, createMessage("PU-Service not available to resolve sekretess")));
        }

        if (SekretessStatus.TRUE.equals(sekretessStatus)) {
            final Optional<AccessResult> accessResult = isAuthorized(intygsTyp, user, Collections.emptyList(),
                Arrays.asList(AuthoritiesConstants.PRIVILEGE_HANTERA_SEKRETESSMARKERAD_PATIENT));
            if (accessResult.isPresent()) {
                return Optional.of(AccessResult.create(AccessResultCode.AUTHORIZATION_SEKRETESS,
                    createMessage("User missing required privilege or cannot handle sekretessmarkerad patient")));
            }
            if (isUserLoggedInOnDifferentUnit(enhetsId)) {
                return Optional.of(AccessResult.create(AccessResultCode.AUTHORIZATION_SEKRETESS_UNIT,
                    createMessage("User not logged in on same unit as draft/intyg unit for sekretessmarkerad patient.")));
            }
        }

        return Optional.empty();
    }

    private Optional<AccessResult> isUnitRuleValid(Vardenhet vardenhet, WebCertUser user, boolean allowSJF,
        boolean isReadOnlyOperation) {

        if (allowSJF && user.getParameters() != null && user.getParameters().isSjf()) {
            return Optional.empty();
        }

        final String errorMessage = "User is logged in on a different unit than the draft/certificate";
        if (user.getOrigin().equals(UserOriginType.DJUPINTEGRATION.name())) {
            final String vardgivarId = vardenhet.getVardgivare().getVardgivarid();
            if (isReadOnlyOperation && vardgivarId != null && !user.getValdVardgivare().getId().equals(vardgivarId)) {
                return Optional.of(AccessResult.create(AccessResultCode.AUTHORIZATION_DIFFERENT_UNIT,
                    createMessage(errorMessage)));
            }
        }

        if (user.getOrigin().equals(UserOriginType.READONLY.name()) && isUserLoggedInOnDifferentUnit(vardenhet.getEnhetsid())) {
            return Optional.of(AccessResult.create(AccessResultCode.AUTHORIZATION_DIFFERENT_UNIT,
                createMessage(errorMessage)));
        }

        if (!webCertUserService.isAuthorizedForUnit(vardenhet.getVardgivare().getVardgivarid(), vardenhet.getEnhetsid(),
            isReadOnlyOperation)) {
            return Optional.of(AccessResult.create(AccessResultCode.AUTHORIZATION_DIFFERENT_UNIT,
                createMessage(errorMessage)));
        }

        return Optional.empty();
    }

    private boolean isUserLoggedInOnDifferentUnit(String enhetsId) {
        return enhetsId != null && !webCertUserService.isUserAllowedAccessToUnit(enhetsId);
    }

    private Optional<AccessResult> isUniqueUtkastRuleValid(String intygsTyp, WebCertUser user, Personnummer personnummer,
        boolean onlyCertificate, String certificateId) {
        if (isAnyUniqueFeatureEnabled(intygsTyp, user)) {
            final Map<String, Map<String, PreviousIntyg>> intygstypToStringToBoolean = utkastService
                .checkIfPersonHasExistingIntyg(personnummer, user, null);

            final PreviousIntyg utkastExists = intygstypToStringToBoolean.get(DRAFT).get(intygsTyp);
            final PreviousIntyg intygExists = intygstypToStringToBoolean.get(CERTIFICATE).get(intygsTyp);

            if (!onlyCertificate && utkastExists != null && utkastExists.isSameVardgivare()) {
                if (isUniqueUtkastFeatureEnabled(intygsTyp, user)) {
                    return Optional.of(AccessResult.create(AccessResultCode.UNIQUE_DRAFT,
                        createMessage("Already exists drafts for this patient")));
                }
            } else {
                if (intygExists != null && !utkastService.isDraftCreatedFromReplacement(certificateId)) {
                    if (isUniqueFeatureEnabled(intygsTyp, user)) {
                        return Optional.of(
                                AccessResult.create(AccessResultCode.UNIQUE_CERTIFICATE,
                                createMessage("Already exists certificates for this patient")));
                    }

                    if (intygExists.isSameVardgivare() && isUniqueIntygFeatureEnabled(intygsTyp, user)) {
                        return Optional.of(AccessResult.create(AccessResultCode.UNIQUE_CERTIFICATE,
                            createMessage("Already exists certificates for this care provider on this patient")));
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

    private String createMessage(String message) {

        StringJoiner sj = new StringJoiner(", ", " [", "]");

        if (careUnit != null) {
            sj.add("unit '" + careUnit.getEnhetsid() + "'");
        }
        if (user != null) {
            sj.add("user '" + user.getHsaId() + "'");
        }

        if (sj.length() > 0) {
            return message + sj.toString();
        }
        return message;
    }
}
