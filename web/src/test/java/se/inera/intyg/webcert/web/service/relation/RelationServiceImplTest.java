package se.inera.intyg.webcert.web.service.relation;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.*;

import org.joda.time.LocalDateTime;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import se.inera.intyg.common.support.common.enumerations.RelationKod;
import se.inera.intyg.webcert.persistence.utkast.model.*;
import se.inera.intyg.webcert.persistence.utkast.repository.UtkastRepository;
import se.inera.intyg.webcert.web.web.controller.moduleapi.dto.RelationItem;

@RunWith(MockitoJUnitRunner.class)
public class RelationServiceImplTest {

    private static final String INTYGID_1 = "intyg1";
    private static final String INTYGID_2 = "intyg2";
    private static final String INTYGID_3 = "intyg3";
    private static final String INTYGID_4 = "intyg4";
    private static final String INTYGID_5 = "intyg5";

    private static LocalDateTime date = LocalDateTime.parse("2016-01-01T00:00");

    // PLEASE NOTE: Ordering matters here in this segment
    private static final Utkast UTKAST_1 = createUtkast(INTYGID_1, null, null);
    private static final Utkast UTKAST_2 = createUtkast(INTYGID_2, INTYGID_1, RelationKod.FRLANG);
    private static final Utkast UTKAST_3 = createUtkast(INTYGID_3, INTYGID_2, RelationKod.KOMPLT);
    private static final Utkast UTKAST_4 = createUtkast(INTYGID_4, INTYGID_3, RelationKod.ERSATT);
    private static final Utkast UTKAST_5 = createUtkast(INTYGID_5, INTYGID_3, RelationKod.KOMPLT);

    @Mock
    private UtkastRepository utkastRepo;

    @InjectMocks
    private RelationServiceImpl relationService;

    @Before
    public void setup() {
        /* This is setup as following:
         * Utkast 1 -- Utkast 2 -- Utkast 3 -- Utkast 4
         *                                  \- Utkast 5 */
        when(utkastRepo.findOne(eq(INTYGID_5))).thenReturn(UTKAST_5);
        when(utkastRepo.findOne(eq(INTYGID_4))).thenReturn(UTKAST_4);
        when(utkastRepo.findOne(eq(INTYGID_3))).thenReturn(UTKAST_3);
        when(utkastRepo.findOne(eq(INTYGID_2))).thenReturn(UTKAST_2);
        when(utkastRepo.findOne(eq(INTYGID_1))).thenReturn(UTKAST_1);
        when(utkastRepo.findAllByRelationIntygsId(eq(INTYGID_3))).thenReturn(Arrays.asList(UTKAST_4, UTKAST_5));
        when(utkastRepo.findAllByRelationIntygsId(eq(INTYGID_4))).thenReturn(new ArrayList<>());
    }

    @Test
    public void testGetParentRelationList() {
        List<RelationItem> res = relationService.getParentRelations(INTYGID_4);
        assertNotNull(res);
        assertEquals(3, res.size());

        assertEquals(INTYGID_3, res.get(0).getIntygsId());
        assertEquals(INTYGID_2, res.get(1).getIntygsId());
        assertEquals(INTYGID_1, res.get(2).getIntygsId());

        assertEquals(RelationKod.KOMPLT.name(), res.get(0).getKod());
        assertEquals(RelationKod.FRLANG.name(), res.get(1).getKod());
        assertEquals(null, res.get(2).getKod());

        verify(utkastRepo, times(4)).findOne(anyString());
        verify(utkastRepo, times(1)).findOne(eq(INTYGID_1));
        verify(utkastRepo, times(1)).findOne(eq(INTYGID_2));
        verify(utkastRepo, times(1)).findOne(eq(INTYGID_3));
        verify(utkastRepo, times(1)).findOne(eq(INTYGID_4));
    }

    @Test
    public void testGetParentRelationListOne() {
        List<RelationItem> res = relationService.getParentRelations(INTYGID_1);
        assertNotNull(res);
        assertTrue(res.isEmpty());

        verify(utkastRepo, times(1)).findOne(eq(INTYGID_1));
    }

    @Test
    public void testGetParentRelationListNone() {
        String notExisting = "notExisting";
        List<RelationItem> res = relationService.getParentRelations(notExisting);
        assertNotNull(res);
        assertEquals(0, res.size());
        verify(utkastRepo, times(1)).findOne(eq(notExisting));
    }

    @Test
    public void testGetChildRelationsIsSorted() {
        List<RelationItem> res = relationService.getChildRelations(INTYGID_3);
        assertNotNull(res);
        assertEquals(2, res.size());
        assertEquals(INTYGID_5, res.get(0).getIntygsId());
        assertEquals(INTYGID_4, res.get(1).getIntygsId());
        verify(utkastRepo, times(1)).findAllByRelationIntygsId(eq(INTYGID_3));
    }

    @Test
    public void testGetChildRelationsNone() {
        List<RelationItem> res = relationService.getChildRelations(INTYGID_4);
        assertNotNull(res);
        assertTrue(res.isEmpty());
        verify(utkastRepo, times(1)).findAllByRelationIntygsId(eq(INTYGID_4));
    }

    @Test
    public void testGetRelations() {
        List<RelationItem> res = relationService.getRelations(INTYGID_3);
        assertNotNull(res);
        assertEquals(5, res.size());

        assertEquals(INTYGID_5, res.get(0).getIntygsId());
        assertEquals(INTYGID_4, res.get(1).getIntygsId());
        assertEquals(INTYGID_3, res.get(2).getIntygsId());
        assertEquals(INTYGID_2, res.get(3).getIntygsId());
        assertEquals(INTYGID_1, res.get(4).getIntygsId());
        verify(utkastRepo, times(1)).findAllByRelationIntygsId(eq(INTYGID_3));
        verify(utkastRepo, times(4)).findOne(anyString());
        verify(utkastRepo, times(1)).findOne(eq(INTYGID_1));
        verify(utkastRepo, times(1)).findOne(eq(INTYGID_2));
        verify(utkastRepo, times(2)).findOne(eq(INTYGID_3));
    }

    private static Utkast createUtkast(String intygid, String parent, RelationKod kod) {
        Utkast res = new Utkast();
        res.setIntygsId(intygid);
        res.setRelationIntygsId(parent);
        res.setRelationKod(kod);
        res.setStatus(UtkastStatus.SIGNED);
        res.setSignatur(new Signatur(date, "", intygid, "", "", ""));
        date = date.plusDays(1);
        return res;
    }
}
