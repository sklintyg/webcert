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
package se.inera.intyg.webcert.web.service.referens;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import se.inera.intyg.webcert.persistence.referens.model.Referens;
import se.inera.intyg.webcert.persistence.referens.repository.ReferensRepository;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class ReferensServiceTest {
    private String intygsId = "intygsId";
    private String referens = "referens";
    private Referens ref = new Referens();

    @Mock
    private ReferensRepository repo;

    @InjectMocks
    private ReferensService referensService = new ReferensServiceImpl();

    @Before
    public void setup() {
        ref.setReferens(referens);
        ref.setIntygsId(intygsId);
    }

    @Test
    public void saveReferens() {
        referensService.saveReferens(intygsId, referens);
        verify(repo).findByIntygId(intygsId);
        verify(repo).save(ref);
    }

    @Test
    public void getReferensForIntygsId() {
        when(repo.findByIntygId(intygsId)).thenReturn(ref);
        String output = referensService.getReferensForIntygsId(intygsId);
        assertEquals(referens, output);
    }

    @Test
    public void getReferensForIntygsIdReturnsNullWhenDoesntExist() {
        when(repo.findByIntygId(intygsId)).thenReturn(null);
        String output = referensService.getReferensForIntygsId(intygsId);
        assertNull(output);
    }

    @Test
    public void referensExists() {
        when(repo.findByIntygId(intygsId)).thenReturn(ref);
        assertTrue("Referens not found", referensService.referensExists(intygsId));
    }
}