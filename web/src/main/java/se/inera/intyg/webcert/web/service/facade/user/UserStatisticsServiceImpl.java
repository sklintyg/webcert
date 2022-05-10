/*
 * Copyright (C) 2022 Inera AB (http://www.inera.se)
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
package se.inera.intyg.webcert.web.service.facade.user;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import se.inera.intyg.infra.security.common.model.UserOriginType;
import se.inera.intyg.webcert.web.service.user.WebCertUserService;
import se.inera.intyg.webcert.web.service.user.dto.WebCertUser;
import se.inera.intyg.webcert.web.service.utkast.UtkastService;

import java.util.Map;

@Service
public class UserStatisticsServiceImpl implements UserStatisticsService {

    private static final Logger LOG = LoggerFactory.getLogger(UserStatisticsServiceImpl.class);

    private final WebCertUserService webCertUserService;
    private final UtkastService utkastService;

    @Autowired
    public UserStatisticsServiceImpl(WebCertUserService webCertUserService, UtkastService utkastService) {
        this.webCertUserService = webCertUserService;
        this.utkastService = utkastService;
    }

    @Override
    public long getNumberOfDraftsOnSelectedUnit() {
        final var user = webCertUserService.getUser();
        if (validateUser(user)) {
            return getStatistics(user);
        }
        return 0L;
    }

    private boolean validateUser(WebCertUser user) {
        if (user == null) {
            LOG.warn("getStatistics was called, but webcertUser was null!");
            return false;
        } else if (UserOriginType.DJUPINTEGRATION.name().equals(user.getOrigin())) {
            LOG.debug("getStatistics was called, but webcertUser origin is DJUPINTEGRATION - returning empty answer");
            return false;
        }
        return true;
    }

    private long getStatistics(WebCertUser user) {
        final var units = user.getIdsOfAllVardenheter();
        if (units == null || units.isEmpty()) {
            LOG.warn("getStatistics was called by user {} that have no id:s of vardenheter present in the user context: {}",
                    user.getHsaId(), user.getAsJson());
            return 0L;
        }

        final var statistics = utkastService.getNbrOfUnsignedDraftsByCareUnits(units);
        return getFromMap(user.getValdVardenhet().getId(), statistics);
    }

    private long getFromMap(String id, Map<String, Long> statsMap) {
        final var value = statsMap.get(id);
        return value != null ? value : 0L;
    }

}