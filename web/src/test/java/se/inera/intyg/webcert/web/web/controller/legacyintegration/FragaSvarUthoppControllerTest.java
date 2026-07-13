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
package se.inera.intyg.webcert.web.web.controller.legacyintegration;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Arrays;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import se.inera.intyg.webcert.infra.security.authorities.CommonAuthoritiesResolver;
import se.inera.intyg.webcert.infra.security.common.model.AuthoritiesConstants;
import se.inera.intyg.webcert.web.csintegration.aggregate.GetIssuingUnitIdAggregator;
import se.inera.intyg.webcert.web.service.user.WebCertUserService;
import se.inera.intyg.webcert.web.web.controller.facade.util.ReactUriFactory;

@ExtendWith(MockitoExtension.class)
class FragaSvarUthoppControllerTest {

  @Mock private WebCertUserService webCertUserService;
  @Mock private GetIssuingUnitIdAggregator getIssuingUnitIdAggregator;
  @Mock private ReactUriFactory reactUriFactory;
  @Mock private CommonAuthoritiesResolver commonAuthoritiesResolver;

  @InjectMocks private FragaSvarUthoppController fragaSvarUthoppController;

  @Test
  void shouldReturnArrayOfGrantedRoles() {
    final var expectedRoles =
        List.of(
            AuthoritiesConstants.ROLE_ADMIN,
            AuthoritiesConstants.ROLE_LAKARE,
            AuthoritiesConstants.ROLE_TANDLAKARE,
            AuthoritiesConstants.ROLE_SJUKSKOTERSKA,
            AuthoritiesConstants.ROLE_BARNMORSKA);
    final var grantedRoles = fragaSvarUthoppController.getGrantedRoles();
    expectedRoles.forEach(role -> assertTrue(Arrays.asList(grantedRoles).contains(role)));
  }
}
