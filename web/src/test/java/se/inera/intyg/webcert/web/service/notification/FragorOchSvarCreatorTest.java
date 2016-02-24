/*
 * Copyright (C) 2016 Inera AB (http://www.inera.se)
 *
 * This file is part of sklintyg (https://github.com/sklintyg).
 *
 * sklintyg is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * sklintyg is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package se.inera.intyg.webcert.web.service.notification;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.junit.Test;

import se.inera.intyg.common.support.modules.support.api.notification.FragorOchSvar;
import se.inera.intyg.webcert.persistence.fragasvar.model.FragaSvarStatus;
import se.inera.intyg.webcert.persistence.model.Status;

public class FragorOchSvarCreatorTest {

    private static final String FRAGESTALLARE_FK = "FK";
    private static final String FRAGESTALLARE_WEBCERT = "WC";

    private FragorOchSvarCreatorImpl fsCreator = new FragorOchSvarCreatorImpl();

    @Test
    public void testPerformCountHan8() {

        List<FragaSvarStatus> fsStatuses = Collections.singletonList(new FragaSvarStatus(1L, FRAGESTALLARE_WEBCERT, null, Status.PENDING_EXTERNAL_ACTION));
        FragorOchSvar fos = fsCreator.performCount(fsStatuses);
        assertNotNull(fos);
        assertEquals(0, fos.getAntalFragor());
        assertEquals(0, fos.getAntalHanteradeFragor());
        assertEquals(0, fos.getAntalSvar());
        assertEquals(0, fos.getAntalHanteradeSvar());
    }

    @Test
    public void testPerformCountHan7() {

        List<FragaSvarStatus> fsStatuses = Collections.singletonList(new FragaSvarStatus(1L, FRAGESTALLARE_WEBCERT, "Ett svar från FK", Status.ANSWERED));
        FragorOchSvar fos = fsCreator.performCount(fsStatuses);
        assertNotNull(fos);
        assertEquals(0, fos.getAntalFragor());
        assertEquals(0, fos.getAntalHanteradeFragor());
        assertEquals(1, fos.getAntalSvar());
        assertEquals(0, fos.getAntalHanteradeSvar());
    }

    @Test
    public void testPerformCountHan10() {

        List<FragaSvarStatus> fsStatuses = Collections.singletonList(new FragaSvarStatus(1L, FRAGESTALLARE_WEBCERT, "Ett svar från FK", Status.CLOSED));
        FragorOchSvar fos = fsCreator.performCount(fsStatuses);
        assertNotNull(fos);
        assertEquals(0, fos.getAntalFragor());
        assertEquals(0, fos.getAntalHanteradeFragor());
        assertEquals(1, fos.getAntalSvar());
        assertEquals(1, fos.getAntalHanteradeSvar());
    }

    @Test
    public void testPerformCountHan6() {

        List<FragaSvarStatus> fsStatuses = Collections.singletonList(new FragaSvarStatus(1L, FRAGESTALLARE_FK, null, Status.PENDING_INTERNAL_ACTION));
        FragorOchSvar fos = fsCreator.performCount(fsStatuses);
        assertNotNull(fos);
        assertEquals(1, fos.getAntalFragor());
        assertEquals(0, fos.getAntalHanteradeFragor());
        assertEquals(0, fos.getAntalSvar());
        assertEquals(0, fos.getAntalHanteradeSvar());
    }

    @Test
    public void testPerformCountHan9Answered() {

        List<FragaSvarStatus> fsStatuses = Collections.singletonList(new FragaSvarStatus(1L, FRAGESTALLARE_FK, "Ett svar från WC", Status.CLOSED));
        FragorOchSvar fos = fsCreator.performCount(fsStatuses);
        assertNotNull(fos);
        assertEquals(1, fos.getAntalFragor());
        assertEquals(1, fos.getAntalHanteradeFragor());
        assertEquals(0, fos.getAntalSvar());
        assertEquals(0, fos.getAntalHanteradeSvar());
    }

    @Test
    public void testPerformCountHan9NotAnswered() {

        List<FragaSvarStatus> fsStatuses = Collections.singletonList(new FragaSvarStatus(1L, FRAGESTALLARE_FK, null, Status.CLOSED));
        FragorOchSvar fos = fsCreator.performCount(fsStatuses);
        assertNotNull(fos);
        assertEquals(1, fos.getAntalFragor());
        assertEquals(1, fos.getAntalHanteradeFragor());
        assertEquals(0, fos.getAntalSvar());
        assertEquals(0, fos.getAntalHanteradeSvar());
    }

    @Test
    public void testAll() {

        // 1. Skickar fråga från WC till FK
        // Förväntad statusuppdatering: HAN8 0,0,0,0
        List<FragaSvarStatus> fsStatuses = Collections.singletonList(new FragaSvarStatus(1L, FRAGESTALLARE_WEBCERT, null, Status.PENDING_EXTERNAL_ACTION));
        FragorOchSvar fos = fsCreator.performCount(fsStatuses);
        assertNotNull(fos);
        assertEquals(0, fos.getAntalFragor());
        assertEquals(0, fos.getAntalHanteradeFragor());
        assertEquals(0, fos.getAntalSvar());
        assertEquals(0, fos.getAntalHanteradeSvar());

        // 2. FK svarar på frågan
        // Förväntad statusuppdatering: HAN7 0,0,1,0
        fsStatuses = Collections.singletonList(new FragaSvarStatus(1L, FRAGESTALLARE_WEBCERT, "Ett svar kom in", Status.ANSWERED));
        fos = fsCreator.performCount(fsStatuses);
        assertNotNull(fos);
        assertEquals(0, fos.getAntalFragor());
        assertEquals(0, fos.getAntalHanteradeFragor());
        assertEquals(1, fos.getAntalSvar());
        assertEquals(0, fos.getAntalHanteradeSvar());

        // 3. Markerar svaret som hanterat
        // Förväntad statusuppdatering: HAN10 0,0,1,1
        fsStatuses = Collections.singletonList(new FragaSvarStatus(1L, FRAGESTALLARE_WEBCERT, "Ett svar kom in", Status.CLOSED));
        fos = fsCreator.performCount(fsStatuses);
        assertNotNull(fos);
        assertEquals(0, fos.getAntalFragor());
        assertEquals(0, fos.getAntalHanteradeFragor());
        assertEquals(1, fos.getAntalSvar());
        assertEquals(1, fos.getAntalHanteradeSvar());

        // 4. FK skickar fråga till WC
        // Förväntad statusuppdatering: HAN6 1,0,1,1
        fsStatuses = Arrays.asList(new FragaSvarStatus(1L, FRAGESTALLARE_WEBCERT, "Ett svar kom in", Status.CLOSED),
                new FragaSvarStatus(1L, FRAGESTALLARE_FK, null, Status.PENDING_INTERNAL_ACTION));
        fos = fsCreator.performCount(fsStatuses);
        assertNotNull(fos);
        assertEquals(1, fos.getAntalFragor());
        assertEquals(0, fos.getAntalHanteradeFragor());
        assertEquals(1, fos.getAntalSvar());
        assertEquals(1, fos.getAntalHanteradeSvar());

        // 5. WC svarar på frågan från FK
        // Förväntad statusuppdatering: HAN9 1,1,1,1
        fsStatuses = Arrays.asList(new FragaSvarStatus(1L, FRAGESTALLARE_WEBCERT, "Ett svar kom in", Status.CLOSED),
                new FragaSvarStatus(1L, FRAGESTALLARE_FK, "Ett svar kom in", Status.CLOSED));
        fos = fsCreator.performCount(fsStatuses);
        assertNotNull(fos);
        assertEquals(1, fos.getAntalFragor());
        assertEquals(1, fos.getAntalHanteradeFragor());
        assertEquals(1, fos.getAntalSvar());
        assertEquals(1, fos.getAntalHanteradeSvar());

    }

}
