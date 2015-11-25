package se.inera.intyg.webcert.web.converter;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.joda.time.LocalDateTime;
import org.junit.Test;

import se.inera.intyg.common.support.model.CertificateState;
import se.inera.intyg.common.support.model.Status;
import se.inera.intyg.common.support.modules.support.api.dto.Personnummer;
import se.inera.intyg.webcert.persistence.utkast.model.Utkast;
import se.inera.intyg.webcert.persistence.utkast.model.UtkastStatus;
import se.inera.intyg.webcert.web.service.intyg.dto.IntygItem;
import se.inera.intyg.webcert.web.test.TestIntygFactory;
import se.inera.intyg.webcert.web.web.controller.api.dto.ListIntygEntry;

public class IntygDraftsConverterTest {

    public IntygDraftsConverterTest() {

    }

    @Test
    public void testMergeWithEmptyLists() {

        List<IntygItem> intygList = new ArrayList<>();

        List<Utkast> utkastList = new ArrayList<>();

        List<ListIntygEntry> res = IntygDraftsConverter.merge(intygList, utkastList);

        assertNotNull(res);
        assertTrue(res.isEmpty());
    }

    @Test
    public void testMergeWithBothListsFilled() {

        List<IntygItem> intygList = TestIntygFactory.createListWithIntygItems();

        List<Utkast> utkastList = TestIntygFactory.createListWithUtkast();

        List<ListIntygEntry> res = IntygDraftsConverter.merge(intygList, utkastList);

        assertNotNull(res);
        assertFalse(res.isEmpty());
        assertEquals(4, res.size());
        assertOrder(res, "4321");
    }

    @Test
    public void testConvertIntygToListEntries() {

        LocalDateTime modfied = LocalDateTime.parse("2014-01-01T10:00:00");
        String id = "123";
        String type = "type";
        String updatedSignedBy = "Dr Dengroth";
        String updatedSignedByHsaId = "HSA1234";
        Personnummer patientId = new Personnummer("19121212-1212");

        List<Utkast> utkastList = Collections.singletonList(TestIntygFactory.createUtkast(id, modfied, type, updatedSignedBy,
                updatedSignedByHsaId, UtkastStatus.DRAFT_COMPLETE, patientId));

        List<ListIntygEntry> res = IntygDraftsConverter.convertUtkastsToListIntygEntries(utkastList);

        assertNotNull(res);
        assertEquals(1, res.size());

        ListIntygEntry ref = res.get(0);
        assertEquals(id, ref.getIntygId());
        assertEquals(type, ref.getIntygType());
        assertEquals("DRAFT_COMPLETE", ref.getStatus());
        assertEquals(updatedSignedBy, ref.getUpdatedSignedBy());
        assertEquals(modfied, ref.getLastUpdatedSigned());
    }

    @Test
    public void testFindLatestStatus() {

        LocalDateTime defaultTime = LocalDateTime.now();
        CertificateState res;
        List<Status> statuses;

        // test with empty list
        statuses = new ArrayList<>();
        res = IntygDraftsConverter.findLatestStatus(statuses);
        assertEquals(CertificateState.UNHANDLED, res);

        // test with just some statuses
        statuses = new ArrayList<>();
        statuses.add(new Status(CertificateState.RECEIVED, "MI", defaultTime.minusHours(2)));
        statuses.add(new Status(CertificateState.SENT, "FK", defaultTime.minusHours(1)));
        res = IntygDraftsConverter.findLatestStatus(statuses);
        assertEquals(CertificateState.SENT, res);

        // test with DELETED in the list, which should be removed
        statuses = new ArrayList<>();
        statuses.add(new Status(CertificateState.CANCELLED, "FK", defaultTime.minusHours(2)));
        statuses.add(new Status(CertificateState.SENT, "FK", defaultTime.minusHours(3)));
        statuses.add(new Status(CertificateState.DELETED, "MI", defaultTime.minusHours(1)));
        statuses.add(new Status(CertificateState.RECEIVED, "MI", defaultTime.minusHours(4)));
        res = IntygDraftsConverter.findLatestStatus(statuses);
        assertEquals(CertificateState.CANCELLED, res);

        // test with DELETED and RESTORED in the list, which should be removed
        statuses = new ArrayList<>();
        statuses.add(new Status(CertificateState.CANCELLED, "FK", defaultTime.minusHours(2)));
        statuses.add(new Status(CertificateState.SENT, "FK", defaultTime.minusHours(3)));
        statuses.add(new Status(CertificateState.RESTORED, "MI", defaultTime.minusMinutes(30)));
        statuses.add(new Status(CertificateState.DELETED, "MI", defaultTime.minusHours(1)));
        statuses.add(new Status(CertificateState.RECEIVED, "MI", defaultTime.minusHours(4)));
        res = IntygDraftsConverter.findLatestStatus(statuses);
        assertEquals(CertificateState.CANCELLED, res);

        // test with just DELETED, which will be removed and result in an empty list
        statuses = new ArrayList<>();
        statuses.add(new Status(CertificateState.DELETED, "MI", defaultTime.minusHours(1)));
        res = IntygDraftsConverter.findLatestStatus(statuses);
        assertEquals(CertificateState.UNHANDLED, res);
    }

    private void assertOrder(List<ListIntygEntry> res, String expectedOrder) {

        StringBuilder sb = new StringBuilder();

        for (ListIntygEntry entry : res) {
            sb.append(entry.getIntygId());
        }

        assertEquals(expectedOrder, sb.toString());
    }

}
