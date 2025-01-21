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
package se.inera.intyg.webcert.web.integration.util;

import java.util.*;

import se.inera.intyg.common.support.common.enumerations.KvIntygstyp;
import se.inera.intyg.common.support.modules.support.api.ModuleApi;
import se.inera.intyg.common.support.modules.support.api.dto.ValidateDraftCreationResponse;
import se.inera.intyg.infra.security.authorities.validation.AuthoritiesValidator;
import se.inera.intyg.infra.security.common.model.AuthoritiesConstants;
import se.inera.intyg.infra.security.common.model.IntygUser;
import se.inera.intyg.webcert.common.model.SekretessStatus;
import se.inera.intyg.webcert.common.service.exception.WebCertServiceErrorCodeEnum;
import se.inera.intyg.webcert.common.service.exception.WebCertServiceException;
import se.inera.intyg.webcert.web.service.utkast.dto.PreviousIntyg;
import se.riv.clinicalprocess.healthcond.certificate.v3.ResultCodeType;

public final class AuthoritiesHelperUtil {

    private static final AuthoritiesValidator authoritiesValidator = new AuthoritiesValidator();
    private static final String DRAFT = "utkast";
    private static final String CERTIFICATE = "intyg";

    // CHECKSTYLE:OFF LineLength
    public static final String DRAFT_FROM_SAME_CARE_PROVIDER_AND_UNIT_EXISTS = "Det finns ett utkast på %s för detta personnummer. Du kan inte skapa ett nytt utkast men kan däremot välja att fortsätta med det befintliga utkastet.";
    public static final String DRAFT_FROM_SAME_CARE_PROVIDER_ON_OTHER_UNIT_EXISTS = "Det finns ett utkast på %s för detta personnummer på annan vårdenhet. Du kan inte skapa ett nytt utkast men kan däremot välja att fortsätta med det befintliga utkastet.";
    public static final String DRAFT_FROM_OTHER_CARE_PROVIDER_EXISTS = "Det finns ett utkast på %1$s för detta personnummer hos annan vårdgivare. Senast skapade %1$s är det som gäller. Om du fortsätter och lämnar in %1$set så blir det därför detta %1$s som gäller.";
    public static final String CERTIFICATE_FROM_SAME_CARE_PROVIDER_AND_UNIT_EXISTS = "Det finns ett signerat %1$s för detta personnummer. Du kan inte skapa ett nytt %1$s men kan däremot välja att ersätta det befintliga %1$set.";
    public static final String CERTIFICATE_FROM_SAME_CARE_PROVIDER_ON_OTHER_UNIT_EXISTS = "Det finns ett signerat %1$s för detta personnummer på annan vårdenhet. Du kan inte skapa ett nytt %1$s men kan däremot välja att ersätta det befintliga %1$set.";
    public static final String CERTIFICATE_FROM_OTHER_CARE_PROVIDER_EXISTS = "Det finns ett signerat %1$s för detta personnummer hos annan vårdgivare. Det är inte möjligt att skapa ett nytt %1$s.";
    public static final String CERTIFICATE_FROM_OTHER_CARE_PROVIDER_EXISTS_OVERRIDE = "Det finns ett signerat %1$s för detta personnummer hos annan vårdgivare. Senast skapade %1$s är det som gäller. Om du fortsätter och lämnar in %1$set så blir det därför detta %1$s som gäller.";
    // CHECKSTYLE:ON LineLength

    private AuthoritiesHelperUtil() {
    }

    public static boolean mayNotCreateUtkastForSekretessMarkerad(SekretessStatus sekretessStatus, IntygUser user,
        String intygsTyp) {
        if (SekretessStatus.UNDEFINED.equals(sekretessStatus)) {
            throw new WebCertServiceException(WebCertServiceErrorCodeEnum.PU_PROBLEM,
                "Could not fetch sekretesstatus for patient from PU service");
        }
        boolean sekr = sekretessStatus == SekretessStatus.TRUE;
        return (sekr && !authoritiesValidator.given(user, intygsTyp)
            .privilege(AuthoritiesConstants.PRIVILEGE_HANTERA_SEKRETESSMARKERAD_PATIENT).isVerified());
    }

    public static ValidateDraftCreationResponse performUniqueAndModuleValidation(
        IntygUser user,
        String certificateType,
        Map<String, Map<String, PreviousIntyg>> previousDraftAndCertificates,
        ModuleApi moduleApi) {

        ValidateDraftCreationResponse certificateValidation =
            validateUniqueCertificate(user, certificateType, previousDraftAndCertificates);
        ValidateDraftCreationResponse draftValidation =
            validateUniqueDraft(user, certificateType, previousDraftAndCertificates);
        ValidateDraftCreationResponse certificateTypeSpecificValidation =
            validateCertificateTypeSpecifics(moduleApi, previousDraftAndCertificates);

        return getPrimaryValidationMessage(certificateValidation, draftValidation, certificateTypeSpecificValidation);
    }

    private static ValidateDraftCreationResponse getPrimaryValidationMessage(
        ValidateDraftCreationResponse certificateValidation,
        ValidateDraftCreationResponse draftValidation,
        ValidateDraftCreationResponse certificateTypeSpecificValidation) {

        if (containsError(certificateValidation)) {
            return certificateValidation;
        }

        if (containsError(draftValidation)) {
            return draftValidation;
        }

        if (containsError(certificateTypeSpecificValidation)) {
            return certificateTypeSpecificValidation;
        }

        if (containsInfo(certificateValidation)) {
            return certificateValidation;
        }

        if (containsInfo(draftValidation)) {
            return draftValidation;
        }

        if (containsInfo(certificateTypeSpecificValidation)) {
            return certificateTypeSpecificValidation;
        }

        return null;
    }

    private static boolean containsError(ValidateDraftCreationResponse validateDraftCreationResponse) {
        return validateDraftCreationResponse != null
            && validateDraftCreationResponse.getResultCode() == ResultCodeType.ERROR;
    }

    private static boolean containsInfo(ValidateDraftCreationResponse validateDraftCreationResponse) {
        return validateDraftCreationResponse != null
            && validateDraftCreationResponse.getResultCode() == ResultCodeType.INFO;
    }

    private static ValidateDraftCreationResponse validateUniqueCertificate(
        IntygUser user,
        String certificateType, Map<String,
        Map<String, PreviousIntyg>> previousDraftAndCertificates) {

        PreviousIntyg previousCertificate = previousDraftAndCertificates.get(CERTIFICATE).get(certificateType);

        if (previousCertificate != null
            && (authoritiesValidator.given(user, certificateType)
            .features(AuthoritiesConstants.FEATURE_UNIKT_INTYG).isVerified()
            || authoritiesValidator.given(user, certificateType)
            .features(AuthoritiesConstants.FEATURE_UNIKT_INTYG_INOM_VG).isVerified())) {
            if (previousCertificate.isSameVardgivare()) {
                if (previousCertificate.isSameEnhet()) {
                    return createResponse(CERTIFICATE_FROM_SAME_CARE_PROVIDER_AND_UNIT_EXISTS, ResultCodeType.ERROR,
                        certificateType);
                } else {
                    return createResponse(CERTIFICATE_FROM_SAME_CARE_PROVIDER_ON_OTHER_UNIT_EXISTS, ResultCodeType.ERROR,
                        certificateType);
                }
            } else {
                if (authoritiesValidator.given(user, certificateType)
                    .features(AuthoritiesConstants.FEATURE_UNIKT_INTYG).isVerified()) {
                    return createResponse(CERTIFICATE_FROM_OTHER_CARE_PROVIDER_EXISTS, ResultCodeType.ERROR,
                        certificateType);
                } else if (authoritiesValidator.given(user, certificateType)
                    .features(AuthoritiesConstants.FEATURE_UNIKT_INTYG_INOM_VG).isVerified()) {
                    return createResponse(CERTIFICATE_FROM_OTHER_CARE_PROVIDER_EXISTS_OVERRIDE, ResultCodeType.INFO,
                        certificateType);
                }
            }
        }

        return null;
    }

    private static ValidateDraftCreationResponse validateUniqueDraft(
        IntygUser user,
        String certificateType,
        Map<String, Map<String, PreviousIntyg>> previousDraftAndCertificates) {

        PreviousIntyg previousDraft = previousDraftAndCertificates.get(DRAFT).get(certificateType);

        if (previousDraft != null && authoritiesValidator.given(user, certificateType)
            .features(AuthoritiesConstants.FEATURE_UNIKT_UTKAST_INOM_VG).isVerified()) {
            if (previousDraft.isSameVardgivare()) {
                if (previousDraft.isSameEnhet()) {
                    return createResponse(DRAFT_FROM_SAME_CARE_PROVIDER_AND_UNIT_EXISTS, ResultCodeType.ERROR,
                        certificateType);
                } else {
                    return createResponse(DRAFT_FROM_SAME_CARE_PROVIDER_ON_OTHER_UNIT_EXISTS, ResultCodeType.ERROR,
                        certificateType);
                }
            } else {
                return createResponse(DRAFT_FROM_OTHER_CARE_PROVIDER_EXISTS, ResultCodeType.INFO, certificateType);
            }
        }

        return null;
    }

    private static ValidateDraftCreationResponse validateCertificateTypeSpecifics(
        ModuleApi moduleApi, Map<String, Map<String, PreviousIntyg>> previousDraftAndCertificates) {

        Set<String> previousCertificateSet = previousDraftAndCertificates.get(CERTIFICATE).keySet();

        return moduleApi.validateDraftCreation(previousCertificateSet).orElse(null);
    }

    private static ValidateDraftCreationResponse createResponse(String message, ResultCodeType resultCode,
        String certificateType) {
        String messageWithCertificateName = String.format(message,
            KvIntygstyp.getDisplayNameFromCodeValue(certificateType).toLowerCase());
        return new ValidateDraftCreationResponse(messageWithCertificateName, resultCode);
    }

}
