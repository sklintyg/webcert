/*
 * Copyright (C) 2018 Inera AB (http://www.inera.se)
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
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import java.util.*;

import org.apache.commons.lang3.tuple.Pair;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import se.inera.intyg.common.support.modules.support.api.notification.ArendeCount;
import se.inera.intyg.common.support.modules.support.api.notification.FragorOchSvar;
import se.inera.intyg.webcert.persistence.arende.model.Arende;
import se.inera.intyg.webcert.persistence.arende.model.ArendeAmne;
import se.inera.intyg.webcert.persistence.arende.repository.ArendeRepository;
import se.inera.intyg.webcert.persistence.fragasvar.model.FragaSvarStatus;
import se.inera.intyg.webcert.persistence.fragasvar.repository.FragaSvarRepository;
import se.inera.intyg.webcert.persistence.model.Status;
import se.inera.intyg.webcert.web.service.fragasvar.dto.FrageStallare;

@RunWith(MockitoJUnitRunner.class)
public class FragorOchSvarCreatorTest {

    private static final String FRAGESTALLARE_FK = FrageStallare.FORSAKRINGSKASSAN.getKod();
    private static final String FRAGESTALLARE_WEBCERT = FrageStallare.WEBCERT.getKod();
    private static final String INTYG_ID = "intygsId";
    private static final String INTYGSTYP_FK7263 = "fk7263";
    private static final String INTYGSTYP_LUSE = "luse";

    @InjectMocks
    private FragorOchSvarCreatorImpl fsCreator;

    @Mock
    private FragaSvarRepository fragaSvarRepository;

    @Mock
    private ArendeRepository arendeRepository;

    @Test
    public void testPerformCountHan8() {
        when(fragaSvarRepository.findFragaSvarStatusesForIntyg(INTYG_ID))
                .thenReturn(Collections.singletonList(new FragaSvarStatus(1L, FRAGESTALLARE_WEBCERT, null, Status.PENDING_EXTERNAL_ACTION)));
        FragorOchSvar fos = fsCreator.createFragorOchSvar(INTYG_ID);
        assertNotNull(fos);
        assertEquals(0, fos.getAntalFragor());
        assertEquals(0, fos.getAntalHanteradeFragor());
        assertEquals(0, fos.getAntalSvar());
        assertEquals(0, fos.getAntalHanteradeSvar());
        verify(fragaSvarRepository).findFragaSvarStatusesForIntyg(INTYG_ID);
        verifyNoMoreInteractions(fragaSvarRepository);
        verifyNoMoreInteractions(arendeRepository);
    }

    @Test
    public void testPerformCountHan7() {
        when(fragaSvarRepository.findFragaSvarStatusesForIntyg(INTYG_ID))
                .thenReturn(Collections.singletonList(new FragaSvarStatus(1L, FRAGESTALLARE_WEBCERT, "Ett svar från FK", Status.ANSWERED)));
        FragorOchSvar fos = fsCreator.createFragorOchSvar(INTYG_ID);
        assertNotNull(fos);
        assertEquals(0, fos.getAntalFragor());
        assertEquals(0, fos.getAntalHanteradeFragor());
        assertEquals(1, fos.getAntalSvar());
        assertEquals(0, fos.getAntalHanteradeSvar());
        verify(fragaSvarRepository).findFragaSvarStatusesForIntyg(INTYG_ID);
        verifyNoMoreInteractions(fragaSvarRepository);
        verifyNoMoreInteractions(arendeRepository);
    }

    @Test
    public void testPerformCountHan10() {
        when(fragaSvarRepository.findFragaSvarStatusesForIntyg(INTYG_ID))
                .thenReturn(Collections.singletonList(new FragaSvarStatus(1L, FRAGESTALLARE_WEBCERT, "Ett svar från FK", Status.CLOSED)));
        FragorOchSvar fos = fsCreator.createFragorOchSvar(INTYG_ID);
        assertNotNull(fos);
        assertEquals(0, fos.getAntalFragor());
        assertEquals(0, fos.getAntalHanteradeFragor());
        assertEquals(1, fos.getAntalSvar());
        assertEquals(1, fos.getAntalHanteradeSvar());
        verify(fragaSvarRepository).findFragaSvarStatusesForIntyg(INTYG_ID);
        verifyNoMoreInteractions(fragaSvarRepository);
        verifyNoMoreInteractions(arendeRepository);
    }

    @Test
    public void testPerformCountHan6() {
        when(fragaSvarRepository.findFragaSvarStatusesForIntyg(INTYG_ID))
                .thenReturn(Collections.singletonList(new FragaSvarStatus(1L, FRAGESTALLARE_FK, null, Status.PENDING_INTERNAL_ACTION)));
        FragorOchSvar fos = fsCreator.createFragorOchSvar(INTYG_ID);
        assertNotNull(fos);
        assertEquals(1, fos.getAntalFragor());
        assertEquals(0, fos.getAntalHanteradeFragor());
        assertEquals(0, fos.getAntalSvar());
        assertEquals(0, fos.getAntalHanteradeSvar());
        verify(fragaSvarRepository).findFragaSvarStatusesForIntyg(INTYG_ID);
        verifyNoMoreInteractions(fragaSvarRepository);
        verifyNoMoreInteractions(arendeRepository);
    }

    @Test
    public void testPerformCountHan9Answered() {
        when(fragaSvarRepository.findFragaSvarStatusesForIntyg(INTYG_ID))
                .thenReturn(Collections.singletonList(new FragaSvarStatus(1L, FRAGESTALLARE_FK, "Ett svar från WC", Status.CLOSED)));
        FragorOchSvar fos = fsCreator.createFragorOchSvar(INTYG_ID);
        assertNotNull(fos);
        assertEquals(1, fos.getAntalFragor());
        assertEquals(1, fos.getAntalHanteradeFragor());
        assertEquals(0, fos.getAntalSvar());
        assertEquals(0, fos.getAntalHanteradeSvar());
        verify(fragaSvarRepository).findFragaSvarStatusesForIntyg(INTYG_ID);
        verifyNoMoreInteractions(fragaSvarRepository);
        verifyNoMoreInteractions(arendeRepository);
    }

    @Test
    public void testPerformCountHan9NotAnswered() {
        when(fragaSvarRepository.findFragaSvarStatusesForIntyg(INTYG_ID))
                .thenReturn(Collections.singletonList(new FragaSvarStatus(1L, FRAGESTALLARE_FK, null, Status.CLOSED)));
        FragorOchSvar fos = fsCreator.createFragorOchSvar(INTYG_ID);
        assertNotNull(fos);
        assertEquals(1, fos.getAntalFragor());
        assertEquals(1, fos.getAntalHanteradeFragor());
        assertEquals(0, fos.getAntalSvar());
        assertEquals(0, fos.getAntalHanteradeSvar());
        verify(fragaSvarRepository).findFragaSvarStatusesForIntyg(INTYG_ID);
        verifyNoMoreInteractions(fragaSvarRepository);
        verifyNoMoreInteractions(arendeRepository);
    }

    @Test
    public void testAll() {

        // 1. Skickar fråga från WC till FK
        // Förväntad statusuppdatering: HAN8 0,0,0,0
        when(fragaSvarRepository.findFragaSvarStatusesForIntyg(INTYG_ID))
                .thenReturn(Collections.singletonList(new FragaSvarStatus(1L, FRAGESTALLARE_WEBCERT, null, Status.PENDING_EXTERNAL_ACTION)));
        FragorOchSvar fos = fsCreator.createFragorOchSvar(INTYG_ID);
        assertNotNull(fos);
        assertEquals(0, fos.getAntalFragor());
        assertEquals(0, fos.getAntalHanteradeFragor());
        assertEquals(0, fos.getAntalSvar());
        assertEquals(0, fos.getAntalHanteradeSvar());

        // 2. FK svarar på frågan
        // Förväntad statusuppdatering: HAN7 0,0,1,0
        when(fragaSvarRepository.findFragaSvarStatusesForIntyg(INTYG_ID))
                .thenReturn(Collections.singletonList(new FragaSvarStatus(1L, FRAGESTALLARE_WEBCERT, "Ett svar kom in", Status.ANSWERED)));
        fos = fsCreator.createFragorOchSvar(INTYG_ID);
        assertNotNull(fos);
        assertEquals(0, fos.getAntalFragor());
        assertEquals(0, fos.getAntalHanteradeFragor());
        assertEquals(1, fos.getAntalSvar());
        assertEquals(0, fos.getAntalHanteradeSvar());

        // 3. Markerar svaret som hanterat
        // Förväntad statusuppdatering: HAN10 0,0,1,1
        when(fragaSvarRepository.findFragaSvarStatusesForIntyg(INTYG_ID))
                .thenReturn(Collections.singletonList(new FragaSvarStatus(1L, FRAGESTALLARE_WEBCERT, "Ett svar kom in", Status.CLOSED)));
        fos = fsCreator.createFragorOchSvar(INTYG_ID);
        assertNotNull(fos);
        assertEquals(0, fos.getAntalFragor());
        assertEquals(0, fos.getAntalHanteradeFragor());
        assertEquals(1, fos.getAntalSvar());
        assertEquals(1, fos.getAntalHanteradeSvar());

        // 4. FK skickar fråga till WC
        // Förväntad statusuppdatering: HAN6 1,0,1,1
        when(fragaSvarRepository.findFragaSvarStatusesForIntyg(INTYG_ID))
                .thenReturn(Arrays.asList(new FragaSvarStatus(1L, FRAGESTALLARE_WEBCERT, "Ett svar kom in", Status.CLOSED),
                        new FragaSvarStatus(1L, FRAGESTALLARE_FK, null, Status.PENDING_INTERNAL_ACTION)));
        fos = fsCreator.createFragorOchSvar(INTYG_ID);
        assertNotNull(fos);
        assertEquals(1, fos.getAntalFragor());
        assertEquals(0, fos.getAntalHanteradeFragor());
        assertEquals(1, fos.getAntalSvar());
        assertEquals(1, fos.getAntalHanteradeSvar());

        // 5. WC svarar på frågan från FK
        // Förväntad statusuppdatering: HAN9 1,1,1,1
        when(fragaSvarRepository.findFragaSvarStatusesForIntyg(INTYG_ID))
                .thenReturn(Arrays.asList(new FragaSvarStatus(1L, FRAGESTALLARE_WEBCERT, "Ett svar kom in", Status.CLOSED),
                        new FragaSvarStatus(1L, FRAGESTALLARE_FK, "Ett svar kom in", Status.CLOSED)));
        fos = fsCreator.createFragorOchSvar(INTYG_ID);
        assertNotNull(fos);
        assertEquals(1, fos.getAntalFragor());
        assertEquals(1, fos.getAntalHanteradeFragor());
        assertEquals(1, fos.getAntalSvar());
        assertEquals(1, fos.getAntalHanteradeSvar());

    }

    @Test
    public void testCountArendeOhanteradFraga() {
        when(arendeRepository.findByIntygsId(INTYG_ID)).thenReturn(Arrays
                .asList(buildArendeFkFragaOpen()));
        Pair<ArendeCount, ArendeCount> arenden = fsCreator.createArenden(INTYG_ID, INTYGSTYP_LUSE);
        assertNotNull(arenden);
        assertEquals("Antal skickade frågor", 0, arenden.getLeft().getTotalt());
        assertEquals("Antal skickade hanterade frågor", 0, arenden.getLeft().getHanterade());
        assertEquals("Antal besvarade skickade frågor", 0, arenden.getLeft().getBesvarade());
        assertEquals("Antal ej besvarade skickade frågor", 0, arenden.getLeft().getEjBesvarade());
        assertEquals("Antal mottagna frågor", 1, arenden.getRight().getTotalt());
        assertEquals("Antal mottagna hanterade frågor", 0, arenden.getRight().getHanterade());
        assertEquals("Antal besvarade mottagna frågor", 0, arenden.getRight().getBesvarade());
        assertEquals("Antal ej besvarade mottagna frågor", 1, arenden.getRight().getEjBesvarade());
        verify(arendeRepository).findByIntygsId(INTYG_ID);
        verifyNoMoreInteractions(fragaSvarRepository);
        verifyNoMoreInteractions(arendeRepository);
    }

    @Test
    public void testCountArendeOhanteradWithPaminnelseFraga() {
        when(arendeRepository.findByIntygsId(INTYG_ID)).thenReturn(Arrays
                .asList(buildArendeFkFragaOpen(), buildArendeFkFragaPaminnelseOpen()));
        Pair<ArendeCount, ArendeCount> arenden = fsCreator.createArenden(INTYG_ID, INTYGSTYP_LUSE);
        assertNotNull(arenden);
        assertEquals("Antal skickade frågor", 0, arenden.getLeft().getTotalt());
        assertEquals("Antal skickade hanterade frågor", 0, arenden.getLeft().getHanterade());
        assertEquals("Antal besvarade skickade frågor", 0, arenden.getLeft().getBesvarade());
        assertEquals("Antal ej besvarade skickade frågor", 0, arenden.getLeft().getEjBesvarade());
        assertEquals("Antal mottagna frågor", 1, arenden.getRight().getTotalt());
        assertEquals("Antal mottagna hanterade frågor", 0, arenden.getRight().getHanterade());
        assertEquals("Antal besvarade mottagna frågor", 0, arenden.getRight().getBesvarade());
        assertEquals("Antal ej besvarade mottagna frågor", 1, arenden.getRight().getEjBesvarade());
        verify(arendeRepository).findByIntygsId(INTYG_ID);
        verifyNoMoreInteractions(fragaSvarRepository);
        verifyNoMoreInteractions(arendeRepository);
    }

    @Test
    public void testCountArendeHanteradFraga() {
        when(arendeRepository.findByIntygsId(INTYG_ID)).thenReturn(Arrays
                .asList(buildArendeFkFragaClosed()));
        Pair<ArendeCount, ArendeCount> arenden = fsCreator.createArenden(INTYG_ID, INTYGSTYP_LUSE);
        assertNotNull(arenden);
        assertEquals("Antal skickade frågor", 0, arenden.getLeft().getTotalt());
        assertEquals("Antal skickade hanterade frågor", 0, arenden.getLeft().getHanterade());
        assertEquals("Antal besvarade skickade frågor", 0, arenden.getLeft().getBesvarade());
        assertEquals("Antal ej besvarade skickade frågor", 0, arenden.getLeft().getEjBesvarade());
        assertEquals("Antal mottagna frågor", 1, arenden.getRight().getTotalt());
        assertEquals("Antal mottagna hanterade frågor", 1, arenden.getRight().getHanterade());
        assertEquals("Antal besvarade mottagna frågor", 0, arenden.getRight().getBesvarade());
        assertEquals("Antal ej besvarade mottagna frågor", 0, arenden.getRight().getEjBesvarade());
        verify(arendeRepository).findByIntygsId(INTYG_ID);
        verifyNoMoreInteractions(fragaSvarRepository);
        verifyNoMoreInteractions(arendeRepository);
    }

    @Test
    public void testCountArendeOhanteratSvar() {
        when(arendeRepository.findByIntygsId(INTYG_ID)).thenReturn(buildArendeAnswerFromFKOpen());
        Pair<ArendeCount, ArendeCount> arenden = fsCreator.createArenden(INTYG_ID, INTYGSTYP_LUSE);
        assertNotNull(arenden);
        assertEquals("Antal skickade frågor", 1, arenden.getLeft().getTotalt());
        assertEquals("Antal skickade hanterade frågor", 0, arenden.getLeft().getHanterade());
        assertEquals("Antal besvarade skickade frågor", 1, arenden.getLeft().getBesvarade());
        assertEquals("Antal ej besvarade skickade frågor", 0, arenden.getLeft().getEjBesvarade());
        assertEquals("Antal mottagna frågor", 0, arenden.getRight().getTotalt());
        assertEquals("Antal mottagna hanterade frågor", 0, arenden.getRight().getHanterade());
        assertEquals("Antal besvarade mottagna frågor", 0, arenden.getRight().getBesvarade());
        assertEquals("Antal ej besvarade mottagna frågor", 0, arenden.getRight().getEjBesvarade());
        verify(arendeRepository).findByIntygsId(INTYG_ID);
        verifyNoMoreInteractions(fragaSvarRepository);
        verifyNoMoreInteractions(arendeRepository);
    }

    @Test
    public void testCountArendeHanteratSvar() {
        when(arendeRepository.findByIntygsId(INTYG_ID)).thenReturn(buildArendeAnswerFromFKClosed());
        Pair<ArendeCount, ArendeCount> arenden = fsCreator.createArenden(INTYG_ID, INTYGSTYP_LUSE);
        assertNotNull(arenden);
        assertEquals("Antal skickade frågor", 1, arenden.getLeft().getTotalt());
        assertEquals("Antal skickade hanterade frågor", 1, arenden.getLeft().getHanterade());
        assertEquals("Antal besvarade skickade frågor", 0, arenden.getLeft().getBesvarade());
        assertEquals("Antal ej besvarade skickade frågor", 0, arenden.getLeft().getEjBesvarade());
        assertEquals("Antal mottagna frågor", 0, arenden.getRight().getTotalt());
        assertEquals("Antal mottagna hanterade frågor", 0, arenden.getRight().getHanterade());
        assertEquals("Antal besvarade mottagna frågor", 0, arenden.getRight().getBesvarade());
        assertEquals("Antal ej besvarade mottagna frågor", 0, arenden.getRight().getEjBesvarade());
        verify(arendeRepository).findByIntygsId(INTYG_ID);
        verifyNoMoreInteractions(fragaSvarRepository);
        verifyNoMoreInteractions(arendeRepository);
    }

    @Test
    public void testCountArendeAll() {
        List<Arende> allArenden = new ArrayList<>();
        allArenden.addAll(Arrays.asList(buildArendeFkFragaOpen(), buildArendeFkFragaClosed()));
        allArenden.addAll(buildArendeAnswerFromFKClosed());
        allArenden.addAll(buildArendeAnswerFromFKOpen());
        when(arendeRepository.findByIntygsId(INTYG_ID)).thenReturn(allArenden);

        Pair<ArendeCount, ArendeCount> arenden = fsCreator.createArenden(INTYG_ID, INTYGSTYP_LUSE);
        assertNotNull(arenden);
        assertEquals("Antal skickade frågor", 2, arenden.getLeft().getTotalt());
        assertEquals("Antal skickade hanterade frågor", 1, arenden.getLeft().getHanterade());
        assertEquals("Antal besvarade skickade frågor", 1, arenden.getLeft().getBesvarade());
        assertEquals("Antal ej besvarade skickade frågor", 0, arenden.getLeft().getEjBesvarade());
        assertEquals("Antal mottagna frågor", 2, arenden.getRight().getTotalt());
        assertEquals("Antal mottagna hanterade frågor", 1, arenden.getRight().getHanterade());
        assertEquals("Antal besvarade mottagna frågor", 0, arenden.getRight().getBesvarade());
        assertEquals("Antal ej besvarade mottagna frågor", 1, arenden.getRight().getEjBesvarade());
        verify(arendeRepository).findByIntygsId(INTYG_ID);
        verifyNoMoreInteractions(fragaSvarRepository);
        verifyNoMoreInteractions(arendeRepository);
    }

    @Test
    public void testCountArendeFk7263OhanteradFraga() {
        when(fragaSvarRepository.findFragaSvarStatusesForIntyg(INTYG_ID))
                .thenReturn(Collections.singletonList(new FragaSvarStatus(1L, FRAGESTALLARE_FK, null, Status.PENDING_INTERNAL_ACTION)));
        Pair<ArendeCount, ArendeCount> arenden = fsCreator.createArenden(INTYG_ID, INTYGSTYP_FK7263);
        assertNotNull(arenden);
        assertEquals("Antal skickade frågor", 0, arenden.getLeft().getTotalt());
        assertEquals("Antal skickade hanterade frågor", 0, arenden.getLeft().getHanterade());
        assertEquals("Antal besvarade skickade frågor", 0, arenden.getLeft().getBesvarade());
        assertEquals("Antal ej besvarade skickade frågor", 0, arenden.getLeft().getEjBesvarade());
        assertEquals("Antal mottagna frågor", 1, arenden.getRight().getTotalt());
        assertEquals("Antal mottagna hanterade frågor", 0, arenden.getRight().getHanterade());
        assertEquals("Antal besvarade mottagna frågor", 0, arenden.getRight().getBesvarade());
        assertEquals("Antal ej besvarade mottagna frågor", 1, arenden.getRight().getEjBesvarade());
        verify(fragaSvarRepository).findFragaSvarStatusesForIntyg(INTYG_ID);
        verifyNoMoreInteractions(fragaSvarRepository);
        verifyNoMoreInteractions(arendeRepository);
    }

    @Test
    public void testCountArendeFk7263HanteradFraga() {
        when(fragaSvarRepository.findFragaSvarStatusesForIntyg(INTYG_ID))
                .thenReturn(Collections.singletonList(new FragaSvarStatus(1L, FRAGESTALLARE_FK, "Ett svar från WC", Status.CLOSED)));
        Pair<ArendeCount, ArendeCount> arenden = fsCreator.createArenden(INTYG_ID, INTYGSTYP_FK7263);
        assertNotNull(arenden);
        assertEquals("Antal skickade frågor", 0, arenden.getLeft().getTotalt());
        assertEquals("Antal skickade hanterade frågor", 0, arenden.getLeft().getHanterade());
        assertEquals("Antal besvarade skickade frågor", 0, arenden.getLeft().getBesvarade());
        assertEquals("Antal ej besvarade skickade frågor", 0, arenden.getLeft().getEjBesvarade());
        assertEquals("Antal mottagna frågor", 1, arenden.getRight().getTotalt());
        assertEquals("Antal mottagna hanterade frågor", 1, arenden.getRight().getHanterade());
        assertEquals("Antal besvarade mottagna frågor", 0, arenden.getRight().getBesvarade());
        assertEquals("Antal ej besvarade mottagna frågor", 0, arenden.getRight().getEjBesvarade());
        verify(fragaSvarRepository).findFragaSvarStatusesForIntyg(INTYG_ID);
        verifyNoMoreInteractions(fragaSvarRepository);
        verifyNoMoreInteractions(arendeRepository);
    }

    @Test
    public void testCountArendeFk7263OhanteratSvar() {
        when(fragaSvarRepository.findFragaSvarStatusesForIntyg(INTYG_ID))
                .thenReturn(Collections.singletonList(new FragaSvarStatus(1L, FRAGESTALLARE_WEBCERT, "Ett svar från FK", Status.ANSWERED)));
        Pair<ArendeCount, ArendeCount> arenden = fsCreator.createArenden(INTYG_ID, INTYGSTYP_FK7263);
        assertNotNull(arenden);
        assertEquals("Antal skickade frågor", 1, arenden.getLeft().getTotalt());
        assertEquals("Antal skickade hanterade frågor", 0, arenden.getLeft().getHanterade());
        assertEquals("Antal besvarade skickade frågor", 1, arenden.getLeft().getBesvarade());
        assertEquals("Antal ej besvarade skickade frågor", 0, arenden.getLeft().getEjBesvarade());
        assertEquals("Antal mottagna frågor", 0, arenden.getRight().getTotalt());
        assertEquals("Antal mottagna hanterade frågor", 0, arenden.getRight().getHanterade());
        assertEquals("Antal besvarade mottagna frågor", 0, arenden.getRight().getBesvarade());
        assertEquals("Antal ej besvarade mottagna frågor", 0, arenden.getRight().getEjBesvarade());
        verify(fragaSvarRepository).findFragaSvarStatusesForIntyg(INTYG_ID);
        verifyNoMoreInteractions(fragaSvarRepository);
        verifyNoMoreInteractions(arendeRepository);
    }

    @Test
    public void testCountArendeFk7263HanteratSvar() {
        when(fragaSvarRepository.findFragaSvarStatusesForIntyg(INTYG_ID))
                .thenReturn(Collections.singletonList(new FragaSvarStatus(1L, FRAGESTALLARE_WEBCERT, "Ett svar från FK", Status.CLOSED)));
        Pair<ArendeCount, ArendeCount> arenden = fsCreator.createArenden(INTYG_ID, INTYGSTYP_FK7263);
        assertNotNull(arenden);
        assertEquals("Antal skickade frågor", 1, arenden.getLeft().getTotalt());
        assertEquals("Antal skickade hanterade frågor", 1, arenden.getLeft().getHanterade());
        assertEquals("Antal besvarade skickade frågor", 0, arenden.getLeft().getBesvarade());
        assertEquals("Antal ej besvarade skickade frågor", 0, arenden.getLeft().getEjBesvarade());
        assertEquals("Antal mottagna frågor", 0, arenden.getRight().getTotalt());
        assertEquals("Antal mottagna hanterade frågor", 0, arenden.getRight().getHanterade());
        assertEquals("Antal besvarade mottagna frågor", 0, arenden.getRight().getBesvarade());
        assertEquals("Antal ej besvarade mottagna frågor", 0, arenden.getRight().getEjBesvarade());
        verify(fragaSvarRepository).findFragaSvarStatusesForIntyg(INTYG_ID);
        verifyNoMoreInteractions(fragaSvarRepository);
        verifyNoMoreInteractions(arendeRepository);
    }

    @Test
    public void testCountArendeFk7263All() {
        when(fragaSvarRepository.findFragaSvarStatusesForIntyg(INTYG_ID))
                .thenReturn(Arrays.asList(new FragaSvarStatus(1L, FRAGESTALLARE_WEBCERT, "Ett svar kom in", Status.CLOSED),
                        new FragaSvarStatus(1L, FRAGESTALLARE_FK, "Ett svar kom in", Status.CLOSED)));

        Pair<ArendeCount, ArendeCount> arenden = fsCreator.createArenden(INTYG_ID, INTYGSTYP_FK7263);
        assertNotNull(arenden);
        assertEquals("Antal skickade frågor", 1, arenden.getLeft().getTotalt());
        assertEquals("Antal skickade hanterade frågor", 1, arenden.getLeft().getHanterade());
        assertEquals("Antal besvarade skickade frågor", 0, arenden.getLeft().getBesvarade());
        assertEquals("Antal ej besvarade skickade frågor", 0, arenden.getLeft().getEjBesvarade());
        assertEquals("Antal mottagna frågor", 1, arenden.getRight().getTotalt());
        assertEquals("Antal mottagna hanterade frågor", 1, arenden.getRight().getHanterade());
        assertEquals("Antal besvarade mottagna frågor", 0, arenden.getRight().getBesvarade());
        assertEquals("Antal ej besvarade mottagna frågor", 0, arenden.getRight().getEjBesvarade());
        verify(fragaSvarRepository).findFragaSvarStatusesForIntyg(INTYG_ID);
        verifyNoMoreInteractions(fragaSvarRepository);
        verifyNoMoreInteractions(arendeRepository);
    }

    private List<Arende> buildArendeAnswerFromFKClosed() {
        final String svarPaId = UUID.randomUUID().toString();
        Arende arende = new Arende();
        arende.setSkickatAv(FRAGESTALLARE_WEBCERT);
        arende.setStatus(Status.CLOSED);
        arende.setMeddelandeId(svarPaId);
        Arende answer = new Arende();
        answer.setSkickatAv(FRAGESTALLARE_FK);
        answer.setStatus(Status.ANSWERED);
        answer.setSvarPaId(svarPaId);
        return Arrays.asList(arende, answer);
    }

    private List<Arende> buildArendeAnswerFromFKOpen() {
        final String svarPaId = UUID.randomUUID().toString();
        Arende arende = new Arende();
        arende.setSkickatAv(FRAGESTALLARE_WEBCERT);
        arende.setStatus(Status.ANSWERED);
        arende.setMeddelandeId(svarPaId);
        Arende answer = new Arende();
        answer.setSkickatAv(FRAGESTALLARE_FK);
        answer.setStatus(Status.ANSWERED);
        answer.setSvarPaId(svarPaId);
        return Arrays.asList(arende, answer);
    }

    private Arende buildArendeFkFragaClosed() {
        Arende arende = new Arende();
        arende.setSkickatAv(FRAGESTALLARE_FK);
        arende.setStatus(Status.CLOSED);
        return arende;
    }

    private Arende buildArendeFkFragaOpen() {
        Arende arende = new Arende();
        arende.setSkickatAv(FRAGESTALLARE_FK);
        arende.setStatus(Status.PENDING_INTERNAL_ACTION);
        return arende;
    }

    private Arende buildArendeFkFragaPaminnelseOpen() {
        Arende arende = new Arende();
        arende.setSkickatAv(FRAGESTALLARE_FK);
        arende.setStatus(Status.PENDING_INTERNAL_ACTION);
        arende.setAmne(ArendeAmne.PAMINN);
        arende.setPaminnelseMeddelandeId("paminnelseMeddelandeId");
        return arende;
    }
}
