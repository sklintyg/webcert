package se.inera.intyg.webcert.web.converter;

import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import se.inera.certificate.model.CertificateState;
import se.inera.certificate.model.Status;
import se.inera.webcert.persistence.utkast.model.Utkast;
import se.inera.intyg.webcert.web.service.intyg.dto.IntygItem;
import se.inera.intyg.webcert.web.web.controller.api.dto.IntygSource;
import se.inera.intyg.webcert.web.web.controller.api.dto.ListIntygEntry;

public final class IntygDraftsConverter {

    private static final Logger LOG = LoggerFactory.getLogger(IntygDraftsConverter.class);

    private static final Comparator<ListIntygEntry> INTYG_ENTRY_DATE_COMPARATOR_DESC =
            (ie1, ie2) -> ie2.getLastUpdatedSigned().compareTo(ie1.getLastUpdatedSigned());

    private static final Comparator<Status> INTYG_STATUS_COMPARATOR = (c1, c2) -> c1.getTimestamp().compareTo(c2.getTimestamp());

    private static final List<CertificateState> ARCHIVED_STATUSES = Arrays.asList(CertificateState.DELETED, CertificateState.RESTORED);

    private IntygDraftsConverter() {

    }

    public static List<ListIntygEntry> merge(List<IntygItem> intygList, List<Utkast> utkastList) {

        LOG.debug("Merging intyg, signed {}, drafts {}", intygList.size(), utkastList.size());

        return Stream.concat(
                intygList.stream()
                        .map(IntygDraftsConverter::convertIntygItemToListIntygEntry),
                utkastList.stream()
                        .map(IntygDraftsConverter::convertUtkastToListIntygEntry))
                .sorted(INTYG_ENTRY_DATE_COMPARATOR_DESC)
                .collect(Collectors.toList());
    }
    // CHECKSTYLE:ON
    public static List<ListIntygEntry> convertUtkastsToListIntygEntries(List<Utkast> utkastList) {

        return utkastList.stream()
                .map(IntygDraftsConverter::convertUtkastToListIntygEntry)
                .sorted(INTYG_ENTRY_DATE_COMPARATOR_DESC)
                .collect(Collectors.toList());
    }

    public static ListIntygEntry convertUtkastToListIntygEntry(Utkast utkast) {

        ListIntygEntry entry = new ListIntygEntry();

        entry.setIntygId(utkast.getIntygsId());
        entry.setIntygType(utkast.getIntygsTyp());
        entry.setSource(IntygSource.WC);
        entry.setUpdatedSignedBy(utkast.getSenastSparadAv().getNamn());
        entry.setLastUpdatedSigned(utkast.getSenastSparadDatum());
        entry.setPatientId(utkast.getPatientPersonnummer());
        entry.setVidarebefordrad(utkast.getVidarebefordrad());
        entry.setStatus(utkast.getStatus().toString());
        entry.setVersion(utkast.getVersion());

        return entry;
    }

    public static ListIntygEntry convertIntygItemToListIntygEntry(IntygItem intygItem) {

        ListIntygEntry entry = new ListIntygEntry();

        entry.setIntygId(intygItem.getId());
        entry.setIntygType(intygItem.getType());
        entry.setStatus(findLatestStatus(intygItem.getStatuses()).toString());
        entry.setSource(IntygSource.IT);
        entry.setLastUpdatedSigned(intygItem.getSignedDate());
        entry.setUpdatedSignedBy(intygItem.getSignedBy());

        return entry;
    }

    public static CertificateState findLatestStatus(List<Status> intygStatuses) {

        if ((intygStatuses == null) || intygStatuses.isEmpty()) {
            return CertificateState.UNHANDLED;
        }

        Optional<Status> status = intygStatuses.stream()
                .filter(s -> !ARCHIVED_STATUSES.contains(s.getType()))
                .max(INTYG_STATUS_COMPARATOR);

        if (status.isPresent()) {
            return status.get().getType();
        } else {
            return CertificateState.UNHANDLED;
        }
    }
}
