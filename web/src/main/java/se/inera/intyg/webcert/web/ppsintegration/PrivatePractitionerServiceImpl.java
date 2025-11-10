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

package se.inera.intyg.webcert.web.ppsintegration;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;
import se.inera.intyg.webcert.integration.privatepractitioner.model.GetPrivatePractitionerConfigResponse;
import se.inera.intyg.webcert.integration.privatepractitioner.service.PrivatePractitionerIntegratonService;
import se.inera.intyg.webcert.web.ppsintegration.dto.HospInformation;
import se.inera.intyg.webcert.web.ppsintegration.dto.PrivatePractitioner;
import se.inera.intyg.webcert.web.service.facade.user.UserService;
import se.inera.intyg.webcert.web.web.controller.api.dto.PrivatePractitionerDTO;
import se.inera.intyg.webcert.web.web.controller.api.dto.PrivatePractitionerRegisterRequest;

@Service
@Profile("private-practitioner-service-active")
@RequiredArgsConstructor
public class PrivatePractitionerServiceImpl implements PrivatePractitionerService {
    private final UserService userService;
    private final PrivatePractitionerIntegratonService privatePractitionerIntegratonService;

    @Override
    public PrivatePractitioner registerPrivatePractitioner(
        PrivatePractitionerRegisterRequest privatePractitionerRegisterRequest) {
        return null;
    }

    @Override
    public PrivatePractitioner getPrivatePractitioner() {
        return null;
    }

    @Override
    public void updatePrivatePractitioner(PrivatePractitionerDTO privatePractitioner) {
    }

    @Override
    public GetPrivatePractitionerConfigResponse getPrivatePractitionerConfig() {
        return privatePractitionerIntegratonService.getPrivatePractitionerConfig();
    }

    @Override
    public HospInformation getHospInformation() {
      final var hsaId = userService.getLoggedInUser().getHsaId();
      return HospInformation.convert(privatePractitionerIntegratonService.getHospInformation(hsaId));
    }


}
