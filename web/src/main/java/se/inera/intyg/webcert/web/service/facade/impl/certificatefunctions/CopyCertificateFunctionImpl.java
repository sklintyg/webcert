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

import static se.inera.intyg.common.support.facade.model.CertificateRelationType.COPIED;
import static se.inera.intyg.common.support.facade.model.CertificateStatus.LOCKED;
import static se.inera.intyg.common.support.facade.model.CertificateStatus.UNSIGNED;
import static se.inera.intyg.webcert.web.service.facade.impl.CertificateMessageType.CERTIFICATE_ON_DIFFERENT_CARE_PROVIDER;
import static se.inera.intyg.webcert.web.service.facade.impl.CertificateMessageType.DRAFT_ON_DIFFERENT_CARE_PROVIDER;

import java.util.Arrays;
import java.util.Optional;
import org.springframework.stereotype.Component;
import se.inera.intyg.common.doi.support.DoiModuleEntryPoint;
import se.inera.intyg.common.support.facade.model.Certificate;
import se.inera.intyg.common.support.facade.model.metadata.CertificateRelations;
import se.inera.intyg.schemas.contract.Personnummer;
import se.inera.intyg.webcert.web.service.facade.CertificateTypeMessageService;
import se.inera.intyg.webcert.web.service.facade.impl.CertificateMessage;
import se.inera.intyg.webcert.web.service.facade.impl.CertificateMessageType;
import se.inera.intyg.webcert.web.web.controller.facade.dto.ResourceLinkDTO;
import se.inera.intyg.webcert.web.web.controller.facade.dto.ResourceLinkTypeDTO;

@Component
public class CopyCertificateFunctionImpl implements CopyCertificateFunction {

    private static final String COPY_NAME = "Kopiera";
    private static final String COPY_DESCRIPTION = "Skapar en redigerbar kopia av utkastet på den enheten du är inloggad på.";

    private static final String COPY_DESCRIPTION_BODY = "<p>"
        + "Genom att kopiera ett låst intygsutkast skapas ett nytt utkast med samma information som i det ursprungliga "
        + "låsta utkastet. Du kan redigera utkastet innan du signerar det. Det ursprungliga låsta utkastet finns kvar."
        + "</p>"
        + "<br/>"
        + "<p>Det nya utkastet skapas på den enhet du är inloggad på.</p>";

    private static final String COPY_DESCRIPTION_BODY_WITH_MESSAGE = "<div class='ic-alert ic-alert--status ic-alert--observe'>\n"
        + "<i class='ic-alert__icon ic-observe-icon'></i><p>%s</p></div><br/>" + COPY_DESCRIPTION_BODY;

    private final CertificateTypeMessageService certificateTypeMessageService;

    public CopyCertificateFunctionImpl(CertificateTypeMessageService certificateTypeMessageService) {
        this.certificateTypeMessageService = certificateTypeMessageService;
    }

    @Override
    public Optional<ResourceLinkDTO> get(Certificate certificate) {
        if (certificate.getMetadata().getStatus() != LOCKED) {
            return Optional.empty();
        }

        final var patientId = Personnummer.createPersonnummer(certificate.getMetadata().getPatient().getPersonId().getId()).orElseThrow();
        final var message = certificateTypeMessageService.get(certificate.getMetadata().getType(), patientId);
        if (message.isPresent()) {
            return getResourceLineWithMessage(message.get(), certificate.getMetadata().getType());
        }

        if (isCopyCertificateAvailable(certificate)) {
            return Optional.of(
                ResourceLinkDTO.create(
                    ResourceLinkTypeDTO.COPY_CERTIFICATE,
                    COPY_NAME,
                    COPY_DESCRIPTION,
                    COPY_DESCRIPTION_BODY,
                    true
                )
            );
        }

        if (isCopyCertificateContinueAvailable(certificate)) {
            return Optional.of(
                ResourceLinkDTO.create(
                    ResourceLinkTypeDTO.COPY_CERTIFICATE_CONTINUE,
                    COPY_NAME,
                    COPY_DESCRIPTION,
                    true
                )
            );
        }
        return Optional.empty();
    }

    private Optional<ResourceLinkDTO> getResourceLineWithMessage(CertificateMessage message, String certificateType) {
        if (isResourceLinkEnabled(message.getMessageType(), certificateType)) {
            return Optional.of(
                ResourceLinkDTO.create(
                    ResourceLinkTypeDTO.COPY_CERTIFICATE,
                    COPY_NAME,
                    COPY_DESCRIPTION,
                    String.format(COPY_DESCRIPTION_BODY_WITH_MESSAGE, message.getMessage()),
                    true
                )
            );
        }

        return Optional.of(
            ResourceLinkDTO.create(
                ResourceLinkTypeDTO.COPY_CERTIFICATE,
                COPY_NAME,
                message.getMessage(),
                false
            )
        );
    }

    private boolean isResourceLinkEnabled(CertificateMessageType messageType, String certificateType) {
        return (CERTIFICATE_ON_DIFFERENT_CARE_PROVIDER.equals(messageType) && DoiModuleEntryPoint.MODULE_ID.equals(certificateType))
            || DRAFT_ON_DIFFERENT_CARE_PROVIDER.equals(messageType);
    }

    private boolean isCopyCertificateAvailable(Certificate certificate) {
        return !includesChildRelation(certificate.getMetadata().getRelations());
    }

    private boolean isCopyCertificateContinueAvailable(Certificate certificate) {
        return includesChildRelation(certificate.getMetadata().getRelations());
    }

    private boolean includesChildRelation(CertificateRelations relations) {
        if (missingChildRelations(relations)) {
            return false;
        }

        return Arrays.stream(relations.getChildren()).anyMatch(
            relation -> relation.getType().equals(COPIED) && relation.getStatus().equals(UNSIGNED)
        );
    }

    private boolean missingChildRelations(CertificateRelations relations) {
        return relations == null || relations.getChildren() == null;
    }
}
