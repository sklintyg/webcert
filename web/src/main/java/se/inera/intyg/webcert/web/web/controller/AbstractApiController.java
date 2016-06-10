/*
 * Copyright (C) 2016 Inera AB (http://www.inera.se)
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

package se.inera.intyg.webcert.web.web.controller;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import se.inera.intyg.common.security.authorities.validation.AuthoritiesValidator;
import se.inera.intyg.common.support.model.common.internal.HoSPersonal;
import se.inera.intyg.webcert.web.converter.util.IntygConverterUtil;
import se.inera.intyg.webcert.web.service.user.WebCertUserService;
import se.inera.intyg.webcert.web.service.user.dto.WebCertUser;

public abstract class AbstractApiController {

    private static final Logger LOG = LoggerFactory.getLogger(AbstractApiController.class);

    protected static final String UTF_8 = "UTF-8";

    protected static final String UTF_8_CHARSET = ";charset=utf-8";

    protected AuthoritiesValidator authoritiesValidator = new AuthoritiesValidator();

    @Autowired
    private WebCertUserService webCertUserService;

    protected HoSPersonal createHoSPersonFromUser() {
        WebCertUser user = webCertUserService.getUser();
        return IntygConverterUtil.buildHosPersonalFromWebCertUser(user, null);
    }

    protected List<String> getEnhetIdsForCurrentUser() {

        WebCertUser webCertUser = webCertUserService.getUser();
        List<String> vardenheterIds = webCertUser.getIdsOfSelectedVardenhet();

        LOG.debug("Current user '{}' has assignments: {}", webCertUser.getHsaId(), vardenheterIds);

        return vardenheterIds;
    }

    public WebCertUserService getWebCertUserService() {
        return webCertUserService;
    }

}
