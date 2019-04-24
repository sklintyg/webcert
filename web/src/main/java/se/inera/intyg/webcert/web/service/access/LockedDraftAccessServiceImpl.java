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
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import se.inera.intyg.common.db.support.DbModuleEntryPoint;
import se.inera.intyg.infra.security.authorities.validation.AuthExpectationSpecification;
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
public class LockedDraftAccessServiceImpl implements LockedDraftAccessService {
    private static final String DRAFT = "utkast";
    private static final String CERTIFICATE = "intyg";

    private final WebCertUserService webCertUserService;
    private final PatientDetailsResolver patientDetailsResolver;
    private final UtkastService utkastService;

    @Autowired
    public LockedDraftAccessServiceImpl(final WebCertUserService webCertUserService,
            final PatientDetailsResolver patientDetailsResolver,
            final UtkastService utkastService) {
        this.webCertUserService = webCertUserService;
        this.patientDetailsResolver = patientDetailsResolver;
        this.utkastService = utkastService;
    }

    @Override
    public AccessResult allowToRead(String intygsTyp, String enhetsId, Personnummer personnummer) {
        final WebCertUser user = webCertUserService.getUser();

        Optional<AccessResult> accessResult = isAuthorized(intygsTyp, user, AuthoritiesConstants.FEATURE_HANTERA_INTYGSUTKAST,
                AuthoritiesConstants.PRIVILEGE_SKRIVA_INTYG);

        if (!accessResult.isPresent()) {
            accessResult = isSekretessRuleValid(intygsTyp, enhetsId, user, personnummer);
        }

        return accessResult.isPresent() ? accessResult.get() : AccessResult.noProblem();
    }

    @Override
    public AccessResult allowedToCopyLockedUtkast(String intygsTyp, String enhetsId, Personnummer personnummer) {
        final WebCertUser user = webCertUserService.getUser();

        Optional<AccessResult> accessResult = isAuthorized(intygsTyp, user, AuthoritiesConstants.FEATURE_HANTERA_INTYGSUTKAST,
                AuthoritiesConstants.PRIVILEGE_SKRIVA_INTYG);

        if (!accessResult.isPresent()) {
            accessResult = isDeceasedRuleValid(user, intygsTyp, null, personnummer, false);
        }

        if (!accessResult.isPresent()) {
            accessResult = isInactiveUnitRuleValidCreate(user);
        }

        if (!accessResult.isPresent()) {
            accessResult = isRenewRuleValid(user, intygsTyp, enhetsId, true);
        }

        if (!accessResult.isPresent()) {
            accessResult = isSekretessRuleValid(intygsTyp, enhetsId, user, personnummer);
        }

        if (!accessResult.isPresent()) {
            accessResult = isUniqueUtkastRuleValid(intygsTyp, user, personnummer);
        }

        return accessResult.isPresent() ? accessResult.get() : AccessResult.noProblem();
    }

    @Override
    public AccessResult allowedToInvalidateLockedUtkast(String intygsTyp, String enhetsId, Personnummer personnummer) {
        final WebCertUser user = webCertUserService.getUser();

        Optional<AccessResult> accessResult = isAuthorized(intygsTyp, user, AuthoritiesConstants.FEATURE_MAKULERA_INTYG,
                AuthoritiesConstants.PRIVILEGE_MAKULERA_INTYG);

        if (!accessResult.isPresent()) {
            accessResult = isDeceasedRuleValid(user, intygsTyp, enhetsId, personnummer, true);
        }

        if (!accessResult.isPresent()) {
            accessResult = isInactiveUnitRuleValid(user, enhetsId);
        }

        if (!accessResult.isPresent()) {
            accessResult = isRenewRuleValid(user, intygsTyp, enhetsId, false);
        }

        if (!accessResult.isPresent()) {
            accessResult = isSekretessRuleValid(intygsTyp, enhetsId, user, personnummer);
        }

        if (!accessResult.isPresent() && !webCertUserService.userIsLoggedInOnEnhetOrUnderenhet(enhetsId)) {
            accessResult = Optional
                    .of(AccessResult.create(AccessResultCode.AUTHORIZATION_DIFFERENT_UNIT,
                            "User is logged in on a different unit than the draft/certificate"));
        }

        return accessResult.isPresent() ? accessResult.get() : AccessResult.noProblem();
    }

    @Override
    public AccessResult allowToPrint(String intygsTyp, String enhetsId, Personnummer personnummer) {
        final WebCertUser user = webCertUserService.getUser();

        Optional<AccessResult> accessResult = isAuthorized(intygsTyp, user, AuthoritiesConstants.FEATURE_UTSKRIFT,
                AuthoritiesConstants.PRIVILEGE_VISA_INTYG);

        if (!accessResult.isPresent()) {
            accessResult = isDeceasedRuleValid(user, intygsTyp, enhetsId, personnummer, false);
        }

        if (!accessResult.isPresent()) {
            accessResult = isInactiveUnitRuleValid(user, enhetsId);
        }

        if (!accessResult.isPresent()) {
            accessResult = isRenewRuleValid(user, intygsTyp, enhetsId, false);
        }

        if (!accessResult.isPresent()) {
            accessResult = isSekretessRuleValid(intygsTyp, enhetsId, user, personnummer);
        }

        if (!accessResult.isPresent() && !webCertUserService.userIsLoggedInOnEnhetOrUnderenhet(enhetsId)) {
            accessResult = Optional
                    .of(AccessResult.create(AccessResultCode.AUTHORIZATION_DIFFERENT_UNIT,
                            "User is logged in on a different unit than the draft/certificate"));
        }

        return accessResult.isPresent() ? accessResult.get() : AccessResult.noProblem();
    }

    private Optional<AccessResult> isAuthorized(String intygsTyp, WebCertUser user, String privilege) {
        return isAuthorized(intygsTyp, user, privilege, null);
    }

    private Optional<AccessResult> isAuthorized(String intygsTyp, WebCertUser user, String feature, String privilege) {
        final AuthExpectationSpecification authExpectationSpecification = getAuthExpectationSpecification(intygsTyp, user, feature,
                privilege);
        if (authExpectationSpecification.isVerified()) {
            return Optional.empty();
        } else {
            return Optional.of(AccessResult.create(AccessResultCode.AUTHORIZATION_VALIDATION,
                    authExpectationSpecification.error()));
        }
    }

    private AuthExpectationSpecification getAuthExpectationSpecification(String intygsTyp, WebCertUser user, String feature,
            String privilege) {
        final AuthoritiesValidator authoritiesValidator = new AuthoritiesValidator();
        if (privilege == null) {
            return authoritiesValidator.given(user, intygsTyp).features(feature);
        } else if (feature == null) {
            return authoritiesValidator.given(user, intygsTyp).privilege(privilege);
        } else {
            return authoritiesValidator.given(user, intygsTyp).features(feature).privilege(privilege);
        }
    }

    private Optional<AccessResult> isDeceasedRuleValid(WebCertUser user, String intygsTyp, String enhetsId, Personnummer personnummer,
            boolean ignoreDB) {
        if (patientDetailsResolver.isAvliden(personnummer)) {
            if (!ignoreDB && DbModuleEntryPoint.MODULE_ID.equalsIgnoreCase(intygsTyp)) {
                return Optional.of(AccessResult.create(AccessResultCode.DECEASED_PATIENT, "Patienten avliden"));
            } else if (enhetsId != null && !webCertUserService.userIsLoggedInOnEnhetOrUnderenhet(enhetsId)) {
                return Optional.of(AccessResult.create(AccessResultCode.DECEASED_PATIENT, "Patienten avliden"));
            } else if (enhetsId == null
                    && isAuthorized(intygsTyp, user, AuthoritiesConstants.FEATURE_HANTERA_INTYGSUTKAST_AVLIDEN).isPresent()) {
                // TODO Need to handle this better when it is a create.
                return Optional.of(AccessResult.create(AccessResultCode.DECEASED_PATIENT, "Patienten avliden"));
            } else {
                return Optional.empty();
            }
        } else {
            return Optional.empty();
        }
    }

    private boolean isDeceasedRuleValidForEdit(String intygsTyp, Personnummer personnummer) {
        boolean isDeceasedRuleValid = true;
        final boolean isAvliden = patientDetailsResolver.isAvliden(personnummer);
        if (isAvliden && intygsTyp.equalsIgnoreCase(DbModuleEntryPoint.MODULE_ID)) {
            isDeceasedRuleValid = false;
        }
        return isDeceasedRuleValid;
    }

    private Optional<AccessResult> isInactiveUnitRuleValidCreate(WebCertUser user) {
        if (user.getParameters() != null && user.getParameters().isInactiveUnit()) {
            return Optional.of(AccessResult.create(AccessResultCode.INACTIVE_UNIT, "Parameter inactive unit"));
        } else {
            return Optional.empty();
        }
    }

    private Optional<AccessResult> isInactiveUnitRuleValid(WebCertUser user, String enhetsId) {
        if (user.getParameters() != null && user.getParameters().isInactiveUnit()
                && !webCertUserService.userIsLoggedInOnEnhetOrUnderenhet(enhetsId)) {
            return Optional.of(AccessResult.create(AccessResultCode.INACTIVE_UNIT, "Parameter inactive unit"));
        } else {
            return Optional.empty();
        }
    }

    private Optional<AccessResult> isRenewRuleValid(WebCertUser user, String intygsTyp, String enhetsId, boolean excludeAllCertificates) {
        if (excludeAllCertificates) {
            return Optional.empty();
        } else if (user.getParameters() != null && !user.getParameters().isFornyaOk()
                && !webCertUserService.userIsLoggedInOnEnhetOrUnderenhet(enhetsId)) {
            return Optional.of(AccessResult.create(AccessResultCode.RENEW_FALSE, "Parameter renewOK is false"));
        } else {
            return Optional.empty();
        }
    }

    private Optional<AccessResult> isRenewRuleValidCreate(WebCertUser user, String intygsTyp) {
        if (user.getParameters() != null && !user.getParameters().isFornyaOk()) {
            return Optional.of(AccessResult.create(AccessResultCode.RENEW_FALSE, "Parameter renewOK is false"));
        } else {
            return Optional.empty();
        }
    }

    private Optional<AccessResult> isSekretessRuleValid(String intygsTyp, String enhetsId, WebCertUser user, Personnummer personnummer) {
        final SekretessStatus sekretessStatus = patientDetailsResolver.getSekretessStatus(personnummer);
        if (SekretessStatus.UNDEFINED.equals(sekretessStatus)) {
            return Optional.of(AccessResult.create(AccessResultCode.PU_PROBLEM, "PU-Service not available to resolve sekretess"));
        } else if (SekretessStatus.TRUE.equals(sekretessStatus)) {
            Optional<AccessResult> accessResult = isAuthorized(intygsTyp, user, null,
                    AuthoritiesConstants.PRIVILEGE_HANTERA_SEKRETESSMARKERAD_PATIENT);
            if (accessResult.isPresent()) {
                return Optional.of(AccessResult.create(AccessResultCode.AUTHORIZATION_SEKRETESS,
                        "User missing required privilege or cannot handle sekretessmarkerad patient"));
            }
            if (enhetsId != null && !webCertUserService.userIsLoggedInOnEnhetOrUnderenhet(enhetsId)) {
                return Optional.of(AccessResult.create(AccessResultCode.AUTHORIZATION_SEKRETESS_UNIT,
                        "User not logged in on same unit as draft/intyg unit for sekretessmarkerad patient."));
            }
        }
        return Optional.empty();
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
                    } else if (intygExists.isSameVardgivare() && isUniqueIntygFeatureEnabled(intygsTyp, user)) {
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
