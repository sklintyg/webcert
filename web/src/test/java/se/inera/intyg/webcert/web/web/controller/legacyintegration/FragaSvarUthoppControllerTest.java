/*
 * Copyright (C) 2025 Inera AB (http://www.inera.se)
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
import se.inera.intyg.infra.security.common.model.AuthoritiesConstants;

class FragaSvarUthoppControllerTest {

    private final FragaSvarUthoppController fragaSvarUthoppController = new FragaSvarUthoppController();

    @Test
    void shouldReturnArrayOfGrantedRoles() {
        final var expectedRoles = List.of(
            AuthoritiesConstants.ROLE_ADMIN, AuthoritiesConstants.ROLE_LAKARE, AuthoritiesConstants.ROLE_TANDLAKARE,
            AuthoritiesConstants.ROLE_SJUKSKOTERSKA, AuthoritiesConstants.ROLE_BARNMORSKA);
        final var grantedRoles = fragaSvarUthoppController.getGrantedRoles();
        expectedRoles.forEach(role -> assertTrue(Arrays.asList(grantedRoles).contains(role)));
    }
}
