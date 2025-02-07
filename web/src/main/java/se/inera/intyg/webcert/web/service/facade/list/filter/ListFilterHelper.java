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
package se.inera.intyg.webcert.web.service.facade.list.filter;

import org.springframework.stereotype.Service;
import se.inera.intyg.common.support.model.UtkastStatus;
import se.inera.intyg.webcert.web.service.facade.list.config.dto.*;
import se.inera.intyg.webcert.web.service.facade.list.dto.*;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

@Service
public class ListFilterHelper {

    public static LocalDateTime getSavedFrom(ListFilter filter) {
        ListFilterDateRangeValue saved = (ListFilterDateRangeValue) filter.getValue("SAVED");
        return saved != null && saved.getFrom() != null ? saved.getFrom() : null;
    }

    public static LocalDateTime getSavedTo(ListFilter filter) {
        ListFilterDateRangeValue saved = (ListFilterDateRangeValue) filter.getValue("SAVED");
        return saved != null && saved.getTo() != null ? saved.getTo().plusDays(1) : null;
    }

    public static LocalDateTime getSentFrom(ListFilter filter) {
        ListFilterDateRangeValue saved = (ListFilterDateRangeValue) filter.getValue("SENT");
        return saved != null && saved.getFrom() != null ? saved.getFrom() : null;
    }

    public static LocalDateTime getSentTo(ListFilter filter) {
        ListFilterDateRangeValue sent = (ListFilterDateRangeValue) filter.getValue("SENT");
        return sent != null && sent.getTo() != null ? sent.getTo() : null;
    }

    public static String getSavedBy(ListFilter filter) {
        ListFilterSelectValue savedBy = (ListFilterSelectValue) filter.getValue("SAVED_BY");
        return savedBy != null && !savedBy.getValue().equals("SHOW_ALL") ? savedBy.getValue() : "";
    }

    public static String getSignedBy(ListFilter filter) {
        ListFilterSelectValue signedBy = (ListFilterSelectValue) filter.getValue("SIGNED_BY");
        return signedBy != null && !signedBy.getValue().equals("SHOW_ALL") ? signedBy.getValue() : "";
    }

    public static String getPatientId(ListFilter filter) {
        ListFilterPersonIdValue patientId = (ListFilterPersonIdValue) filter.getValue("PATIENT_ID");
        return patientId != null ? patientId.getValue() : "";
    }

    public static String getPatientIdWithoutDash(ListFilter filter) {
        ListFilterPersonIdValue patientId = (ListFilterPersonIdValue) filter.getValue("PATIENT_ID");
        return patientId != null ? patientId.getValue().replace("-", "") : "";
    }

    public static Boolean getForwarded(ListFilter filter) {
        ListFilterSelectValue forwarded = (ListFilterSelectValue) filter.getValue("FORWARDED");
        return forwarded != null ? getForwardedValue(forwarded.getValue()) : null;
    }

    public static String getOrderBy(ListFilter filter) {
        ListFilterTextValue orderBy = (ListFilterTextValue) filter.getValue("ORDER_BY");
        return orderBy == null ? "" : orderBy.getValue();
    }

    public static boolean getAscending(ListFilter filter) {
        ListFilterBooleanValue ascending = (ListFilterBooleanValue) filter.getValue("ASCENDING");
        return ascending != null && ascending.getValue();
    }

    public static List<UtkastStatus> getDraftStatus(ListFilter filter) {
        ListFilterSelectValue status = (ListFilterSelectValue) filter.getValue("STATUS");
        return getDraftStatusListFromFilter(status != null ? status.getValue() : "");
    }

    private static List<UtkastStatus> getDraftStatusListFromFilter(String status) {
        final var showAll = Arrays.asList(UtkastStatus.DRAFT_COMPLETE, UtkastStatus.DRAFT_LOCKED, UtkastStatus.DRAFT_INCOMPLETE);

        if (status.isBlank()) {
            return showAll;
        }

        final var convertedStatus = CertificateListItemStatus.valueOf(status);

        if (convertedStatus == CertificateListItemStatus.INCOMPLETE) {
            return List.of(UtkastStatus.DRAFT_INCOMPLETE);
        } else if (convertedStatus == CertificateListItemStatus.COMPLETE) {
            return List.of(UtkastStatus.DRAFT_COMPLETE);
        } else if (convertedStatus == CertificateListItemStatus.LOCKED) {
            return List.of(UtkastStatus.DRAFT_LOCKED);
        }
        return showAll;
    }

    public static String getQuestionStatus(ListFilter filter) {
        ListFilterSelectValue status = (ListFilterSelectValue) filter.getValue("STATUS");
        if (status == null) {
            return "";
        }

        final var questionType = QuestionStatusType.valueOf(status.getValue());
        switch (questionType) {
            case HANDLED:
                return "HANTERAD";
            case NOT_HANDLED:
                return "ALLA_OHANTERADE";
            case COMPLEMENT:
                return "KOMPLETTERING_FRAN_VARDEN";
            case ANSWER:
                return "SVAR_FRAN_VARDEN";
            case READ_ANSWER:
                return "MARKERA_SOM_HANTERAD";
            case WAIT:
                return "SVAR_FRAN_FK";
            case SHOW_ALL:
            default:
                return "ALLA";
        }
    }

    private static Boolean getForwardedValue(String value) {
        if (value.equals(ForwardedType.FORWARDED.toString())) {
            return true;
        } else if (value.equals(ForwardedType.NOT_FORWARDED.toString())) {
            return false;
        }
        return null;
    }

    public static LocalDateTime getSignedFrom(ListFilter filter) {
        ListFilterDateRangeValue signed = (ListFilterDateRangeValue) filter.getValue("SIGNED");
        return signed != null ? signed.getFrom() : LocalDateTime.now().minusMonths(3);
    }

    public static LocalDateTime getSignedTo(ListFilter filter) {
        ListFilterDateRangeValue signed = (ListFilterDateRangeValue) filter.getValue("SIGNED");
        return signed != null ? signed.getTo() : null;
    }

    public static int getStartFrom(ListFilter filter) {
        ListFilterNumberValue startFrom = (ListFilterNumberValue) filter.getValue("START_FROM");
        return startFrom == null ? 0 : startFrom.getValue();
    }

    public static int getPageSize(ListFilter filter) {
        ListFilterNumberValue pageSize = (ListFilterNumberValue) filter.getValue("PAGESIZE");
        return pageSize == null ? 10 : pageSize.getValue();
    }

    public static String convertOrderBy(ListFilter filter, ListType listType) {
        ListFilterTextValue orderBy = (ListFilterTextValue) filter.getValue("ORDER_BY");
        if (orderBy == null) {
            return "";
        }
        return listType == ListType.CERTIFICATES ? convertOrderBy(orderBy.getValue()) : convertOrderByForQuestions(orderBy.getValue());
    }

    private static String convertOrderBy(String orderBy) {
        final var type = ListColumnType.valueOf(orderBy);
        if (type == ListColumnType.SIGNED) {
            return "signedDate";
        } else if (type == ListColumnType.PATIENT_ID) {
            return "civicRegistrationNumber";
        } else if (type == ListColumnType.STATUS) {
            return "status";
        } else if (type == ListColumnType.CERTIFICATE_TYPE_NAME) {
            return "type";
        } else {
            return "signedDate";
        }
    }

    private static String convertOrderByForQuestions(String orderBy) {
        final var type = ListColumnType.valueOf(orderBy);
        if (type == ListColumnType.SENT_RECEIVED) {
            return "receivedDate";
        } else if (type == ListColumnType.FORWARDED) {
            return "vidarebefordrad";
        } else if (type == ListColumnType.SIGNED_BY) {
            return "signeratAvNamn";
        } else if (type == ListColumnType.PATIENT_ID) {
            return "patientId";
        } else if (type == ListColumnType.QUESTION_ACTION) {
            return "amne";
        } else if (type == ListColumnType.SENDER) {
            return "fragestallare";
        } else {
            return "receivedDate";
        }
    }

    public static String getUnitId(ListFilter filter) {
        ListFilterSelectValue unit = (ListFilterSelectValue) filter.getValue("UNIT");
        return unit != null ? unit.getValue() : "";
    }

    public static boolean includeQuestionFromFK(ListFilter filter) {
        return includeQuestionFromSender(filter, QuestionSenderType.FK.toString());
    }

    public static boolean includeQuestionFromUnit(ListFilter filter) {
        return includeQuestionFromSender(filter, QuestionSenderType.WC.toString());
    }

    private static boolean includeQuestionFromSender(ListFilter filter, String recipientName) {
        ListFilterSelectValue sender = (ListFilterSelectValue) filter.getValue("SENDER");
        return sender != null && sender.getValue().equals(recipientName);
    }
}
