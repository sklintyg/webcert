package se.inera.webcert.test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.joda.time.LocalDate;
import org.joda.time.LocalDateTime;

import se.inera.webcert.persistence.intyg.model.Intyg;
import se.inera.webcert.persistence.intyg.model.IntygsStatus;
import se.inera.webcert.persistence.intyg.model.VardpersonReferens;
import se.inera.webcert.service.dto.IntygItem;
import se.inera.webcert.service.dto.IntygStatus;

/**
 * Util for buildting test data
 * 
 * @author nikpet
 *
 */
public final class TestIntygFactory {

    private TestIntygFactory() {
        
    }
    
    public static List<IntygItem> createListWithIntygItems() {
        
        List<IntygItem> list = new ArrayList<IntygItem>();
        
        list.add(createIntygItem("3", LocalDate.parse("2014-01-02")));
        list.add(createIntygItem("4", LocalDate.parse("2014-01-03")));
        
        return list;
    }
    
    public static IntygItem createIntygItem(String id, LocalDate signedDate) {
        
        IntygItem it = new IntygItem();
        
        it.setId(id);
        it.setSignedBy("A Person");
        it.setSignedDate(signedDate);
        it.setType("Type 1");
        
        
        LocalDateTime statusDate = signedDate.toLocalDateTime(signedDate.toDateTimeAtStartOfDay().toLocalTime()); 
        
        IntygStatus is1 = new IntygStatus("RECEIVED","FK", statusDate);
        IntygStatus is2 = new IntygStatus("SENT","MI", statusDate.minusSeconds(15));
        IntygStatus is3 = new IntygStatus("SIGNED","WC", statusDate.minusHours(2));
        
        it.setStatuses(Arrays.asList(is1, is2, is3));
        
        return it;
    }
    
    public static List<Intyg> createListWithIntygDrafts() {
        
        List<Intyg> list = new ArrayList<Intyg>();
        
        list.add(createIntyg("2", LocalDateTime.parse("2014-01-01T10:00:00")));
        list.add(createIntyg("1", LocalDateTime.parse("2014-01-01T08:00:00")));
        
        return list;
    }
    
    public static Intyg createIntyg(String id, LocalDateTime lastUpdated) {
        
        VardpersonReferens vp = new VardpersonReferens();
        vp.setNamn("A Person");
        vp.setHsaId("ABC1234");
        
        Intyg it = new Intyg();
        
        it.setIntygsId(id);
        it.setSenastSparadAv(vp);
        it.setSkapadAv(vp);
        it.setIntygsTyp("Type 1");
        it.setStatus(IntygsStatus.DRAFT_COMPLETE);
        it.setSenastSparadDatum(lastUpdated);
        
        return it;
    }
    
}
