/*
 * Copyright (C) 2023 Inera AB (http://www.inera.se)
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

import org.springframework.stereotype.Component;
import se.inera.intyg.infra.security.authorities.AuthoritiesException;
import se.inera.intyg.webcert.common.service.exception.WebCertServiceErrorCodeEnum;
import se.inera.intyg.webcert.common.service.exception.WebCertServiceException;
import se.inera.intyg.webcert.web.service.access.AccessResult;
import se.inera.intyg.webcert.web.service.access.AccessResultCode;

/**
 * Implementation of AccessResultExceptionHelper.
 */
@Component
public class AccessResultExceptionHelperImpl implements AccessResultExceptionHelper {

    @Override
    public void throwException(AccessResult accessResult) {
        if (accessResult.getCode() == AccessResultCode.PU_PROBLEM) {
            throw new WebCertServiceException(WebCertServiceErrorCodeEnum.PU_PROBLEM, accessResult.getMessage());
        }

        if (accessResult.getCode() == AccessResultCode.AUTHORIZATION_VALIDATION) {
            throw new AuthoritiesException(accessResult.getMessage());
        }

        if (accessResult.getCode() == AccessResultCode.AUTHORIZATION_SEKRETESS) {
            throw new WebCertServiceException(WebCertServiceErrorCodeEnum.AUTHORIZATION_PROBLEM_SEKRETESSMARKERING,
                accessResult.getMessage());
        }

        if (accessResult.getCode() == AccessResultCode.AUTHORIZATION_SEKRETESS_UNIT) {
            throw new WebCertServiceException(WebCertServiceErrorCodeEnum.AUTHORIZATION_PROBLEM_SEKRETESSMARKERING_ENHET,
                accessResult.getMessage());
        }

        throw new WebCertServiceException(WebCertServiceErrorCodeEnum.AUTHORIZATION_PROBLEM, accessResult.getMessage());
    }

    @Override
    public void throwExceptionIfDenied(AccessResult accessResult) {
        if (accessResult.isDenied()) {
            throwException(accessResult);
        }
    }
}
