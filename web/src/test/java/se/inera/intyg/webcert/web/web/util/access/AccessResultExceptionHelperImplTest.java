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
package se.inera.intyg.webcert.web.web.util.access;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import se.inera.intyg.infra.security.authorities.AuthoritiesException;
import se.inera.intyg.webcert.common.service.exception.WebCertServiceException;
import se.inera.intyg.webcert.web.service.access.AccessResult;
import se.inera.intyg.webcert.web.service.access.AccessResultCode;

@ExtendWith(MockitoExtension.class)
public class AccessResultExceptionHelperImplTest {

  @InjectMocks private AccessResultExceptionHelperImpl accessResultExceptionHelper;

  @Test
  public void throwPUProblemException() {
    assertThrows(WebCertServiceException.class, () -> {
    accessResultExceptionHelper.throwException(
        AccessResult.create(AccessResultCode.PU_PROBLEM, ""));
      });
  }

  @Test
  public void throwAuthoritiesException() {
    assertThrows(AuthoritiesException.class, () -> {
    accessResultExceptionHelper.throwException(
        AccessResult.create(AccessResultCode.AUTHORIZATION_VALIDATION, ""));
      });
  }

  @Test
  public void throwAuthorizationSekretessException() {
    assertThrows(WebCertServiceException.class, () -> {
    accessResultExceptionHelper.throwException(
        AccessResult.create(AccessResultCode.AUTHORIZATION_SEKRETESS, ""));
      });
  }

  @Test
  public void throwAuthorizationSekretessUnit() {
    assertThrows(WebCertServiceException.class, () -> {
    accessResultExceptionHelper.throwException(
        AccessResult.create(AccessResultCode.AUTHORIZATION_SEKRETESS_UNIT, ""));
      });
  }

  @Test
  public void throwAnyOtherException() {
    assertThrows(WebCertServiceException.class, () -> {
    accessResultExceptionHelper.throwException(
        AccessResult.create(AccessResultCode.RENEW_FALSE, ""));
      });
  }

  @Test
  public void throwIfAccessDenied() {
    assertThrows(WebCertServiceException.class, () -> {
    accessResultExceptionHelper.throwExceptionIfDenied(
        AccessResult.create(AccessResultCode.RENEW_FALSE, ""));
      });
  }

  @Test
  public void dontThrowIfAccessNoProblem() {
    accessResultExceptionHelper.throwExceptionIfDenied(
        AccessResult.create(AccessResultCode.NO_PROBLEM, ""));
    assertTrue(true);
  }
}
