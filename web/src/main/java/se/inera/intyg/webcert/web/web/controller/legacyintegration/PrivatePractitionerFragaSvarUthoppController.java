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

import static se.inera.intyg.webcert.infra.security.common.model.AuthoritiesConstants.ROLE_PRIVATLAKARE;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import se.inera.intyg.webcert.infra.security.common.model.UserOriginType;

/** Created by eriklupander on 2015-10-08. */
@Controller
@RequestMapping("/webcert/web/user/pp-certificate")
public class PrivatePractitionerFragaSvarUthoppController extends FragaSvarUthoppController {

  @Override
  protected String[] getGrantedRoles() {
    return new String[] {ROLE_PRIVATLAKARE};
  }

  @Override
  protected UserOriginType getGrantedRequestOrigin() {
    return UserOriginType.NORMAL;
  }
}
