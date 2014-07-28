package se.inera.webcert.test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.joda.time.LocalDateTime;

import se.inera.webcert.persistence.intyg.model.Intyg;
import se.inera.webcert.persistence.intyg.model.IntygsStatus;
import se.inera.webcert.persistence.intyg.model.VardpersonReferens;
import se.inera.webcert.service.intyg.dto.IntygItem;
import se.inera.webcert.service.intyg.dto.IntygStatus;

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
        
        list.add(createIntygItem("3", LocalDateTime.parse("2014-01-02T10:11:23")));
        list.add(createIntygItem("4", LocalDateTime.parse("2014-01-03T12:12:18")));
        
        return list;
    }
    
    public static IntygItem createIntygItem(String id, LocalDateTime signedDate) {
        
        IntygItem it = new IntygItem();
        
        it.setId(id);
        it.setSignedBy("A Person");
        it.setSignedDate(signedDate);
        it.setType("Type 1");
        
                
        IntygStatus is1 = new IntygStatus("RECEIVED","FK", signedDate);
        IntygStatus is2 = new IntygStatus("SENT","MI", signedDate.minusSeconds(15));
        IntygStatus is3 = new IntygStatus("SIGNED","WC", signedDate.minusHours(2));
        
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
        return createIntyg(id, lastUpdated, "A Type", "A Person", "HSA1234", IntygsStatus.DRAFT_COMPLETE, "19121212-1212");
    }
    
    public static Intyg createIntyg(String id, LocalDateTime lastUpdated, String type, String modifiedBy, String modifiedByHsaId, IntygsStatus status, String patientId) {
        
        VardpersonReferens vp = new VardpersonReferens();
        vp.setNamn(modifiedBy);
        vp.setHsaId(modifiedByHsaId);
        
        Intyg it = new Intyg();
        
        it.setIntygsId(id);
        it.setSenastSparadAv(vp);
        it.setSkapadAv(vp);
        it.setIntygsTyp(type);
        it.setStatus(status);
        it.setSenastSparadDatum(lastUpdated);
        it.setPatientFornamn("Tolvan");
        it.setPatientEfternamn("Tolvsson");
        it.setPatientPersonnummer(patientId);
        
        return it;
    }
    
}
