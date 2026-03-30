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
package se.inera.intyg.webcert.web.web.controller.testability.facade;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Profile;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import se.inera.intyg.webcert.web.service.testability.FakeLoginService;
import se.inera.intyg.webcert.web.web.controller.testability.facade.dto.FakeLoginDTO;

@RequiredArgsConstructor
@Slf4j
@RestController
@RequestMapping("/testability")
@Profile({"dev", "testability-api"})
public class FakeLoginTestabilityController {

  private final FakeLoginService fakeLoginService;
  private final ObjectMapper objectMapper;

  @PostMapping("/fake")
  public void login(HttpServletRequest request, @RequestBody FakeLoginDTO fakeLoginDTO) {
    fakeLoginService.login(fakeLoginDTO, request);
  }

  @PostMapping("/logout")
  public void logout(HttpServletRequest request) {
    fakeLoginService.logout(request.getSession(false));
  }
}
