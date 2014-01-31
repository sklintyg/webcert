package se.inera.webcert.converter;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.apache.commons.collections.comparators.ReverseComparator;
import org.joda.time.LocalDate;
import org.joda.time.LocalDateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import se.inera.certificate.integration.rest.dto.CertificateContentMeta;
import se.inera.certificate.integration.rest.dto.CertificateStatus;
import se.inera.webcert.persistence.intyg.model.Intyg;
import se.inera.webcert.persistence.intyg.model.IntygsStatus;
import se.inera.webcert.service.dto.IntygItem;
import se.inera.webcert.service.dto.IntygStatus;
import se.inera.webcert.web.controller.api.dto.IntygSource;
import se.inera.webcert.web.controller.api.dto.ListIntygEntry;

public final class IntygMerger {

    private static final Logger LOG = LoggerFactory.getLogger(IntygMerger.class);
    
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
    
    private IntygMerger() {

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
            intygEntry = convert(intyg);
            allIntyg.add(intygEntry);
        }

    }

    private static ListIntygEntry convert(Intyg intyg) {

        ListIntygEntry ie = new ListIntygEntry();

        ie.setIntygId(intyg.getIntygsId());
        ie.setIntygType(intyg.getIntygsTyp());
        ie.setSource(IntygSource.WC);
        ie.setUpdatedSignedBy(intyg.getSenastSparadAv().getNamn());
        ie.setLastUpdatedSigned(intyg.getSenastSparadDatum());
        
        IntygsStatus intygsStatus = intyg.getStatus();
        ie.setStatus(intygsStatus.toString());
        
        if (IntygsStatus.DRAFT_DISCARDED.equals(intygsStatus)) {
            ie.setDiscarded(true);
        }
        
        return ie;
    }

    private static ListIntygEntry convert(IntygItem intygItem) {
        
        ListIntygEntry ie = new ListIntygEntry();
        
        ie.setIntygId(intygItem.getId());
        ie.setIntygType(intygItem.getType());
        ie.setStatus(findLastStatus(intygItem.getStatuses()));
        ie.setSource(IntygSource.IT);
        ie.setLastUpdatedSigned(convertToLocalDateTime(intygItem.getSignedDate()));
        ie.setUpdatedSignedBy(intygItem.getSignedBy());
                
        return ie;
    }

    private static LocalDateTime convertToLocalDateTime(LocalDate localDate) {
        
        if (localDate == null) {
            return null;
        }
        
        return localDate.toLocalDateTime(localDate.toDateTimeAtStartOfDay().toLocalTime());
    }
    
    
    private static String findLastStatus(List<IntygStatus> list) {
        
        if (list == null || list.isEmpty()) {
            LOG.debug("No statuses found");
            return "-";
        }
        
        IntygStatus latestStatus = Collections.max(list, intygStatusComparator);
        
        return latestStatus.getType();
    } 
    
}
