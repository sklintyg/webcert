package se.inera.webcert.converter;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import se.inera.webcert.persistence.intyg.model.Intyg;
import se.inera.webcert.persistence.intyg.model.IntygsStatus;
import se.inera.webcert.service.intyg.dto.IntygItem;
import se.inera.webcert.service.intyg.dto.IntygStatus;
import se.inera.webcert.web.controller.api.dto.IntygSource;
import se.inera.webcert.web.controller.api.dto.ListIntygEntry;

public final class IntygDraftsConverter {

    private static final Logger LOG = LoggerFactory.getLogger(IntygDraftsConverter.class);

    private static Comparator<ListIntygEntry> intygEntryDateComparator = new Comparator<ListIntygEntry>() {

        @Override
        public int compare(ListIntygEntry ie1, ListIntygEntry ie2) {
            return ie1.getLastUpdatedSigned().compareTo(ie2.getLastUpdatedSigned());
        }

    };

    private static Comparator<IntygStatus> intygStatusComparator = new Comparator<IntygStatus>() {

        @Override
        public int compare(IntygStatus c1, IntygStatus c2) {
            return c1.getTimestamp().compareTo(c2.getTimestamp());
        }

    };

    private IntygDraftsConverter() {

    }

    public static List<ListIntygEntry> merge(List<IntygItem> signedIntygList, List<Intyg> draftIntygList) {

        LOG.debug("Merging intyg, signed {}, drafts {}", signedIntygList.size(), draftIntygList.size());

        List<ListIntygEntry> allIntyg = new ArrayList<ListIntygEntry>();

        addSignedIntyg(allIntyg, signedIntygList);
        addDraftIntyg(allIntyg, draftIntygList);

        // sort according to signedUpdate date and then reverse so that last is on top.
        Collections.sort(allIntyg, intygEntryDateComparator);
        Collections.reverse(allIntyg);

        return allIntyg;
    }

    public static List<ListIntygEntry> convertIntygToListEntries(List<Intyg> draftIntygList) {

        List<ListIntygEntry> allIntyg = new ArrayList<ListIntygEntry>();

        ListIntygEntry intygEntry;

        for (Intyg cert : draftIntygList) {
            intygEntry = convertIntygToListIntygEntry(cert);
            allIntyg.add(intygEntry);
        }

        Collections.sort(allIntyg, intygEntryDateComparator);
        Collections.reverse(allIntyg);

        return allIntyg;
    }

    private static void addSignedIntyg(List<ListIntygEntry> allIntyg, List<IntygItem> signedIntygList) {

        ListIntygEntry intygEntry;

        for (IntygItem cert : signedIntygList) {
            intygEntry = convert(cert);
            allIntyg.add(intygEntry);
        }
    }

    private static void addDraftIntyg(List<ListIntygEntry> allIntyg, List<Intyg> draftIntygList) {

        ListIntygEntry intygEntry;

        for (Intyg intyg : draftIntygList) {
            intygEntry = convertIntygToListIntygEntry(intyg);
            allIntyg.add(intygEntry);
        }

    }

    public static ListIntygEntry convertIntygToListIntygEntry(Intyg intyg) {

        ListIntygEntry ie = new ListIntygEntry();

        ie.setIntygId(intyg.getIntygsId());
        ie.setIntygType(intyg.getIntygsTyp());
        ie.setSource(IntygSource.WC);
        ie.setUpdatedSignedBy(intyg.getSenastSparadAv().getNamn());
        ie.setLastUpdatedSigned(intyg.getSenastSparadDatum());
        ie.setPatientId(intyg.getPatientPersonnummer());
        ie.setForwarded(intyg.getVidarebefordrad());

        IntygsStatus intygsStatus = intyg.getStatus();
        ie.setStatus(intygsStatus.toString());

        return ie;
    }

    private static ListIntygEntry convert(IntygItem intygItem) {

        ListIntygEntry ie = new ListIntygEntry();

        ie.setIntygId(intygItem.getId());
        ie.setIntygType(intygItem.getType());
        ie.setStatus(findLastStatus(intygItem));
        ie.setSource(IntygSource.IT);
        ie.setLastUpdatedSigned(intygItem.getSignedDate());
        ie.setUpdatedSignedBy(intygItem.getSignedBy());

        return ie;
    }

    private static String findLastStatus(IntygItem intygItem) {

        List<IntygStatus> list = intygItem.getStatuses();

        if (list == null || list.isEmpty()) {
            LOG.debug("No statuses found in Intyg {}", intygItem.getId());
            return "UNKNOWN";
        }

        IntygStatus latestStatus = Collections.max(list, intygStatusComparator);

        return latestStatus.getType();
    }

}
