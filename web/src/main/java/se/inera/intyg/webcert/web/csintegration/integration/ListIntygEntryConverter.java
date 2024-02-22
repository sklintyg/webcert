/*
 * Copyright (C) 2024 Inera AB (http://www.inera.se)
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
package se.inera.intyg.webcert.web.csintegration.integration;

import java.util.Collections;
import org.springframework.stereotype.Component;
import se.inera.intyg.common.support.facade.model.Certificate;
import se.inera.intyg.common.support.facade.model.CertificateStatus;
import se.inera.intyg.common.support.facade.model.link.ResourceLink;
import se.inera.intyg.common.support.facade.model.link.ResourceLinkTypeEnum;
import se.inera.intyg.common.support.model.UtkastStatus;
import se.inera.intyg.schemas.contract.Personnummer;
import se.inera.intyg.webcert.web.web.controller.api.dto.ListIntygEntry;
import se.inera.intyg.webcert.web.web.util.resourcelinks.dto.ActionLink;
import se.inera.intyg.webcert.web.web.util.resourcelinks.dto.ActionLinkType;

@Component
public class ListIntygEntryConverter {

    public ListIntygEntry convert(Certificate certificate) {
        final var listIntygEntry = new ListIntygEntry();
        final var metadata = certificate.getMetadata();

        listIntygEntry.setIntygId(metadata.getId());
        listIntygEntry.setIntygType(metadata.getType());
        listIntygEntry.setIntygTypeName(metadata.getName());
        listIntygEntry.setIntygTypeVersion(metadata.getTypeVersion());
        listIntygEntry.setVersion(metadata.getVersion());

        listIntygEntry.setTestIntyg(metadata.isTestCertificate());
        listIntygEntry.setStatus(convertStatus(metadata.getStatus(), metadata.isValidForSign()));
        listIntygEntry.setStatusName(metadata.getStatus().name());

        listIntygEntry.setPatientId(createPatientId(metadata.getPatient().getPersonId().getId()));
        listIntygEntry.setAvliden(metadata.getPatient().isDeceased());
        listIntygEntry.setSekretessmarkering(metadata.getPatient().isProtectedPerson());

        listIntygEntry.setUpdatedSignedBy(metadata.getIssuedBy().getFullName());
        listIntygEntry.setUpdatedSignedById(metadata.getIssuedBy().getPersonId());
        listIntygEntry.setLastUpdatedSigned(metadata.getCreated());
        listIntygEntry.setVardenhetId(metadata.getCareUnit().getUnitId());
        listIntygEntry.setVardgivarId(metadata.getCareProvider().getUnitId());
        listIntygEntry.setVidarebefordrad(metadata.isForwarded());

        if (certificate.getLinks() != null) {
            certificate.getLinks().forEach(link -> listIntygEntry.getLinks().add(convertResourceLink(link)));
            listIntygEntry.getLinks().removeAll(Collections.singleton(null));
        }

        return listIntygEntry;
    }

    private ActionLink convertResourceLink(ResourceLink resourceLink) {
        if (resourceLink.getType() == ResourceLinkTypeEnum.READ_CERTIFICATE) {
            return new ActionLink(ActionLinkType.LASA_INTYG);
        }

        if (resourceLink.getType() == ResourceLinkTypeEnum.FORWARD_CERTIFICATE) {
            return new ActionLink(ActionLinkType.VIDAREBEFORDRA_UTKAST);
        }

        return null;
    }

    public String convertStatus(CertificateStatus status, boolean validForSign) {
        if (status == CertificateStatus.UNSIGNED) {
            if (validForSign) {
                return UtkastStatus.DRAFT_COMPLETE.toString();
            }
            return UtkastStatus.DRAFT_INCOMPLETE.toString();
        }

        return null;
    }

    private Personnummer createPatientId(String patientId) {
        return Personnummer.createPersonnummer(patientId)
            .orElseThrow(() ->
                new IllegalArgumentException(
                    String.format("PatientId has wrong format: '%s'", patientId)
                )
            );
    }
}
