package se.inera.webcert.converter;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.joda.time.LocalDateTime;
import org.junit.Test;

import se.inera.webcert.persistence.intyg.model.Intyg;
import se.inera.webcert.persistence.intyg.model.IntygsStatus;
import se.inera.webcert.service.intyg.dto.IntygItem;
import se.inera.webcert.service.intyg.dto.IntygStatus;
import se.inera.webcert.service.intyg.dto.StatusType;
import se.inera.webcert.test.TestIntygFactory;
import se.inera.webcert.web.controller.api.dto.ListIntygEntry;

public class IntygDraftsConverterTest {

    public IntygDraftsConverterTest() {

    }

    @Test
    public void testMergeWithEmptyLists() {

        List<IntygItem> signedIntygList = new ArrayList<>();

        List<Intyg> draftIntygList = new ArrayList<>();

        List<ListIntygEntry> res = IntygDraftsConverter.merge(signedIntygList, draftIntygList);

        assertNotNull(res);
        assertTrue(res.isEmpty());
    }

    @Test
    public void testMergeWithBothListsFilled() {

        List<IntygItem> signedIntygList = TestIntygFactory.createListWithIntygItems();

        List<Intyg> draftIntygList = TestIntygFactory.createListWithIntygDrafts();

        List<ListIntygEntry> res = IntygDraftsConverter.merge(signedIntygList, draftIntygList);

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
        String patientId = "19121212-1212";

        List<Intyg> draftIntygList = Arrays.asList(TestIntygFactory.createIntyg(id, modfied, type, updatedSignedBy,
                updatedSignedByHsaId, IntygsStatus.DRAFT_COMPLETE, patientId));

        List<ListIntygEntry> res = IntygDraftsConverter.convertIntygToListEntries(draftIntygList);

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
        StatusType res;
        List<IntygStatus> statuses;
        
        // test with empty list
        statuses = new ArrayList<>();
        res = IntygDraftsConverter.findLatestStatus(statuses);
        assertEquals(StatusType.UNKNOWN, res);
        
        // test with just some statuses
        statuses = new ArrayList<>();
        statuses.add(new IntygStatus(StatusType.RECEIVED, "MI", defaultTime.minusHours(2)));
        statuses.add(new IntygStatus(StatusType.SENT, "FK", defaultTime.minusHours(1)));
        res = IntygDraftsConverter.findLatestStatus(statuses);
        assertEquals(StatusType.SENT, res);
        
        // test with DELETED in the list, which should be removed
        statuses = new ArrayList<>();
        statuses.add(new IntygStatus(StatusType.CANCELLED, "FK", defaultTime.minusHours(2)));
        statuses.add(new IntygStatus(StatusType.SENT, "FK", defaultTime.minusHours(3)));
        statuses.add(new IntygStatus(StatusType.DELETED, "MI", defaultTime.minusHours(1)));
        statuses.add(new IntygStatus(StatusType.RECEIVED, "MI", defaultTime.minusHours(4)));
        res = IntygDraftsConverter.findLatestStatus(statuses);
        assertEquals(StatusType.CANCELLED, res);
        
        // test with DELETED and RESTORED in the list, which should be removed
        statuses = new ArrayList<>();
        statuses.add(new IntygStatus(StatusType.CANCELLED, "FK", defaultTime.minusHours(2)));
        statuses.add(new IntygStatus(StatusType.SENT, "FK", defaultTime.minusHours(3)));
        statuses.add(new IntygStatus(StatusType.RESTORED, "MI", defaultTime.minusMinutes(30)));
        statuses.add(new IntygStatus(StatusType.DELETED, "MI", defaultTime.minusHours(1)));
        statuses.add(new IntygStatus(StatusType.RECEIVED, "MI", defaultTime.minusHours(4)));
        res = IntygDraftsConverter.findLatestStatus(statuses);
        assertEquals(StatusType.CANCELLED, res);
        
        // test with just DELETED, which will be removed and result in an empty list
        statuses = new ArrayList<>();
        statuses.add(new IntygStatus(StatusType.DELETED, "MI", defaultTime.minusHours(1)));
        res = IntygDraftsConverter.findLatestStatus(statuses);
        assertEquals(StatusType.UNKNOWN, res);
    }
    
    private void assertOrder(List<ListIntygEntry> res, String expectedOrder) {

        StringBuilder sb = new StringBuilder();

        for (ListIntygEntry entry : res) {
            sb.append(entry.getIntygId());
        }

        assertEquals(expectedOrder, sb.toString());
    }

}
