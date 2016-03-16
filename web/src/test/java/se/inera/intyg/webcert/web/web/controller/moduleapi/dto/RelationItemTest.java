package se.inera.intyg.webcert.web.web.controller.moduleapi.dto;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import se.inera.intyg.common.support.common.enumerations.RelationKod;
import se.inera.intyg.webcert.persistence.utkast.model.UtkastStatus;

public class RelationItemTest {

    @Test
    public void testConstructor() {
        final String intygsId = "intygsId";
        final RelationKod relationsKod = RelationKod.KOMPLT;
        final UtkastStatus draft_complete = UtkastStatus.DRAFT_COMPLETE;
        final UtkastStatus draft_incomplete = UtkastStatus.DRAFT_INCOMPLETE;
        final UtkastStatus signed = UtkastStatus.SIGNED;

        RelationItem ri1 = new RelationItem(intygsId, relationsKod, draft_complete);
        RelationItem ri2 = new RelationItem(intygsId, relationsKod, draft_incomplete);
        RelationItem ri3 = new RelationItem(intygsId, relationsKod, signed);

        assertEquals(intygsId, ri1.getIntygsId());
        assertEquals(relationsKod.value(), ri1.getKod());
        assertEquals("UTKAST", ri1.getStatus());
        assertEquals("UTKAST", ri2.getStatus());
        assertEquals("INTYG", ri3.getStatus());
    }
}
