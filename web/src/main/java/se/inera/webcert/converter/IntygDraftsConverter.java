package se.inera.webcert.converter;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.Predicate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import se.inera.webcert.persistence.intyg.model.Intyg;
import se.inera.webcert.persistence.intyg.model.IntygsStatus;
import se.inera.webcert.service.intyg.dto.IntygItem;
import se.inera.webcert.service.intyg.dto.IntygStatus;
import se.inera.webcert.service.intyg.dto.StatusType;
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
    
    private static Predicate removeArchivedIntygStatusesPredicate = new Predicate() {
        
        private final List<StatusType> archivedStatuses = Arrays.asList(StatusType.DELETED, StatusType.RESTORED);
        
        @Override
        public boolean evaluate(Object obj) {
            if (obj instanceof IntygStatus) {
                IntygStatus intygStatus = (IntygStatus) obj;
                return !archivedStatuses.contains(intygStatus.getType());
            }
            return false;
        }
    };

    private IntygDraftsConverter() {

    }

    public static List<ListIntygEntry> merge(List<IntygItem> signedIntygList, List<Intyg> draftIntygList) {

        LOG.debug("Merging intyg, signed {}, drafts {}", signedIntygList.size(), draftIntygList.size());

        List<ListIntygEntry> allIntyg = new ArrayList<ListIntygEntry>();

        ListIntygEntry intygEntry;
        
        // add all signed intyg
        for (IntygItem cert : signedIntygList) {
            intygEntry = convertIntygItemToListIntygEntry(cert);
            allIntyg.add(intygEntry);
        }
        
        // add alldrafts
        for (Intyg intyg : draftIntygList) {
            intygEntry = convertIntygToListIntygEntry(intyg);
            allIntyg.add(intygEntry);
        }
        
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

    public static ListIntygEntry convertIntygToListIntygEntry(Intyg intyg) {

        ListIntygEntry ie = new ListIntygEntry();

        ie.setIntygId(intyg.getIntygsId());
        ie.setIntygType(intyg.getIntygsTyp());
        ie.setSource(IntygSource.WC);
        ie.setUpdatedSignedBy(intyg.getSenastSparadAv().getNamn());
        ie.setLastUpdatedSigned(intyg.getSenastSparadDatum());
        ie.setPatientId(intyg.getPatientPersonnummer());
        ie.setVidarebefordrad(intyg.getVidarebefordrad());

        StatusType status = convertIntygsStatusToStatusType(intyg.getStatus());
        ie.setStatus(status.toString());

        return ie;
    }
    
    private static StatusType convertIntygsStatusToStatusType(IntygsStatus intygsStatus) {
        
        switch (intygsStatus) {
        case DRAFT_COMPLETE:
            return StatusType.DRAFT_COMPLETE;
        case DRAFT_INCOMPLETE:
            return StatusType.DRAFT_INCOMPLETE;
        case SIGNED:
            return StatusType.SIGNED;
        default:
            return StatusType.UNKNOWN;
        }
    }

    public static ListIntygEntry convertIntygItemToListIntygEntry(IntygItem intygItem) {

        ListIntygEntry ie = new ListIntygEntry();

        ie.setIntygId(intygItem.getId());
        ie.setIntygType(intygItem.getType());
        ie.setStatus(findLatestStatus(intygItem.getStatuses()).toString());
        ie.setSource(IntygSource.IT);
        ie.setLastUpdatedSigned(intygItem.getSignedDate());
        ie.setUpdatedSignedBy(intygItem.getSignedBy());

        return ie;
    }

    public static StatusType findLatestStatus(List<IntygStatus> intygStatuses) {

        if (intygStatuses == null || intygStatuses.isEmpty()) {
            return StatusType.UNKNOWN;
        }
        
        CollectionUtils.filter(intygStatuses, removeArchivedIntygStatusesPredicate);
        
        if (intygStatuses.isEmpty()) {
            return StatusType.UNKNOWN;
        }
        
        IntygStatus latestStatus = Collections.max(intygStatuses, intygStatusComparator);
        return latestStatus.getType();
    }

}
