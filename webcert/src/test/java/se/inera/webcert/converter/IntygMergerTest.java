package se.inera.webcert.converter;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.List;

import org.junit.Test;

import se.inera.webcert.persistence.intyg.model.Intyg;
import se.inera.webcert.service.dto.IntygItem;
import se.inera.webcert.test.TestIntygFactory;
import se.inera.webcert.web.controller.api.dto.ListIntygEntry;

public class IntygMergerTest {

    public IntygMergerTest() {
        
    }

    @Test
    public void testMergeWithEmptyLists() {
        
        List<IntygItem> signedIntygList = new ArrayList<>();
        
        List<Intyg> draftIntygList = new ArrayList<>();
        
        List<ListIntygEntry> res = IntygMerger.merge(signedIntygList, draftIntygList);
        
        assertNotNull(res);
        assertTrue(res.isEmpty());
    }
    
    @Test
    public void testMergeWithBothListsFilled() {
        
        List<IntygItem> signedIntygList = TestIntygFactory.createListWithIntygItems();
                
        List<Intyg> draftIntygList = TestIntygFactory.createListWithIntygDrafts();
        
        List<ListIntygEntry> res = IntygMerger.merge(signedIntygList, draftIntygList);
        
        assertNotNull(res);
        assertFalse(res.isEmpty());
        assertEquals(4, res.size());
        assertOrder(res, "4321");
    }
    
    private void assertOrder(List<ListIntygEntry> res, String expectedOrder) {
        
        StringBuilder sb = new StringBuilder();
        
        for (ListIntygEntry entry : res) {
            sb.append(entry.getIntygId());
        }
        
        assertEquals(expectedOrder, sb.toString());
    }

}
