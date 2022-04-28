/*
 * Copyright (C) 2022 Inera AB (http://www.inera.se)
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

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import se.inera.intyg.common.support.model.UtkastStatus;

import se.inera.intyg.infra.certificate.dto.CertificateListEntry;
import se.inera.intyg.infra.integration.hsatk.services.legacy.HsaOrganizationsService;

import se.inera.intyg.webcert.web.service.facade.list.config.dto.ListColumnType;
import se.inera.intyg.webcert.web.service.facade.list.dto.*;
import se.inera.intyg.webcert.web.web.controller.api.dto.ListIntygEntry;

@Service
public class CertificateListItemConverterImpl implements CertificateListItemConverter {

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

    private CertificateListItem convertListItem(ListIntygEntry listIntygEntry, ListType listType) {
        final var listItem = new CertificateListItem();
        final var convertedStatus = convertStatus(listIntygEntry.getStatus());
        final var patientListInfo = getPatientListInfo(listIntygEntry);

        listItem.addValue(ListColumnType.CERTIFICATE_TYPE_NAME, listIntygEntry.getIntygTypeName());
        listItem.addValue(ListColumnType.STATUS, convertedStatus);
        listItem.addValue(ListColumnType.SAVED, listIntygEntry.getLastUpdatedSigned());
        listItem.addValue(ListColumnType.PATIENT_ID, patientListInfo);
        listItem.addValue(listType == ListType.DRAFTS ? ListColumnType.SAVED_BY : ListColumnType.SAVED_SIGNED_BY,
                listIntygEntry.getUpdatedSignedBy()
        );
        listItem.addValue(ListColumnType.FORWARDED, getForwardedListInfo(listIntygEntry));
        listItem.addValue(ListColumnType.CERTIFICATE_ID, listIntygEntry.getIntygId());
        listItem.addValue(ListColumnType.LINKS, resourceLinkListHelper.get(listIntygEntry, convertedStatus)
        );
        return listItem;
    }

    private CertificateListItem convertListItem(CertificateListEntry entry) {
        final var listItem = new CertificateListItem();
        final var convertedStatus = entry.isSent()
                ? CertificateStatus.SENT.getName() : CertificateStatus.NOT_SENT.getName();
        final var patientListInfo = getPatientListInfo(entry);
        listItem.addValue(ListColumnType.CERTIFICATE_TYPE_NAME, entry.getCertificateTypeName());
        listItem.addValue(ListColumnType.SIGNED, entry.getSignedDate());
        listItem.addValue(ListColumnType.PATIENT_ID, patientListInfo);
        listItem.addValue(ListColumnType.CERTIFICATE_ID, entry.getCertificateId());
        listItem.addValue(ListColumnType.STATUS, convertedStatus);
        listItem.addValue(ListColumnType.LINKS, resourceLinkListHelper.get(entry, convertedStatus));
        return listItem;
    }

    private String convertStatus(String status) {
        final var convertedStatus = getCertificateStatus(status);
        return convertedStatus != null ? convertedStatus.getName() : "Okänd";
    }

    private CertificateStatus getCertificateStatus(String status) {
        if (status.equals(UtkastStatus.DRAFT_COMPLETE.toString())) {
            return CertificateStatus.COMPLETE;
        } else if (status.equals(UtkastStatus.DRAFT_INCOMPLETE.toString())) {
            return CertificateStatus.INCOMPLETE;
        } else if (status.equals(UtkastStatus.DRAFT_LOCKED.toString())) {
            return CertificateStatus.LOCKED;
        } else if (status.equals("RENEWED")) {
            return CertificateStatus.RENEWED;
        } else if (status.equals("COMPLEMENTED")) {
            return CertificateStatus.COMPLEMENTED;
        } else if (status.equals("CANCELLED")) {
            return CertificateStatus.REVOKED;
        } else if (status.equals("SENT")  || status.equals("RECEIVED")) {
            return CertificateStatus.SENT;
        } else if (status.equals("SIGNED")) {
            return CertificateStatus.SIGNED;
        }
        return null;
    }

    private PatientListInfo getPatientListInfo(ListIntygEntry listIntygEntry) {
        return new PatientListInfo(
                listIntygEntry.getPatientId().getPersonnummerWithDash(),
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

    private ForwardedListInfo getForwardedListInfo(ListIntygEntry entry) {
        final var unit = hsaOrganizationsService.getVardenhet(entry.getVardenhetId());
        final var careGiver = hsaOrganizationsService.getVardgivareInfo(entry.getVardgivarId());
        return new ForwardedListInfo(
                entry.isVidarebefordrad(),
                unit.getNamn(),
                careGiver.getNamn());
    }
}
