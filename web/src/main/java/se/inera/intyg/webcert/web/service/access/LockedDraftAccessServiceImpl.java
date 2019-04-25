/*
 * Copyright (C) 2019 Inera AB (http://www.inera.se)
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

package se.inera.intyg.webcert.web.service.access;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import se.inera.intyg.common.db.support.DbModuleEntryPoint;
import se.inera.intyg.infra.security.common.model.AuthoritiesConstants;
import se.inera.intyg.schemas.contract.Personnummer;
import se.inera.intyg.webcert.web.service.patient.PatientDetailsResolver;
import se.inera.intyg.webcert.web.service.user.WebCertUserService;
import se.inera.intyg.webcert.web.service.user.dto.WebCertUser;
import se.inera.intyg.webcert.web.service.utkast.UtkastService;

@Service
public class LockedDraftAccessServiceImpl extends AccessServiceImpl implements LockedDraftAccessService {

    @Autowired
    public LockedDraftAccessServiceImpl(final WebCertUserService webCertUserService,
            final PatientDetailsResolver patientDetailsResolver,
            final UtkastService utkastService) {
        super(webCertUserService, patientDetailsResolver, utkastService);
    }

    @Override
    public AccessResult allowToRead(String intygsTyp, String enhetsId, Personnummer personnummer) {
        final WebCertUser user = getUser();

        Optional<AccessResult> accessResult = isAuthorized(intygsTyp, user, AuthoritiesConstants.FEATURE_HANTERA_INTYGSUTKAST,
                AuthoritiesConstants.PRIVILEGE_SKRIVA_INTYG);

        if (!accessResult.isPresent()) {
            accessResult = isSekretessRuleValid(intygsTyp, enhetsId, user, personnummer);
        }

        return accessResult.isPresent() ? accessResult.get() : AccessResult.noProblem();
    }

    @Override
    public AccessResult allowedToCopyLockedUtkast(String intygsTyp, String enhetsId, Personnummer personnummer) {
        final WebCertUser user = getUser();

        Optional<AccessResult> accessResult = isAuthorized(intygsTyp, user, AuthoritiesConstants.FEATURE_HANTERA_INTYGSUTKAST,
                AuthoritiesConstants.PRIVILEGE_SKRIVA_INTYG);

        if (!accessResult.isPresent()) {
            final List<String> invalidTypes = Arrays.asList(new String[] { DbModuleEntryPoint.MODULE_ID });
            accessResult = isDeceasedRuleValid(user, intygsTyp, null, personnummer, invalidTypes);
        }

        if (!accessResult.isPresent()) {
            accessResult = isInactiveUnitRuleValidCreate(user);
        }

        if (!accessResult.isPresent()) {
            final List<String> invalidTypes = Arrays.asList(new String[] { "ALL" });
            accessResult = isRenewRuleValid(user, intygsTyp, enhetsId, invalidTypes);
        }

        if (!accessResult.isPresent()) {
            accessResult = isSekretessRuleValid(intygsTyp, enhetsId, user, personnummer);
        }

        if (!accessResult.isPresent()) {
            accessResult = isUniqueUtkastRuleValid(intygsTyp, user, personnummer);
        }

        return accessResult.isPresent() ? accessResult.get() : AccessResult.noProblem();
    }

    @Override
    public AccessResult allowedToInvalidateLockedUtkast(String intygsTyp, String enhetsId, Personnummer personnummer) {
        final WebCertUser user = getUser();

        Optional<AccessResult> accessResult = isAuthorized(intygsTyp, user, AuthoritiesConstants.FEATURE_MAKULERA_INTYG,
                AuthoritiesConstants.PRIVILEGE_MAKULERA_INTYG);

        if (!accessResult.isPresent()) {
            accessResult = isDeceasedRuleValid(user, intygsTyp, enhetsId, personnummer, Collections.emptyList());
        }

        if (!accessResult.isPresent()) {
            accessResult = isInactiveUnitRuleValid(user, enhetsId);
        }

        if (!accessResult.isPresent()) {
            accessResult = isRenewRuleValid(user, intygsTyp, enhetsId, Collections.emptyList());
        }

        if (!accessResult.isPresent()) {
            accessResult = isSekretessRuleValid(intygsTyp, enhetsId, user, personnummer);
        }

        if (!accessResult.isPresent() && isUserLoggedInOnDifferentUnit(enhetsId)) {
            accessResult = Optional
                    .of(AccessResult.create(AccessResultCode.AUTHORIZATION_DIFFERENT_UNIT,
                            "User is logged in on a different unit than the draft/certificate"));
        }

        return accessResult.isPresent() ? accessResult.get() : AccessResult.noProblem();
    }

    @Override
    public AccessResult allowToPrint(String intygsTyp, String enhetsId, Personnummer personnummer) {
        final WebCertUser user = getUser();

        Optional<AccessResult> accessResult = isAuthorized(intygsTyp, user, AuthoritiesConstants.FEATURE_UTSKRIFT,
                AuthoritiesConstants.PRIVILEGE_VISA_INTYG);

        if (!accessResult.isPresent()) {
            final List<String> invalidTypes = Arrays.asList(new String[] { DbModuleEntryPoint.MODULE_ID });
            accessResult = isDeceasedRuleValid(user, intygsTyp, enhetsId, personnummer, invalidTypes);
        }

        if (!accessResult.isPresent()) {
            accessResult = isInactiveUnitRuleValid(user, enhetsId);
        }

        if (!accessResult.isPresent()) {

            accessResult = isRenewRuleValid(user, intygsTyp, enhetsId, Collections.emptyList());
        }

        if (!accessResult.isPresent()) {
            accessResult = isSekretessRuleValid(intygsTyp, enhetsId, user, personnummer);
        }

        if (!accessResult.isPresent() && isUserLoggedInOnDifferentUnit(enhetsId)) {
            accessResult = Optional
                    .of(AccessResult.create(AccessResultCode.AUTHORIZATION_DIFFERENT_UNIT,
                            "User is logged in on a different unit than the draft/certificate"));
        }

        return accessResult.isPresent() ? accessResult.get() : AccessResult.noProblem();
    }
}
