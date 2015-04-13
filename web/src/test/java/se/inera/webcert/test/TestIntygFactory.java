package se.inera.webcert.test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.joda.time.LocalDateTime;

import se.inera.certificate.model.CertificateState;
import se.inera.certificate.model.Status;
import se.inera.webcert.persistence.utkast.model.Utkast;
import se.inera.webcert.persistence.utkast.model.UtkastStatus;
import se.inera.webcert.persistence.utkast.model.VardpersonReferens;
import se.inera.webcert.service.intyg.dto.IntygItem;
import se.inera.webcert.service.intyg.dto.IntygItemListResponse;

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

    public static IntygItemListResponse createIntygItemListResponse(List<IntygItem> intygItemList, boolean offlineMode) {
        return new IntygItemListResponse(intygItemList, offlineMode);
    }

    public static IntygItem createIntygItem(String id, LocalDateTime signedDate) {

        IntygItem it = new IntygItem();

        it.setId(id);
        it.setSignedBy("A Person");
        it.setSignedDate(signedDate);
        it.setType("Type 1");

        Status is1 = new Status(CertificateState.RECEIVED, "FK", signedDate);
        Status is2 = new Status(CertificateState.SENT, "MI", signedDate.plusSeconds(15));

        it.setStatuses(Arrays.asList(is1, is2));

        return it;
    }

    public static List<Utkast> createListWithUtkast() {

        List<Utkast> list = new ArrayList<Utkast>();

        list.add(createUtkast("2", LocalDateTime.parse("2014-01-01T10:00:00")));
        list.add(createUtkast("1", LocalDateTime.parse("2014-01-01T08:00:00")));

        return list;
    }

    public static Utkast createUtkast(String id, LocalDateTime lastUpdated) {
        return createUtkast(id, lastUpdated, "A Type", "A Person", "HSA1234", UtkastStatus.DRAFT_COMPLETE, "19121212-1212");
    }

    public static Utkast createUtkast(String id, LocalDateTime lastUpdated, String type, String modifiedBy, String modifiedByHsaId,
            UtkastStatus status, String patientId) {

        VardpersonReferens vp = new VardpersonReferens();
        vp.setNamn(modifiedBy);
        vp.setHsaId(modifiedByHsaId);

        Utkast utkast = new Utkast();

        utkast.setIntygsId(id);
        utkast.setSenastSparadAv(vp);
        utkast.setSkapadAv(vp);
        utkast.setIntygsTyp(type);
        utkast.setStatus(status);
        utkast.setSenastSparadDatum(lastUpdated);
        utkast.setPatientFornamn("Tolvan");
        utkast.setPatientEfternamn("Tolvsson");
        utkast.setPatientPersonnummer(patientId);

        return utkast;
    }

}
