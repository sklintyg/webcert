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
package se.inera.intyg.webcert.web.service.facade.list;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import se.inera.intyg.common.support.model.UtkastStatus;
import se.inera.intyg.infra.certificate.dto.CertificateListEntry;
import se.inera.intyg.infra.integration.hsatk.services.legacy.HsaOrganizationsService;
import se.inera.intyg.webcert.common.model.WebcertCertificateRelation;
import se.inera.intyg.webcert.web.service.arende.ArendeServiceImpl;
import se.inera.intyg.webcert.web.service.facade.list.config.dto.ListColumnType;
import se.inera.intyg.webcert.web.service.facade.list.dto.CertificateListItem;
import se.inera.intyg.webcert.web.service.facade.list.dto.CertificateListItemStatus;
import se.inera.intyg.webcert.web.service.facade.list.dto.ForwardedListInfo;
import se.inera.intyg.webcert.web.service.facade.list.dto.ListType;
import se.inera.intyg.webcert.web.service.facade.list.dto.PatientListInfo;
import se.inera.intyg.webcert.web.service.facade.list.dto.QuestionSenderType;
import se.inera.intyg.webcert.web.web.controller.api.dto.ArendeListItem;
import se.inera.intyg.webcert.web.web.controller.api.dto.ListIntygEntry;
import se.inera.intyg.webcert.web.web.controller.api.dto.Relations;
import se.inera.intyg.webcert.web.web.controller.facade.dto.ResourceLinkDTO;
import se.inera.intyg.webcert.web.web.controller.facade.dto.ResourceLinkTypeDTO;

@Service
public class CertificateListItemConverterImpl implements CertificateListItemConverter {

    public static final String DRAFT_LOCKED_CANCELLED = "DRAFT_LOCKED_CANCELLED";
    public static final String CANCELLED = "CANCELLED";
    public static final String COMPLEMENTED = "COMPLEMENTED";
    public static final String SENT = "SENT";
    public static final String SIGNED = "SIGNED";
    public static final String RECEIVED = "RECEIVED";
    public static final String UNKOWN_STATUS = "Okänd status";

    private final HsaOrganizationsService hsaOrganizationsService;
    private final ResourceLinkListHelper resourceLinkListHelper;

    @Autowired
    public CertificateListItemConverterImpl(HsaOrganizationsService hsaOrganizationsService,
        ResourceLinkListHelper resourceLinkListHelper) {
        this.hsaOrganizationsService = hsaOrganizationsService;
        this.resourceLinkListHelper = resourceLinkListHelper;
    }

    @Override
    public CertificateListItem convert(ListIntygEntry listIntygEntry, ListType listType) {
        return convertListItem(listIntygEntry, listType);
    }

    @Override
    public CertificateListItem convert(CertificateListEntry entry) {
        return convertListItem(entry);
    }

    @Override
    public CertificateListItem convert(ArendeListItem entry) {
        return convertListItem(entry);
    }

    private CertificateListItem convertListItem(ListIntygEntry listIntygEntry, ListType listType) {
        final var listItem = new CertificateListItem();
        final var certificateListItemStatus = listType == ListType.CERTIFICATES
            ? getCertificateListItemStatus(listIntygEntry.getStatus().equals(SENT))
            : getCertificateListItemStatus(listIntygEntry.getStatus(), listIntygEntry.getRelations());
        final var patientListInfo = getPatientListInfo(listIntygEntry);
        final var convertedLinks = resourceLinkListHelper.get(listIntygEntry, certificateListItemStatus);

        listItem.addValue(ListColumnType.CERTIFICATE_TYPE_NAME, listIntygEntry.getIntygTypeName());
        listItem.addValue(ListColumnType.STATUS, convertStatus(certificateListItemStatus));
        listItem.addValue(
            listType == ListType.CERTIFICATES ? ListColumnType.SIGNED : ListColumnType.SAVED,
            listType == ListType.CERTIFICATES ? listIntygEntry.getSigned() : listIntygEntry.getLastUpdated()
        );
        listItem.addValue(ListColumnType.PATIENT_ID, patientListInfo);
        listItem.addValue(listType == ListType.DRAFTS ? ListColumnType.SAVED_BY : ListColumnType.SAVED_SIGNED_BY,
            listIntygEntry.getUpdatedSignedBy()
        );
        listItem.addValue(ListColumnType.CERTIFICATE_ID, listIntygEntry.getIntygId());
        listItem.addValue(ListColumnType.LINKS, convertedLinks);
        if (isAllowedToForward(convertedLinks)) {
            listItem.addValue(ListColumnType.FORWARD_CERTIFICATE, getForwardedListInfo(listIntygEntry));
        }
        listItem.addValue(ListColumnType.FORWARDED, listIntygEntry.isVidarebefordrad());
        return listItem;
    }

    private CertificateListItem convertListItem(ArendeListItem entry) {
        final var listItem = new CertificateListItem();
        final var patientListInfo = getPatientListInfo(entry);
        final var convertedLinks = resourceLinkListHelper.get(entry, CertificateListItemStatus.SIGNED);

        listItem.addValue(ListColumnType.QUESTION_ACTION, getQuestionAction(entry));
        listItem.addValue(ListColumnType.SENT_RECEIVED, entry.getReceivedDate());
        listItem.addValue(ListColumnType.PATIENT_ID, patientListInfo);
        listItem.addValue(ListColumnType.SIGNED_BY, entry.getSigneratAvNamn());
        listItem.addValue(ListColumnType.CERTIFICATE_ID, entry.getIntygId());
        listItem.addValue(ListColumnType.FORWARDED, entry.isVidarebefordrad());
        listItem.addValue(ListColumnType.SENDER, convertSender(entry.getFragestallare()));
        listItem.addValue(ListColumnType.LINKS, convertedLinks);

        if (isAllowedToForward(convertedLinks)) {
            listItem.addValue(
                ListColumnType.FORWARD_CERTIFICATE,
                getForwardedListInfo(entry.getEnhetsnamn(), entry.getVardgivarnamn(), entry.isVidarebefordrad(), entry.getIntygTyp())
            );
        }

        return listItem;
    }

    private String getQuestionAction(ArendeListItem entry) {
        return ArendeServiceImpl.getAmneString(entry.getAmne(), entry.getStatus(), entry.isPaminnelse(), entry.getFragestallare());
    }

    private String convertSender(String sender) {
        return QuestionSenderType.valueOf(sender).getName();
    }

    private CertificateListItem convertListItem(CertificateListEntry entry) {
        final var listItem = new CertificateListItem();
        final var certificateListItemStatus = getCertificateListItemStatus(entry.isSent());
        final var patientListInfo = getPatientListInfo(entry);
        listItem.addValue(ListColumnType.CERTIFICATE_TYPE_NAME, entry.getCertificateTypeName());
        listItem.addValue(ListColumnType.SIGNED, entry.getSignedDate());
        listItem.addValue(ListColumnType.PATIENT_ID, patientListInfo);
        listItem.addValue(ListColumnType.CERTIFICATE_ID, entry.getCertificateId());
        listItem.addValue(ListColumnType.STATUS, certificateListItemStatus.getName());
        listItem.addValue(ListColumnType.LINKS, resourceLinkListHelper.get(entry, certificateListItemStatus));
        return listItem;
    }

    private String convertStatus(CertificateListItemStatus status) {
        return status != null ? status.getName() : UNKOWN_STATUS;
    }

    private CertificateListItemStatus getCertificateListItemStatus(String status, Relations relations) {
        if (status.equals(UtkastStatus.DRAFT_COMPLETE.toString())) {
            return CertificateListItemStatus.COMPLETE;
        }

        if (status.equals(UtkastStatus.DRAFT_INCOMPLETE.toString())) {
            return CertificateListItemStatus.INCOMPLETE;
        }

        if (status.equals(UtkastStatus.DRAFT_LOCKED.toString())) {
            return CertificateListItemStatus.LOCKED;
        }

        if (isReplaced(relations)) {
            return CertificateListItemStatus.REPLACED;
        }

        if (status.equals(COMPLEMENTED) || isComplementedBySignedCertificate(relations)) {
            return CertificateListItemStatus.COMPLEMENTED;
        } else if (isComplemented(relations)) {
            return CertificateListItemStatus.SENT;
        }

        if (status.equals(CANCELLED) || status.equals(DRAFT_LOCKED_CANCELLED)) {
            return CertificateListItemStatus.REVOKED;
        }

        if (status.equals(SENT)) {
            return CertificateListItemStatus.SENT;
        }

        if (status.equals(SIGNED) || status.equals(RECEIVED)) {
            return CertificateListItemStatus.SIGNED;
        }
        return null;
    }

    private boolean checkActiveRelation(WebcertCertificateRelation relationFromCertificate, WebcertCertificateRelation relationFromDraft) {
        return (relationFromCertificate != null && !relationFromCertificate.isMakulerat()) || relationFromDraft != null;
    }

    private boolean isReplaced(Relations relations) {
        if (relations == null) {
            return false;
        }

        final var replacedByCertificate = relations.getLatestChildRelations().getReplacedByIntyg();
        return checkActiveRelation(replacedByCertificate, null);
    }

    private boolean isComplemented(Relations relations) {
        if (relations == null) {
            return false;
        }

        final var complementedByCertificate = relations.getLatestChildRelations().getComplementedByIntyg();
        final var complementedByDraft = relations.getLatestChildRelations().getComplementedByUtkast();
        return checkActiveRelation(complementedByCertificate, complementedByDraft);
    }

    private boolean isComplementedBySignedCertificate(Relations relations) {
        if (relations == null) {
            return false;
        }

        final var complementedByCertificate = relations.getLatestChildRelations().getComplementedByIntyg();
        return checkActiveRelation(complementedByCertificate, null);
    }

    private CertificateListItemStatus getCertificateListItemStatus(boolean isSent) {
        return isSent ? CertificateListItemStatus.SENT : CertificateListItemStatus.NOT_SENT;
    }

    private PatientListInfo getPatientListInfo(ListIntygEntry listIntygEntry) {
        return new PatientListInfo(
            listIntygEntry.getPatientId().getPersonnummerWithDash(),
            listIntygEntry.isSekretessmarkering(),
            listIntygEntry.isAvliden(),
            listIntygEntry.isTestIntyg()
        );
    }

    private PatientListInfo getPatientListInfo(ArendeListItem listIntygEntry) {
        return new PatientListInfo(
            listIntygEntry.getPatientId(),
            listIntygEntry.isSekretessmarkering(),
            listIntygEntry.isAvliden(),
            listIntygEntry.isTestIntyg()
        );
    }

    private PatientListInfo getPatientListInfo(CertificateListEntry certificateListEntry) {
        return new PatientListInfo(
            certificateListEntry.getCivicRegistrationNumber(),
            certificateListEntry.isProtectedIdentity(),
            certificateListEntry.isDeceased(),
            certificateListEntry.isTestIndicator()
        );
    }

    private boolean isAllowedToForward(List<ResourceLinkDTO> links) {
        return links.stream().anyMatch(link ->
            link.getType() == ResourceLinkTypeDTO.FORWARD_CERTIFICATE || link.getType() == ResourceLinkTypeDTO.FORWARD_QUESTION
        );
    }

    private ForwardedListInfo getForwardedListInfo(ListIntygEntry entry) {
        final var unit = hsaOrganizationsService.getVardenhet(entry.getVardenhetId());
        final var careGiver = hsaOrganizationsService.getVardgivareInfo(entry.getVardgivarId());
        return getForwardedListInfo(unit.getNamn(), careGiver.getNamn(), entry.isVidarebefordrad(), entry.getIntygType());
    }

    private ForwardedListInfo getForwardedListInfo(String unitName, String careGiverName, boolean isForwarded, String certificateType) {
        return new ForwardedListInfo(
            isForwarded,
            unitName,
            careGiverName,
            certificateType);
    }
}
