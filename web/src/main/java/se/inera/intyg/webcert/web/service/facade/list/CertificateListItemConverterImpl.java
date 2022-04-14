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

import org.springframework.stereotype.Service;
import se.inera.intyg.common.support.model.UtkastStatus;
import se.inera.intyg.webcert.web.service.facade.list.config.dto.ListColumnType;
import se.inera.intyg.webcert.web.service.facade.list.dto.CertificateListItem;
import se.inera.intyg.webcert.web.service.facade.list.dto.DraftStatus;
import se.inera.intyg.webcert.web.service.facade.list.dto.ListType;
import se.inera.intyg.webcert.web.service.facade.list.dto.PatientListInfo;
import se.inera.intyg.webcert.web.web.controller.api.dto.ListIntygEntry;

@Service
public class CertificateListItemConverterImpl implements CertificateListItemConverter {

    @Override
    public CertificateListItem convert(ListIntygEntry listIntygEntry, ListType listType) {
        return convertListItem(listIntygEntry, listType);
    }

    private CertificateListItem convertListItem(ListIntygEntry listIntygEntry, ListType listType) {
        final var listItem = new CertificateListItem();
        final var convertedStatus = convertStatus(listIntygEntry.getStatus(), listType);
        final var patientListInfo = getPatientListInfo(listIntygEntry);

        listItem.addValue(ListColumnType.CERTIFICATE_TYPE_NAME, listIntygEntry.getIntygTypeName());
        listItem.addValue(ListColumnType.STATUS, convertedStatus);
        listItem.addValue(ListColumnType.SAVED, listIntygEntry.getLastUpdatedSigned());
        listItem.addValue(ListColumnType.PATIENT_ID, patientListInfo);
        listItem.addValue(ListColumnType.SAVED_BY, listIntygEntry.getUpdatedSignedBy());
        listItem.addValue(ListColumnType.FORWARDED, listIntygEntry.isVidarebefordrad());
        listItem.addValue(ListColumnType.CERTIFICATE_ID, listIntygEntry.getIntygId());
        return listItem;
    }

    private String convertStatus(String status, ListType listType) {
        if (listType == ListType.DRAFTS) {
            return convertDraftStatus(UtkastStatus.valueOf(status)).getName();
        }
        return "";
    }

    private DraftStatus convertDraftStatus(UtkastStatus status) {
        if (status == UtkastStatus.DRAFT_COMPLETE) {
            return DraftStatus.COMPLETE;
        } else if (status == UtkastStatus.DRAFT_INCOMPLETE) {
            return DraftStatus.INCOMPLETE;
        }
        return DraftStatus.LOCKED;
    }

    private PatientListInfo getPatientListInfo(ListIntygEntry listIntygEntry) {
        return new PatientListInfo(
                listIntygEntry.getPatientId().getPersonnummerWithDash(),
                listIntygEntry.isSekretessmarkering(),
                listIntygEntry.isAvliden(),
                listIntygEntry.isTestIntyg()
        );
    }
}
