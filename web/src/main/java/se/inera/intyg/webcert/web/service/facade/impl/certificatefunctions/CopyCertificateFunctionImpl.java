/*
 * Copyright (C) 2023 Inera AB (http://www.inera.se)
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

import java.util.Arrays;
import java.util.Optional;
import org.springframework.stereotype.Component;
import se.inera.intyg.common.support.facade.model.Certificate;
import se.inera.intyg.common.support.facade.model.metadata.CertificateRelations;
import se.inera.intyg.schemas.contract.Personnummer;
import se.inera.intyg.webcert.web.service.facade.CertificateTypeMessageService;
import se.inera.intyg.webcert.web.web.controller.facade.dto.ResourceLinkDTO;
import se.inera.intyg.webcert.web.web.controller.facade.dto.ResourceLinkTypeDTO;

@Component
public class CopyCertificateFunctionImpl implements CopyCertificateFunction {

    private static final String COPY_NAME = "Kopiera";
    private static final String COPY_DESCRIPTION = "Skapar en redigerbar kopia av utkastet på den enheten du är inloggad på.";

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
            return Optional.of(
                ResourceLinkDTO.create(
                    ResourceLinkTypeDTO.COPY_CERTIFICATE,
                    COPY_NAME,
                    message.get(),
                    false
                )
            );
        }

        if (isCopyCertificateAvailable(certificate)) {
            return Optional.of(
                ResourceLinkDTO.create(
                    ResourceLinkTypeDTO.COPY_CERTIFICATE,
                    COPY_NAME,
                    COPY_DESCRIPTION,
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
