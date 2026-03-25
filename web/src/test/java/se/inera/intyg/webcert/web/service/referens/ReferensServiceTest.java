/*
 * Copyright (C) 2026 Inera AB (http://www.inera.se)
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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import se.inera.intyg.webcert.persistence.referens.model.Referens;
import se.inera.intyg.webcert.persistence.referens.repository.ReferensRepository;

@ExtendWith(MockitoExtension.class)
class ReferensServiceTest {

  private String intygsId = "intygsId";
  private String referens = "referens";
  private Referens ref = new Referens();

  @Mock private ReferensRepository repo;

  @InjectMocks private ReferensService referensService = new ReferensServiceImpl();

  @BeforeEach
  void setup() {
    ref.setReferens(referens);
    ref.setIntygsId(intygsId);
  }

  @Test
  void saveReferens() {
    referensService.saveReferens(intygsId, referens);
    verify(repo).findByIntygId(intygsId);
    verify(repo).save(ref);
  }

  @Test
  void getReferensForIntygsId() {
    when(repo.findByIntygId(intygsId)).thenReturn(ref);
    String output = referensService.getReferensForIntygsId(intygsId);
    assertEquals(referens, output);
  }

  @Test
  void getReferensForIntygsIdReturnsNullWhenDoesntExist() {
    when(repo.findByIntygId(intygsId)).thenReturn(null);
    String output = referensService.getReferensForIntygsId(intygsId);
    assertNull(output);
  }

  @Test
  void referensExists() {
    when(repo.findByIntygId(intygsId)).thenReturn(ref);
    assertTrue(referensService.referensExists(intygsId), "Referens not found");
  }
}
