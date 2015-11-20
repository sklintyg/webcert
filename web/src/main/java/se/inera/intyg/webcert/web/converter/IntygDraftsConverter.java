package se.inera.intyg.webcert.web.converter;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.Predicate;
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

    private static final Comparator<ListIntygEntry> INTYG_ENTRY_DATE_COMPARATOR = new Comparator<ListIntygEntry>() {

        @Override
        public int compare(ListIntygEntry ie1, ListIntygEntry ie2) {
            return ie1.getLastUpdatedSigned().compareTo(ie2.getLastUpdatedSigned());
        }

    };

    private static final Comparator<Status> INTYG_STATUS_COMPARATOR = new Comparator<Status>() {

        @Override
        public int compare(Status c1, Status c2) {
            return c1.getTimestamp().compareTo(c2.getTimestamp());
        }

    };

    private static final Predicate REMOVE_ARCHIVED_INTYG_STATUSES_PREDICATE = new Predicate() {

        private final List<CertificateState> archivedStatuses = Arrays.asList(CertificateState.DELETED, CertificateState.RESTORED);

        @Override
        public boolean evaluate(Object obj) {
            if (obj instanceof Status) {
                Status intygStatus = (Status) obj;
                return !archivedStatuses.contains(intygStatus.getType());
            }
            return false;
        }
    };

    private IntygDraftsConverter() {

    }

    public static List<ListIntygEntry> merge(List<IntygItem> intygList, List<Utkast> utkastList) {

        LOG.debug("Merging intyg, signed {}, drafts {}", intygList.size(), utkastList.size());

        List<ListIntygEntry> listIntygEntries = new ArrayList<>();

        ListIntygEntry intygEntry;

        // add all signed intyg
        for (IntygItem cert : intygList) {
            intygEntry = convertIntygItemToListIntygEntry(cert);
            listIntygEntries.add(intygEntry);
        }

        // add alldrafts
        for (Utkast intyg : utkastList) {
            intygEntry = convertUtkastToListIntygEntry(intyg);
            listIntygEntries.add(intygEntry);
        }

        // sort according to signedUpdate date and then reverse so that last is on top.
        Collections.sort(listIntygEntries, INTYG_ENTRY_DATE_COMPARATOR);
        Collections.reverse(listIntygEntries);

        return listIntygEntries;
    }

    public static List<ListIntygEntry> convertUtkastsToListIntygEntries(List<Utkast> utkastList) {

        List<ListIntygEntry> listIntygEntries = new ArrayList<>();

        ListIntygEntry intygEntry;

        for (Utkast cert : utkastList) {
            intygEntry = convertUtkastToListIntygEntry(cert);
            listIntygEntries.add(intygEntry);
        }

        Collections.sort(listIntygEntries, INTYG_ENTRY_DATE_COMPARATOR);
        Collections.reverse(listIntygEntries);

        return listIntygEntries;
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

        CollectionUtils.filter(intygStatuses, REMOVE_ARCHIVED_INTYG_STATUSES_PREDICATE);

        if (intygStatuses.isEmpty()) {
            return CertificateState.UNHANDLED;
        }

        Status latestStatus = Collections.max(intygStatuses, INTYG_STATUS_COMPARATOR);
        return latestStatus.getType();
    }
}
