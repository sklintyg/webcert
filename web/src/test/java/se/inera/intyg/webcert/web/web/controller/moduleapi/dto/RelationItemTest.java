package se.inera.intyg.webcert.web.web.controller.moduleapi.dto;

import static org.junit.Assert.assertEquals;

import org.joda.time.LocalDateTime;
import org.junit.Test;

import se.inera.intyg.common.support.common.enumerations.RelationKod;
import se.inera.intyg.webcert.persistence.utkast.model.*;

public class RelationItemTest {

    @Test
    public void testConstructor() {
        final String intygsId = "intygsId";

        Signatur signatur = new Signatur(LocalDateTime.now(), "", intygsId, "", "", "");

        Utkast utkast1 = new Utkast();
        utkast1.setIntygsId(intygsId);
        utkast1.setRelationKod(RelationKod.KOMPLT);
        utkast1.setStatus(UtkastStatus.DRAFT_COMPLETE);
        utkast1.setSignatur(signatur);
        RelationItem ri1 = new RelationItem(utkast1);

        Utkast utkast2 = new Utkast();
        utkast2.setIntygsId(intygsId);
        utkast2.setRelationKod(RelationKod.KOMPLT);
        utkast2.setStatus(UtkastStatus.DRAFT_INCOMPLETE);
        utkast2.setSignatur(signatur);
        RelationItem ri2 = new RelationItem(utkast2);

        Utkast utkast3 = new Utkast();
        utkast3.setIntygsId(intygsId);
        utkast3.setRelationKod(RelationKod.KOMPLT);
        utkast3.setStatus(UtkastStatus.SIGNED);
        utkast3.setSignatur(signatur);
        RelationItem ri3 = new RelationItem(utkast3);

        assertEquals(intygsId, ri1.getIntygsId());
        assertEquals(RelationKod.KOMPLT.value(), ri1.getKod());
        assertEquals("UTKAST", ri1.getStatus());
        assertEquals("UTKAST", ri2.getStatus());
        assertEquals("INTYG", ri3.getStatus());
    }
}
