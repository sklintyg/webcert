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
package se.inera.intyg.webcert.web.service.facade.impl;

import java.util.Arrays;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import se.inera.intyg.common.support.facade.model.user.User;
import se.inera.intyg.infra.security.authorities.CommonAuthoritiesResolver;
import se.inera.intyg.webcert.web.service.facade.ChangeUnitService;
import se.inera.intyg.webcert.web.service.facade.user.UserService;
import se.inera.intyg.webcert.web.service.underskrift.dss.DssSignatureService;
import se.inera.intyg.webcert.web.service.user.WebCertUserService;
import se.inera.intyg.webcert.web.service.user.dto.WebCertUser;

@Service
public class ChangeUnitServiceImpl implements ChangeUnitService {

    private final WebCertUserService webCertUserService;
    private final DssSignatureService dssSignatureService;
    private final CommonAuthoritiesResolver commonAuthoritiesResolver;
    private final UserService userService;

    private static final Logger LOG = LoggerFactory.getLogger(ChangeUnitServiceImpl.class);


    @Autowired
    public ChangeUnitServiceImpl(WebCertUserService webCertUserService,
        DssSignatureService dssSignatureService,
        CommonAuthoritiesResolver commonAuthoritiesResolver,
        UserService userService) {
        this.webCertUserService = webCertUserService;
        this.dssSignatureService = dssSignatureService;
        this.commonAuthoritiesResolver = commonAuthoritiesResolver;
        this.userService = userService;
    }

    @Override
    public User change(String unitId) throws ChangeUnitException {
        final var user = webCertUserService.getUser();

        LOG.debug("Attempting to change selected unit for user '{}', currently selected unit is '{}'", user.getHsaId(),
            user.getValdVardenhet() != null ? user.getValdVardenhet().getId() : "(none)");

        changeUnit(unitId, user);
        updateSigningService(unitId, user);
        setFeaturesForUser(user);

        LOG.debug("Selected vardenhet is now '{}'", user.getValdVardenhet().getId());

        return userService.getLoggedInUser();
    }

    private void setFeaturesForUser(WebCertUser user) {
        user.setFeatures(commonAuthoritiesResolver.getFeatures(
            Arrays.asList(user.getValdVardenhet().getId(), user.getValdVardgivare().getId())));
    }

    private void updateSigningService(String unitId, WebCertUser user) {
        final var chosenUnit = user.getValdVardenhet();
        if (chosenUnit != null) {
            user.setUseSigningService(dssSignatureService.shouldUseSigningService(unitId));
        }
    }

    private void changeUnit(String unitId, WebCertUser user) throws ChangeUnitException {
        final var changeSuccess = user.changeValdVardenhet(unitId);

        if (!changeSuccess) {
            LOG.error("Unit '{}' is not present in the MIUs for user '{}'", unitId, user.getHsaId());
            throw new ChangeUnitException("Unit change failed");
        }
    }
}
