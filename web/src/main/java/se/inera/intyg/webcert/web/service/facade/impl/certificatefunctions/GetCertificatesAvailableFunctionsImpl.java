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
package se.inera.intyg.webcert.web.service.facade.impl.certificatefunctions;

import static se.inera.intyg.common.support.facade.model.CertificateRelationType.COMPLEMENTED;
import static se.inera.intyg.common.support.facade.model.CertificateRelationType.REPLACED;
import static se.inera.intyg.common.support.facade.model.CertificateStatus.SIGNED;
import static se.inera.intyg.common.support.facade.model.CertificateStatus.UNSIGNED;
import static se.inera.intyg.webcert.web.web.controller.facade.dto.ResourceLinkTypeDTO.FMB;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import se.inera.intyg.common.lisjp.support.LisjpEntryPoint;
import se.inera.intyg.common.support.facade.model.Certificate;
import se.inera.intyg.common.support.facade.model.CertificateRelationType;
import se.inera.intyg.common.support.facade.model.CertificateStatus;
import se.inera.intyg.common.support.facade.model.metadata.CertificateRelations;
import se.inera.intyg.common.support.facade.model.question.QuestionType;
import se.inera.intyg.common.support.facade.util.PatientToolkit;
import se.inera.intyg.infra.security.authorities.AuthoritiesHelper;
import se.inera.intyg.infra.security.common.model.AuthoritiesConstants;
import se.inera.intyg.schemas.contract.Personnummer;
import se.inera.intyg.webcert.web.service.facade.GetCertificatesAvailableFunctions;
import se.inera.intyg.webcert.web.service.facade.question.GetQuestionsFacadeService;
import se.inera.intyg.webcert.web.service.facade.user.UserService;
import se.inera.intyg.webcert.web.service.user.WebCertUserService;
import se.inera.intyg.webcert.web.web.controller.facade.dto.ResourceLinkDTO;
import se.inera.intyg.webcert.web.web.controller.facade.dto.ResourceLinkTypeDTO;

@Service
public class GetCertificatesAvailableFunctionsImpl implements GetCertificatesAvailableFunctions {

    private static final String EDIT_NAME = "Ändra";
    private static final String EDIT_DESCRIPTION = "Ändrar intygsutkast";
    private static final String REMOVE_NAME = "Radera";
    private static final String REMOVE_DESCRIPTION = "Raderar intygsutkastet.";
    private static final String READY_FOR_SIGN_NAME = "Markera klart för signering";
    private static final String READY_FOR_SIGN_DESCRIPTION = "Utkastet markeras som klar för signering.";
    private static final String FMB_NAME = "FMB";
    private static final String FMB_DESCRIPTION = "Läs FMB - ett stöd för ifyllnad och bedömning.";
    private static final String REPLACE_NAME = "Ersätt";
    public static final String REPLACE_DESCRIPTION = "Skapar en kopia av detta intyg som du kan redigera.";
    public static final String REPLACE_DESCRIPTION_DISABLED = "Intyget har minst en ohanterad kompletteringsbegäran"
        + " och går inte att ersätta.";
    private static final String PRINT_CERTIFICATE_DESCRIPTION = "Öppnar ett fönster där du kan välja att skriva ut eller spara intyget "
        + "som PDF.";
    private static final String REVOKE_CERTIFICATE_DESCRIPTION = "Öppnar ett fönster där du kan välja att makulera intyget.";
    private static final String PRINT_NAME = "Skriv ut";
    private static final String PRINT_DRAFT_DESCRIPTION = "Öppnar ett fönster där du kan välja att skriva ut eller spara intygsutkastet "
        + "som PDF.";
    private static final String PRINT_PROTECTED_PERSON_BODY = "<div class='ic-alert ic-alert--status ic-alert--info'>\n"
        + "<i class='ic-alert__icon ic-info-icon'></i><p>Patienten har skyddade personuppgifter. Hantera utskriften varsamt.</p></div>";
    private static final String REVOKE_NAME = "Makulera";
    private static final String REVOKE_LOCKED_DRAFT_DESCRIPTION = "Öppnar ett fönster där du kan välja att makulera det låsta utkastet.";
    private static final String QUESTIONS_NAME = "Ärendekommunikation";
    private static final String QUESTIONS_DESCRIPTION = "Hantera kompletteringsbegäran, frågor och svar";
    private static final String NEW_QUESTION_NAME = "Ny fråga";
    private static final String NEW_QUESTION_DESCRIPTION = "Här kan du ställa en ny fråga till Försäkringskassan.";
    private static final String LUAENA_TYPE = "luae_na";
    private static final String LUAENA_WARNING = "Kontrollera att du använder dig av rätt läkarutlåtande";
    private static final String LUAENA_WARNING_DESCRIPTION =
        "Läkarutlåtande för aktivitetsersättning vid nedsatt arbetsförmåga är till för personer under 30 år.\n"
            + "För personer över 30 år rekommenderar vi Läkarutlåtande för sjukersättning.\n"
            + "Kontrollera en extra gång att du använder dig av rätt läkarutlåtande.";
    private final AuthoritiesHelper authoritiesHelper;
    private final WebCertUserService webCertUserService;
    private final UserService userService;
    private final GetQuestionsFacadeService getQuestionsFacadeService;
    private final CertificateSignConfirmationFunction certificateSignConfirmationFunction;
    private final DisplayPatientAddressInCertificate displayPatientAddressInCertificate;
    private final SendCertificateFunction sendCertificateFunction;
    private final CreateCertificateFromTemplateFunction createCertificateFromTemplateFunction;
    private final ShowRelatedCertificateFunction showRelatedCertificateFunction;
    private final CreateCertificateFromCandidateFunction createCertificateFromCandidateFunction;
    private final CopyCertificateFunction copyCertificateFunction;
    private final SrsFunction srsFunction;
    private final CertificateSignAndSendFunction certificateSignAndSendFunction;

    /**
     * Top level resource for getting resource links for UNSIGNED, SIGNED, LOCKED, REVOKED certificates.
     */

    @Autowired
    public GetCertificatesAvailableFunctionsImpl(AuthoritiesHelper authoritiesHelper, WebCertUserService webCertUserService,
        UserService userService, @Qualifier("GetQuestionsFacadeServiceImpl") GetQuestionsFacadeService getQuestionsFacadeService,
        CertificateSignConfirmationFunction certificateSignConfirmationFunction,
        DisplayPatientAddressInCertificate displayPatientAddressInCertificate,
        SendCertificateFunction sendCertificateFunction, CreateCertificateFromTemplateFunction createCertificateFromTemplateFunction,
        ShowRelatedCertificateFunction showRelatedCertificateFunction,
        CreateCertificateFromCandidateFunction createCertificateFromCandidateFunction, CopyCertificateFunction copyCertificateFunction,
        SrsFunction srsFunction, CertificateSignAndSendFunction certificateSignAndSendFunction) {
        this.authoritiesHelper = authoritiesHelper;
        this.webCertUserService = webCertUserService;
        this.userService = userService;
        this.getQuestionsFacadeService = getQuestionsFacadeService;
        this.certificateSignConfirmationFunction = certificateSignConfirmationFunction;
        this.displayPatientAddressInCertificate = displayPatientAddressInCertificate;
        this.sendCertificateFunction = sendCertificateFunction;
        this.createCertificateFromTemplateFunction = createCertificateFromTemplateFunction;
        this.showRelatedCertificateFunction = showRelatedCertificateFunction;
        this.createCertificateFromCandidateFunction = createCertificateFromCandidateFunction;
        this.copyCertificateFunction = copyCertificateFunction;
        this.srsFunction = srsFunction;
        this.certificateSignAndSendFunction = certificateSignAndSendFunction;
    }

    /**
     * Resource for getting ResourceLinkDTO.
     *
     * @param certificate that resource selection is based on.
     * @return list of resource links.
     */
    @Override
    public List<ResourceLinkDTO> get(Certificate certificate) {
        final var availableFunctions = new ArrayList<ResourceLinkDTO>();
        switch (certificate.getMetadata().getStatus()) {
            case UNSIGNED:
                availableFunctions.addAll(
                    getAvailableFunctionsForDraft(certificate)
                );
                break;
            case SIGNED:
                availableFunctions.addAll(
                    getAvailableFunctionsForCertificate(certificate)
                );
                break;
            case LOCKED:
                availableFunctions.addAll(
                    getAvailableFunctionsForLockedDraft(certificate)
                );
                break;
            case REVOKED:
                availableFunctions.addAll(
                    getAvailableFunctionsForRevokedCertificate(certificate)
                );
                break;
            default:
        }
        return availableFunctions;
    }

    /**
     * Determine all resource links for certificate that is in the draft stage. A rough filtering determines
     * what should be added as available links. This is due to the fact that links may not be filtered future on.
     *
     * @param certificate to be used
     * @return An array of all valid resource links
     */
    private ArrayList<ResourceLinkDTO> getAvailableFunctionsForDraft(Certificate certificate) {
        final var resourceLinks = new ArrayList<ResourceLinkDTO>();

        if (certificate.getMetadata().getType().equals(LUAENA_TYPE) && isDjupintegration()
            && patientOlderThanThirtyYearsAndTwoMonths(certificate.getMetadata().getPatient().getPersonId().getId())) {
            resourceLinks.add(
                ResourceLinkDTO.create(
                    ResourceLinkTypeDTO.WARNING_LUAENA_INTEGRATED,
                    LUAENA_WARNING,
                    LUAENA_WARNING_DESCRIPTION,
                    true
                )
            );
        }

        resourceLinks.add(
            ResourceLinkDTO.create(
                ResourceLinkTypeDTO.EDIT_CERTIFICATE,
                EDIT_NAME,
                EDIT_DESCRIPTION,
                true
            )
        );

        resourceLinks.add(getPrintResourceLink(certificate));

        resourceLinks.add(
            ResourceLinkDTO.create(
                ResourceLinkTypeDTO.REMOVE_CERTIFICATE,
                REMOVE_NAME,
                REMOVE_DESCRIPTION,
                true
            )
        );

        if (isMessagingAvailable(certificate) && isComplementingCertificate(certificate)) {
            resourceLinks.add(
                ResourceLinkDTO.create(
                    ResourceLinkTypeDTO.QUESTIONS,
                    QUESTIONS_NAME,
                    QUESTIONS_DESCRIPTION,
                    true
                )
            );
        }

        if (!webCertUserService.getUser().isLakare() && !webCertUserService.getUser().isPrivatLakare()) {
            resourceLinks.add(
                CertificateForwardFunction.createResourceLinkForDraft()
            );

            resourceLinks.add(
                ResourceLinkDTO.create(
                    ResourceLinkTypeDTO.READY_FOR_SIGN,
                    READY_FOR_SIGN_NAME,
                    READY_FOR_SIGN_DESCRIPTION,
                    true
                )
            );
        }

        if (isLisjp(certificate)) {
            resourceLinks.add(
                ResourceLinkDTO.create(
                    FMB,
                    FMB_NAME,
                    FMB_DESCRIPTION,
                    true
                )
            );
        }

        certificateSignConfirmationFunction.get(certificate, webCertUserService.getUser())
            .ifPresent(resourceLinks::add);

        displayPatientAddressInCertificate.get(certificate)
            .ifPresent(resourceLinks::add);

        createCertificateFromCandidateFunction.get(certificate)
            .ifPresent(resourceLinks::add);

        srsFunction.getSRSFullView(certificate, webCertUserService.getUser())
            .ifPresent(resourceLinks::add);

        certificateSignAndSendFunction.get(certificate)
            .ifPresent(resourceLinks::add);

        return resourceLinks;
    }

    private ArrayList<ResourceLinkDTO> getAvailableFunctionsForCertificate(Certificate certificate) {
        final var resourceLinks = new ArrayList<ResourceLinkDTO>();

        resourceLinks.add(getPrintResourceLink(certificate));

        if (isReplaceCertificateAvailable(certificate)) {
            if (hasUnhandledComplement(certificate)) {
                resourceLinks.add(
                    ResourceLinkDTO.create(
                        ResourceLinkTypeDTO.REPLACE_CERTIFICATE,
                        REPLACE_NAME,
                        REPLACE_DESCRIPTION_DISABLED,
                        false
                    )
                );
            } else {
                resourceLinks.add(
                    ResourceLinkDTO.create(
                        ResourceLinkTypeDTO.REPLACE_CERTIFICATE,
                        REPLACE_NAME,
                        REPLACE_DESCRIPTION,
                        true
                    )
                );
            }

        }

        if (isReplaceCertificateContinueAvailable(certificate)) {
            resourceLinks.add(
                ResourceLinkDTO.create(
                    ResourceLinkTypeDTO.REPLACE_CERTIFICATE_CONTINUE,
                    REPLACE_NAME,
                    REPLACE_DESCRIPTION,
                    true
                )
            );
        }

        if (CertificateRenewFunction.validate(certificate.getMetadata().getType(), certificate.getMetadata().getRelations(),
            certificate.getMetadata().getStatus(), authoritiesHelper)) {
            final var loggedInCareUnitId = userService.getLoggedInCareUnit(webCertUserService.getUser()).getId();
            final var savedCareUnitId = certificate.getMetadata().getCareUnit().getUnitId();
            resourceLinks.add(
                CertificateRenewFunction.createResourceLink(loggedInCareUnitId, savedCareUnitId, certificate.getMetadata().getType())
            );
        }

        resourceLinks.add(
            ResourceLinkDTO.create(
                ResourceLinkTypeDTO.REVOKE_CERTIFICATE,
                REVOKE_NAME,
                REVOKE_CERTIFICATE_DESCRIPTION,
                true
            )
        );

        if (isMessagingAvailable(certificate)) {
            resourceLinks.add(getQuestionsResourceLink(certificate));
        }

        if (isMessagingAvailable(certificate) && isSent(certificate) && !hasUnsignedComplement(certificate)) {
            resourceLinks.add(
                ResourceLinkDTO.create(
                    ResourceLinkTypeDTO.CREATE_QUESTIONS,
                    NEW_QUESTION_NAME,
                    NEW_QUESTION_DESCRIPTION,
                    true
                )
            );
        }

        displayPatientAddressInCertificate.get(certificate)
            .ifPresent(resourceLinks::add);

        createCertificateFromTemplateFunction.get(certificate, webCertUserService.getUser())
            .ifPresent(resourceLinks::add);

        showRelatedCertificateFunction.get(certificate, webCertUserService.getUser())
            .ifPresent(resourceLinks::add);

        sendCertificateFunction.get(certificate)
            .ifPresent(resourceLinks::add);

        srsFunction.getSRSMinimizedView(certificate, webCertUserService.getUser())
            .ifPresent(resourceLinks::add);

        return resourceLinks;
    }

    private ResourceLinkDTO getQuestionsResourceLink(Certificate certificate) {
        if (isSent(certificate)) {
            return ResourceLinkDTO.create(
                ResourceLinkTypeDTO.QUESTIONS,
                QUESTIONS_NAME,
                QUESTIONS_DESCRIPTION,
                true
            );
        } else {
            return ResourceLinkDTO.create(
                ResourceLinkTypeDTO.QUESTIONS_NOT_AVAILABLE,
                QUESTIONS_NAME,
                QUESTIONS_DESCRIPTION,
                true
            );
        }
    }

    private ArrayList<ResourceLinkDTO> getAvailableFunctionsForLockedDraft(Certificate certificate) {
        final var resourceLinks = new ArrayList<ResourceLinkDTO>();
        resourceLinks.add(getPrintResourceLink(certificate));

        resourceLinks.add(
            ResourceLinkDTO.create(ResourceLinkTypeDTO.REVOKE_CERTIFICATE,
                REVOKE_NAME,
                REVOKE_LOCKED_DRAFT_DESCRIPTION,
                true
            )
        );

        copyCertificateFunction.get(certificate)
            .ifPresent(resourceLinks::add);

        displayPatientAddressInCertificate.get(certificate)
            .ifPresent(resourceLinks::add);

        return resourceLinks;
    }

    private ArrayList<ResourceLinkDTO> getAvailableFunctionsForRevokedCertificate(Certificate certificate) {
        final var resourceLinks = new ArrayList<ResourceLinkDTO>();

        if (isMessagingAvailable(certificate)) {
            resourceLinks.add(getQuestionsResourceLink(certificate));
        }

        displayPatientAddressInCertificate.get(certificate)
            .ifPresent(resourceLinks::add);

        return resourceLinks;
    }

    private boolean isSent(Certificate certificate) {
        return certificate.getMetadata().isSent();
    }

    private boolean isComplementingCertificate(Certificate certificate) {
        return certificate.getMetadata().getRelations() != null && certificate.getMetadata().getRelations().getParent() != null
            && certificate.getMetadata().getRelations().getParent().getType() == COMPLEMENTED;
    }

    private boolean isLisjp(Certificate certificate) {
        return certificate.getMetadata().getType().equalsIgnoreCase(LisjpEntryPoint.MODULE_ID);
    }

    private boolean isDjupintegration() {
        final var user = webCertUserService.getUser();
        return user != null && user.getOrigin() != null && user.getOrigin().contains("DJUPINTEGRATION");
    }

    private boolean isReplaceCertificateAvailable(Certificate certificate) {
        return !(isReplacementUnsigned(certificate) || isReplacementSigned(certificate)
            || hasBeenComplementedBySignedCertificate(certificate));
    }

    private boolean isReplaceCertificateContinueAvailable(Certificate certificate) {
        return isReplacementUnsigned(certificate);
    }

    private boolean isReplacementUnsigned(Certificate certificate) {
        return includesChildRelation(certificate.getMetadata().getRelations(), REPLACED, UNSIGNED);
    }

    private boolean isReplacementSigned(Certificate certificate) {
        return includesChildRelation(certificate.getMetadata().getRelations(), REPLACED, SIGNED);
    }

    private boolean includesChildRelation(CertificateRelations relations, CertificateRelationType type, CertificateStatus status) {
        if (missingChildRelations(relations)) {
            return false;
        }

        return Arrays.stream(relations.getChildren()).anyMatch(
            relation -> relation.getType().equals(type) && relation.getStatus().equals(status)
        );
    }

    private boolean missingChildRelations(CertificateRelations relations) {
        return relations == null || relations.getChildren() == null;
    }

    private boolean isMessagingAvailable(Certificate certificate) {
        return authoritiesHelper.isFeatureActive(AuthoritiesConstants.FEATURE_HANTERA_FRAGOR, certificate.getMetadata().getType());
    }

    private ResourceLinkDTO getPrintResourceLink(Certificate certificate) {
        if (!certificate.getMetadata().getPatient().isProtectedPerson()) {
            return
                ResourceLinkDTO.create(
                    ResourceLinkTypeDTO.PRINT_CERTIFICATE,
                    PRINT_NAME,
                    certificate.getMetadata().getStatus() == SIGNED ? PRINT_CERTIFICATE_DESCRIPTION
                        : PRINT_DRAFT_DESCRIPTION,
                    true
                );
        } else {
            return
                ResourceLinkDTO.create(
                    ResourceLinkTypeDTO.PRINT_CERTIFICATE,
                    PRINT_NAME,
                    certificate.getMetadata().getStatus() == UNSIGNED ? PRINT_DRAFT_DESCRIPTION
                        : PRINT_CERTIFICATE_DESCRIPTION,
                    PRINT_PROTECTED_PERSON_BODY,
                    true
                );
        }
    }

    private boolean hasBeenComplementedBySignedCertificate(Certificate certificate) {
        if (certificate.getMetadata().getRelations() != null) {
            return Arrays.stream(certificate.getMetadata().getRelations().getChildren()).anyMatch(
                relation -> relation.getType().equals(COMPLEMENTED) && relation.getStatus() == SIGNED
            );
        }
        return false;
    }

    private boolean hasUnsignedComplement(Certificate certificate) {
        if (certificate.getMetadata().getRelations() != null) {
            return Arrays.stream(certificate.getMetadata().getRelations().getChildren()).anyMatch(
                relation -> relation.getType().equals(COMPLEMENTED) && relation.getStatus() == UNSIGNED
            );
        }
        return false;
    }

    private boolean hasUnhandledComplement(Certificate certificate) {
        final var questions = getQuestionsFacadeService.getQuestions(certificate.getMetadata().getId());
        if (questions != null) {
            return questions.stream().anyMatch(
                question -> !question.isHandled() && question.getType() == QuestionType.COMPLEMENT
            );
        }
        return false;
    }

    private boolean patientOlderThanThirtyYearsAndTwoMonths(String patientId) {
        final var birthDate = PatientToolkit.birthDate(Personnummer.createPersonnummer(patientId).orElseThrow());
        return LocalDate.now(ZoneId.systemDefault()).isAfter(birthDate.plusYears(30).plusMonths(2));
    }
}
