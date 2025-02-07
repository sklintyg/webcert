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
package se.inera.intyg.webcert.web.csintegration.integration;

import java.util.Collections;
import java.util.Comparator;
import java.util.stream.Stream;
import org.springframework.stereotype.Component;
import se.inera.intyg.common.support.common.enumerations.RelationKod;
import se.inera.intyg.common.support.facade.model.Certificate;
import se.inera.intyg.common.support.facade.model.CertificateStatus;
import se.inera.intyg.common.support.facade.model.link.ResourceLink;
import se.inera.intyg.common.support.facade.model.link.ResourceLinkTypeEnum;
import se.inera.intyg.common.support.facade.model.metadata.CertificateRelation;
import se.inera.intyg.common.support.facade.model.metadata.CertificateRelations;
import se.inera.intyg.common.support.model.UtkastStatus;
import se.inera.intyg.schemas.contract.Personnummer;
import se.inera.intyg.webcert.common.model.WebcertCertificateRelation;
import se.inera.intyg.webcert.web.service.facade.list.dto.CertificateListItemStatus;
import se.inera.intyg.webcert.web.web.controller.api.dto.ListIntygEntry;
import se.inera.intyg.webcert.web.web.controller.api.dto.Relations;
import se.inera.intyg.webcert.web.web.controller.api.dto.Relations.FrontendRelations;
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
        listIntygEntry.setStatus(convertStatus(metadata.getStatus(), metadata.isValidForSign(), metadata.isSent()));
        listIntygEntry.setStatusName(metadata.getStatus().name());

        listIntygEntry.setPatientId(createPatientId(metadata.getPatient().getPersonId().getId()));
        listIntygEntry.setAvliden(metadata.getPatient().isDeceased());
        listIntygEntry.setSekretessmarkering(metadata.getPatient().isProtectedPerson());

        listIntygEntry.setUpdatedSignedBy(metadata.getIssuedBy().getFullName());
        listIntygEntry.setUpdatedSignedById(metadata.getIssuedBy().getPersonId());
        listIntygEntry.setLastUpdated(metadata.getModified());
        listIntygEntry.setSigned(metadata.getSigned());
        listIntygEntry.setVardenhetId(metadata.getCareUnit().getUnitId());
        listIntygEntry.setVardgivarId(metadata.getCareProvider().getUnitId());
        listIntygEntry.setVidarebefordrad(metadata.isForwarded());

        listIntygEntry.setRelations(convertRelations(certificate.getMetadata().getRelations()));

        if (certificate.getLinks() != null) {
            certificate.getLinks().forEach(link -> listIntygEntry.getLinks().add(convertResourceLink(link)));
            listIntygEntry.getLinks().removeAll(Collections.singleton(null));
        }

        return listIntygEntry;
    }

    private Relations convertRelations(CertificateRelations relations) {
        if (relations == null || relations.getChildren() == null || relations.getChildren().length == 0) {
            return new Relations();
        }

        final var webcertCertificateRelation = Stream.of(relations.getChildren())
            .max(Comparator.comparing(CertificateRelation::getCreated))
            .map(childRelation ->
                new WebcertCertificateRelation(
                    childRelation.getCertificateId(),
                    convertTypeOfRelation(childRelation),
                    childRelation.getCreated(),
                    convertStatusOfRelation(childRelation),
                    convertRevokedStatus(childRelation)
                )
            )
            .orElseThrow(() -> new IllegalStateException("Could not find any child relations!"));

        final var convertedRelations = new Relations();
        convertedRelations.setLatestChildRelations(new FrontendRelations());
        if (webcertCertificateRelation.getRelationKod() == RelationKod.ERSATT) {
            if (webcertCertificateRelation.getStatus() == UtkastStatus.SIGNED) {
                convertedRelations.getLatestChildRelations().setReplacedByIntyg(webcertCertificateRelation);
            } else {
                convertedRelations.getLatestChildRelations().setReplacedByUtkast(webcertCertificateRelation);
            }
        } else if (webcertCertificateRelation.getRelationKod() == RelationKod.KOMPLT) {
            if (webcertCertificateRelation.getStatus() == UtkastStatus.SIGNED) {
                convertedRelations.getLatestChildRelations().setComplementedByIntyg(webcertCertificateRelation);
            } else {
                convertedRelations.getLatestChildRelations().setComplementedByUtkast(webcertCertificateRelation);
            }
        }
        return convertedRelations;
    }

    private static boolean convertRevokedStatus(CertificateRelation relations) {
        switch (relations.getStatus()) {
            case REVOKED:
            case LOCKED_REVOKED:
                return true;
            case LOCKED:
            case UNSIGNED:
            case SIGNED:
                return false;
            default:
                throw new IllegalStateException("Unexpected status: " + relations.getStatus());
        }
    }

    private static UtkastStatus convertStatusOfRelation(CertificateRelation relations) {
        switch (relations.getStatus()) {
            case SIGNED:
            case REVOKED:
                return UtkastStatus.SIGNED;
            case UNSIGNED:
                return UtkastStatus.DRAFT_COMPLETE;
            case LOCKED:
                return UtkastStatus.DRAFT_LOCKED;
            case LOCKED_REVOKED:
                return UtkastStatus.DRAFT_LOCKED;
            default:
                throw new IllegalStateException("Unexpected status: " + relations.getStatus());
        }
    }

    private static RelationKod convertTypeOfRelation(CertificateRelation relations) {
        switch (relations.getType()) {
            case REPLACED:
                return RelationKod.ERSATT;
            case COMPLEMENTED:
                return RelationKod.KOMPLT;
            case COPIED:
                return RelationKod.KOPIA;
            case EXTENDED:
                return RelationKod.FRLANG;
            default:
                throw new IllegalArgumentException("Unsupported relation type: " + relations.getType());
        }
    }

    private ActionLink convertResourceLink(ResourceLink resourceLink) {
        if (resourceLink.getType() == ResourceLinkTypeEnum.READ_CERTIFICATE) {
            return new ActionLink(ActionLinkType.LASA_INTYG);
        }

        if (resourceLink.getType() == ResourceLinkTypeEnum.FORWARD_CERTIFICATE
            || resourceLink.getType() == ResourceLinkTypeEnum.FORWARD_CERTIFICATE_FROM_LIST) {
            return new ActionLink(ActionLinkType.VIDAREBEFORDRA_UTKAST);
        }

        if (resourceLink.getType() == ResourceLinkTypeEnum.RENEW_CERTIFICATE) {
            return new ActionLink(ActionLinkType.FORNYA_INTYG_FRAN_CERTIFICATE_SERVICE);
        }

        return null;
    }

    public String convertStatus(CertificateStatus status, boolean validForSign, boolean isSent) {
        if (status == CertificateStatus.UNSIGNED) {
            if (validForSign) {
                return UtkastStatus.DRAFT_COMPLETE.toString();
            }
            return UtkastStatus.DRAFT_INCOMPLETE.toString();
        }

        if (status == CertificateStatus.REVOKED) {
            return "CANCELLED";
        }

        if (isSent) {
            return CertificateListItemStatus.SENT.toString();
        }

        if (status == CertificateStatus.SIGNED) {
            return UtkastStatus.SIGNED.toString();
        }

        if (status == CertificateStatus.LOCKED) {
            return UtkastStatus.DRAFT_LOCKED.toString();
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
