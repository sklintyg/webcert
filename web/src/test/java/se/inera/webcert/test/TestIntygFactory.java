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
import se.inera.webcert.service.intyg.dto.StatusType;

/**
 * Util for building test data
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

        IntygStatus is1 = new IntygStatus(StatusType.RECEIVED, "FK", signedDate);
        IntygStatus is2 = new IntygStatus(StatusType.SENT, "MI", signedDate.plusSeconds(15));

        it.setStatuses(Arrays.asList(is1, is2));

        return it;
    }

    public static List<Intyg> createListWithIntygsUtkast() {

        List<Intyg> list = new ArrayList<Intyg>();

        list.add(createIntygsUtkast("2", LocalDateTime.parse("2014-01-01T10:00:00")));
        list.add(createIntygsUtkast("1", LocalDateTime.parse("2014-01-01T08:00:00")));

        return list;
    }

    public static Intyg createIntygsUtkast(String id, LocalDateTime lastUpdated) {
        return createIntygsUtkast(id, lastUpdated, "A Type", "A Person", "HSA1234", IntygsStatus.DRAFT_COMPLETE, "19121212-1212");
    }

    public static Intyg createIntygsUtkast(String id, LocalDateTime lastUpdated, String type, String modifiedBy, String modifiedByHsaId,
            IntygsStatus status, String patientId) {

        VardpersonReferens vp = new VardpersonReferens();
        vp.setNamn(modifiedBy);
        vp.setHsaId(modifiedByHsaId);

        Intyg intygsUtkast = new Intyg();

        intygsUtkast.setIntygsId(id);
        intygsUtkast.setSenastSparadAv(vp);
        intygsUtkast.setSkapadAv(vp);
        intygsUtkast.setIntygsTyp(type);
        intygsUtkast.setStatus(status);
        intygsUtkast.setSenastSparadDatum(lastUpdated);
        intygsUtkast.setPatientFornamn("Tolvan");
        intygsUtkast.setPatientEfternamn("Tolvsson");
        intygsUtkast.setPatientPersonnummer(patientId);

        return intygsUtkast;
    }

}
