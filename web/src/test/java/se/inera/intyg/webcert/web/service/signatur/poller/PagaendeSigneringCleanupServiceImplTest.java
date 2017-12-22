/*
 * Copyright (C) 2017 Inera AB (http://www.inera.se)
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
package se.inera.intyg.webcert.web.service.signatur.poller;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import se.inera.intyg.webcert.persistence.utkast.model.PagaendeSignering;
import se.inera.intyg.webcert.persistence.utkast.repository.PagaendeSigneringRepository;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static org.mockito.Matchers.anyLong;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class PagaendeSigneringCleanupServiceImplTest {

    @Mock
    private PagaendeSigneringRepository pagaendeSigneringRepository;

    @InjectMocks
    private PagaendeSigneringCleanupServiceImpl testee = new PagaendeSigneringCleanupServiceImpl();

    @Test
    public void testCleanupNothingToDo() {
        when(pagaendeSigneringRepository.findAll()).thenReturn(new ArrayList<>());
        testee.cleanup();
        verify(pagaendeSigneringRepository, times(0)).delete(anyLong());
    }

    @Test
    public void testCleanupOneKeepOne() {
        List<PagaendeSignering> list = new ArrayList<>();
        PagaendeSignering ps1 = buildOngoingSignature("1", 1L, LocalDateTime.now().minusMinutes(20));
        PagaendeSignering ps2 = buildOngoingSignature("2", 2L, LocalDateTime.now().minusMinutes(5));

        list.add(ps1);
        list.add(ps2);

        when(pagaendeSigneringRepository.findAll()).thenReturn(list);

        testee.cleanup();
        verify(pagaendeSigneringRepository, times(1)).delete(anyLong());
    }

    private PagaendeSignering buildOngoingSignature(String intygsId, long internReferens, LocalDateTime signeringsDatum) {
        PagaendeSignering ps1 = new PagaendeSignering();
        ps1.setIntygsId(intygsId);
        ps1.setInternReferens(internReferens);
        ps1.setSigneringsDatum(signeringsDatum);
        return ps1;
    }
}
