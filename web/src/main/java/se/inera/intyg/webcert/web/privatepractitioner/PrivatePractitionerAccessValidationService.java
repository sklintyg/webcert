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
package se.inera.intyg.webcert.web.privatepractitioner;

import java.util.Arrays;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import se.inera.intyg.webcert.web.service.facade.GetUserResourceLinks;
import se.inera.intyg.webcert.web.service.user.dto.WebCertUser;
import se.inera.intyg.webcert.web.web.controller.facade.dto.ResourceLinkTypeDTO;

@Component
@RequiredArgsConstructor
public class PrivatePractitionerAccessValidationService {

  private final GetUserResourceLinks getUserResourceLinks;

  public boolean hasAccessToRegister(WebCertUser user) {
    return Arrays.stream(getUserResourceLinks.get(user))
        .anyMatch(
            link ->
                (link.getType().equals(ResourceLinkTypeDTO.ACCESS_REGISTER_PRIVATE_PRACTITIONER)
                    && link.isEnabled()));
  }

  public boolean hasAccessToEdit(WebCertUser user) {
    return Arrays.stream(getUserResourceLinks.get(user))
        .anyMatch(
            link ->
                (link.getType().equals(ResourceLinkTypeDTO.ACCESS_EDIT_PRIVATE_PRACTITIONER)
                    && link.isEnabled()));
  }
}
