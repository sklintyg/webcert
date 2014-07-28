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

    private void assertOrder(List<ListIntygEntry> res, String expectedOrder) {

        StringBuilder sb = new StringBuilder();

        for (ListIntygEntry entry : res) {
            sb.append(entry.getIntygId());
        }

        assertEquals(expectedOrder, sb.toString());
    }

}
