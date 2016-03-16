package se.inera.intyg.webcert.web.service.relation;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.when;

import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import se.inera.intyg.common.support.common.enumerations.RelationKod;
import se.inera.intyg.webcert.persistence.utkast.model.Utkast;
import se.inera.intyg.webcert.persistence.utkast.model.UtkastStatus;
import se.inera.intyg.webcert.persistence.utkast.repository.UtkastRepository;
import se.inera.intyg.webcert.web.web.controller.moduleapi.dto.RelationItem;

@RunWith(MockitoJUnitRunner.class)
public class RelationServiceImplTest {

    private static final String INTYGID_1 = "intyg1";
    private static final String INTYGID_2 = "intyg2";
    private static final String INTYGID_3 = "intyg3";
    private static final String INTYGID_4 = "intyg4";

    private static final Utkast UTKAST_1 = createUtkast(INTYGID_1, null, null);
    private static final Utkast UTKAST_2 = createUtkast(INTYGID_2, INTYGID_1, RelationKod.FRLANG);
    private static final Utkast UTKAST_3 = createUtkast(INTYGID_3, INTYGID_2, RelationKod.KOMPLT);
    private static final Utkast UTKAST_4 = createUtkast(INTYGID_4, INTYGID_3, RelationKod.ERSATT);

    @Mock
    private UtkastRepository utkastRepo;

    @InjectMocks
    private RelationServiceImpl relationService;

    @Before
    public void setup() {
        when(utkastRepo.findOne(eq(INTYGID_4))).thenReturn(UTKAST_4);
        when(utkastRepo.findOne(eq(INTYGID_3))).thenReturn(UTKAST_3);
        when(utkastRepo.findOne(eq(INTYGID_2))).thenReturn(UTKAST_2);
        when(utkastRepo.findOne(eq(INTYGID_1))).thenReturn(UTKAST_1);
    }

    @Test
    public void testGetRelationList() {
        List<RelationItem> res = relationService.getRelations(INTYGID_4);
        assertNotNull(res);
        assertEquals(4, res.size());

        assertEquals(INTYGID_4, res.get(0).getIntygsId());
        assertEquals(INTYGID_3, res.get(1).getIntygsId());
        assertEquals(INTYGID_2, res.get(2).getIntygsId());
        assertEquals(INTYGID_1, res.get(3).getIntygsId());

        assertEquals(RelationKod.ERSATT.name(), res.get(0).getKod());
        assertEquals(RelationKod.KOMPLT.name(), res.get(1).getKod());
        assertEquals(RelationKod.FRLANG.name(), res.get(2).getKod());
        assertEquals(null, res.get(3).getKod());
    }

    @Test
    public void testGetRelationListOne() {
        List<RelationItem> res = relationService.getRelations(INTYGID_1);
        assertNotNull(res);
        assertEquals(1, res.size());

        assertEquals(INTYGID_1, res.get(0).getIntygsId());
        assertEquals(null, res.get(0).getKod());
    }

    @Test
    public void testGetRelationListNone() {
        List<RelationItem> res = relationService.getRelations("finns_ej");
        assertNotNull(res);
        assertEquals(0, res.size());
    }

    private static Utkast createUtkast(String intygid, String parent, RelationKod kod) {
        Utkast res = new Utkast();
        res.setIntygsId(intygid);
        res.setRelationIntygsId(parent);
        res.setRelationKod(kod);
        res.setStatus(UtkastStatus.SIGNED);
        return res;
    }
}
