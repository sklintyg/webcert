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
import se.inera.intyg.common.doi.support.DoiModuleEntryPoint;
import se.inera.intyg.common.fk7263.support.Fk7263EntryPoint;
import se.inera.intyg.common.lisjp.support.LisjpEntryPoint;
import se.inera.intyg.common.support.model.common.internal.Vardenhet;
import se.inera.intyg.infra.security.common.model.AuthoritiesConstants;
import se.inera.intyg.schemas.contract.Personnummer;
import se.inera.intyg.webcert.web.service.patient.PatientDetailsResolver;
import se.inera.intyg.webcert.web.service.user.WebCertUserService;
import se.inera.intyg.webcert.web.service.user.dto.WebCertUser;
import se.inera.intyg.webcert.web.service.utkast.UtkastService;

@Service
public class CertificateAccessServiceImpl extends AccessServiceImpl implements CertificateAccessService {

    @Autowired
    public CertificateAccessServiceImpl(final WebCertUserService webCertUserService,
            final PatientDetailsResolver patientDetailsResolver,
            final UtkastService utkastService) {
        super(webCertUserService, patientDetailsResolver, utkastService);
    }

    @Override
    public AccessResult allowToRead(String intygsTyp, Vardenhet vardenhet, Personnummer personnummer) {
        final WebCertUser user = getUser();

        Optional<AccessResult> accessResult = isAuthorized(intygsTyp, user, AuthoritiesConstants.FEATURE_HANTERA_INTYGSUTKAST,
                AuthoritiesConstants.PRIVILEGE_SKRIVA_INTYG);

        if (!accessResult.isPresent()) {
            accessResult = isSekretessRuleValid(intygsTyp, vardenhet.getEnhetsid(), user, personnummer);
        }

        if (!accessResult.isPresent()) {
            accessResult = isSJFRuleValid(intygsTyp, vardenhet, user, true);
        }

        return accessResult.isPresent() ? accessResult.get() : AccessResult.noProblem();
    }

    @Override
    public AccessResult allowToReplace(String intygsTyp, Vardenhet vardenhet, Personnummer personnummer) {
        final WebCertUser user = getUser();

        Optional<AccessResult> accessResult = isAuthorized(intygsTyp, user, null,
                AuthoritiesConstants.PRIVILEGE_ERSATTA_INTYG);

        if (!accessResult.isPresent()) {
            accessResult = isDeceasedRuleValid(user, intygsTyp, null, personnummer, Collections.emptyList());
        }

        if (!accessResult.isPresent()) {
            accessResult = isInactiveUnitRuleValidCreate(user);
        }

        if (!accessResult.isPresent()) {
            final List<String> excludeIntygsTyper = Arrays.asList(new String[] { DbModuleEntryPoint.MODULE_ID,
                    DoiModuleEntryPoint.MODULE_ID });
            accessResult = isRenewRuleValid(user, intygsTyp, vardenhet.getEnhetsid(), excludeIntygsTyper);
        }

        if (!accessResult.isPresent()) {
            accessResult = isSekretessRuleValid(intygsTyp, vardenhet.getEnhetsid(), user, personnummer);
        }

        if (!accessResult.isPresent() && isUserLoggedInOnDifferentUnit(vardenhet.getEnhetsid())
                && !(DbModuleEntryPoint.MODULE_ID.equalsIgnoreCase(intygsTyp)
                        || DoiModuleEntryPoint.MODULE_ID.equalsIgnoreCase(intygsTyp))) {
            accessResult = Optional
                    .of(AccessResult.create(AccessResultCode.AUTHORIZATION_DIFFERENT_UNIT,
                            "User is logged in on a different unit than the draft/certificate"));
        }

        return accessResult.isPresent() ? accessResult.get() : AccessResult.noProblem();
    }

    @Override
    public AccessResult allowToRenew(String intygsTyp, Vardenhet vardenhet, Personnummer personnummer) {
        return allowToRenew(intygsTyp, vardenhet, personnummer, false);
    }

    @Override
    public AccessResult allowToRenew(String intygsTyp, Vardenhet vardenhet, Personnummer personnummer, boolean isComplement) {
        final WebCertUser user = getUser();

        String privilegie = AuthoritiesConstants.PRIVILEGE_FORNYA_INTYG;
        if (isComplement) {
            privilegie = AuthoritiesConstants.PRIVILEGE_SVARA_MED_NYTT_INTYG;
        }

        Optional<AccessResult> accessResult = isAuthorized(intygsTyp, user, AuthoritiesConstants.FEATURE_FORNYA_INTYG, privilegie);

        if (!accessResult.isPresent()) {
            final List<String> invalidTypes = Arrays.asList(new String[] { DbModuleEntryPoint.MODULE_ID });
            accessResult = isDeceasedRuleValid(user, intygsTyp, null, personnummer, invalidTypes);
        }

        if (!accessResult.isPresent()) {
            accessResult = isInactiveUnitRuleValidCreate(user);
        }

        if (!accessResult.isPresent()) {
            accessResult = isRenewRuleValid(user, intygsTyp, vardenhet.getEnhetsid(), Collections.emptyList(), true);
        }

        if (!accessResult.isPresent()) {
            accessResult = isSekretessRuleValid(intygsTyp, vardenhet.getEnhetsid(), user, personnummer);
        }

        if (!accessResult.isPresent()) {
            accessResult = isUniqueUtkastRuleValid(intygsTyp, user, personnummer);
        }

        if (!accessResult.isPresent()) {
            accessResult = isSJFRuleValid(intygsTyp, vardenhet, user, true);
        }

        return accessResult.isPresent() ? accessResult.get() : AccessResult.noProblem();
    }

    @Override
    public AccessResult allowToPrint(String intygsTyp, Vardenhet vardenhet, Personnummer personnummer, boolean isEmployer) {
        final WebCertUser user = getUser();

        String feature = AuthoritiesConstants.FEATURE_UTSKRIFT;
        if (isEmployer) {
            feature = AuthoritiesConstants.FEATURE_ARBETSGIVARUTSKRIFT;
        }

        Optional<AccessResult> accessResult = isAuthorized(intygsTyp, user, feature, AuthoritiesConstants.PRIVILEGE_VISA_INTYG);

        if (!accessResult.isPresent()) {
            accessResult = isDeceasedRuleValid(user, intygsTyp, vardenhet.getEnhetsid(), personnummer, Collections.emptyList());
        }

        if (!accessResult.isPresent()) {
            accessResult = isInactiveUnitRuleValid(user, intygsTyp, vardenhet.getEnhetsid());
        }

        if (!accessResult.isPresent()) {
            accessResult = isRenewRuleValid(user, intygsTyp, vardenhet.getEnhetsid(), Collections.emptyList());
        }

        if (!accessResult.isPresent()) {
            accessResult = isSekretessRuleValid(intygsTyp, vardenhet.getEnhetsid(), user, personnummer);
        }

        if (!accessResult.isPresent()) {
            accessResult = isUnitRuleValid(intygsTyp, vardenhet, user, true, true);
        }

        return accessResult.isPresent() ? accessResult.get() : AccessResult.noProblem();
    }

    @Override
    public AccessResult allowToInvalidate(String intygsTyp, Vardenhet vardenhet, Personnummer personnummer) {
        final WebCertUser user = getUser();

        Optional<AccessResult> accessResult = isAuthorized(intygsTyp, user, AuthoritiesConstants.FEATURE_MAKULERA_INTYG,
                AuthoritiesConstants.PRIVILEGE_MAKULERA_INTYG);

        if (!accessResult.isPresent()) {
            accessResult = isDeceasedRuleValid(user, intygsTyp, vardenhet.getEnhetsid(), personnummer, Collections.emptyList());
        }

        if (!accessResult.isPresent()) {
            accessResult = isInactiveUnitRuleValid(user, intygsTyp, vardenhet.getEnhetsid());
        }

        if (!accessResult.isPresent()) {
            accessResult = isRenewRuleValid(user, intygsTyp, vardenhet.getEnhetsid(), Collections.emptyList());
        }

        if (!accessResult.isPresent()) {
            accessResult = isSekretessRuleValid(intygsTyp, vardenhet.getEnhetsid(), user, personnummer);
        }

        if (!accessResult.isPresent()) {
            accessResult = isUnitRuleValid(intygsTyp, vardenhet, user, false, true);
        }

        return accessResult.isPresent() ? accessResult.get() : AccessResult.noProblem();
    }

    @Override
    public AccessResult allowToSend(String intygsTyp, Vardenhet vardenhet, Personnummer personnummer) {
        final WebCertUser user = getUser();

        Optional<AccessResult> accessResult = isAuthorized(intygsTyp, user, AuthoritiesConstants.FEATURE_SKICKA_INTYG, null);

        if (!accessResult.isPresent()) {
            final List<String> invalidTypes = Arrays.asList(new String[] { DbModuleEntryPoint.MODULE_ID });
            accessResult = isDeceasedRuleValid(user, intygsTyp, vardenhet.getEnhetsid(), personnummer, invalidTypes);
        }

        if (!accessResult.isPresent()) {
            accessResult = isInactiveUnitRuleValid(user, intygsTyp, vardenhet.getEnhetsid());
        }

        if (!accessResult.isPresent()) {

            accessResult = isRenewRuleValid(user, intygsTyp, vardenhet.getEnhetsid(), Collections.emptyList());
        }

        if (!accessResult.isPresent()) {
            accessResult = isSekretessRuleValid(intygsTyp, vardenhet.getEnhetsid(), user, personnummer);
        }

        if (!accessResult.isPresent() && isUserLoggedInOnDifferentUnit(vardenhet.getEnhetsid())) {
            accessResult = Optional
                    .of(AccessResult.create(AccessResultCode.AUTHORIZATION_DIFFERENT_UNIT,
                            "User is logged in on a different unit than the draft/certificate"));
        }

        return accessResult.isPresent() ? accessResult.get() : AccessResult.noProblem();
    }

    @Override
    public AccessResult allowToCreateQuestion(String intygsTyp, Vardenhet vardenhet, Personnummer personnummer) {
        final WebCertUser user = getUser();

        Optional<AccessResult> accessResult = isAuthorized(intygsTyp, user, AuthoritiesConstants.FEATURE_HANTERA_FRAGOR,
                AuthoritiesConstants.PRIVILEGE_SKAPA_NYFRAGA);

        if (!accessResult.isPresent()) {
            accessResult = isDeceasedRuleValid(user, intygsTyp, vardenhet.getEnhetsid(), personnummer, Collections.emptyList());
        }

        if (!accessResult.isPresent()) {
            accessResult = isInactiveUnitRuleValid(user, intygsTyp, vardenhet.getEnhetsid());
        }

        if (!accessResult.isPresent()) {
            accessResult = isRenewRuleValid(user, intygsTyp, vardenhet.getEnhetsid(), Collections.emptyList());
        }

        if (!accessResult.isPresent()) {
            accessResult = isSekretessRuleValid(intygsTyp, vardenhet.getEnhetsid(), user, personnummer);
        }

        if (!accessResult.isPresent()) {
            accessResult = isSJFRuleValid(intygsTyp, vardenhet, user, false);
        }

        return accessResult.isPresent() ? accessResult.get() : AccessResult.noProblem();
    }

    @Override
    public AccessResult allowToAnswerComplementQuestion(String intygsTyp, Vardenhet vardenhet, Personnummer personnummer,
            boolean newCertificate) {
        final WebCertUser user = getUser();

        String privilegie = null;
        if (newCertificate) {
            privilegie = AuthoritiesConstants.PRIVILEGE_SVARA_MED_NYTT_INTYG;
        } else {
            privilegie = AuthoritiesConstants.PRIVILEGE_BESVARA_KOMPLETTERINGSFRAGA;
        }

        Optional<AccessResult> accessResult = isAuthorized(intygsTyp, user, AuthoritiesConstants.FEATURE_HANTERA_FRAGOR, privilegie);

        if (!accessResult.isPresent()) {
            accessResult = isDeceasedRuleValid(user, intygsTyp, vardenhet.getEnhetsid(), personnummer, Collections.emptyList());
        }

        if (!accessResult.isPresent()) {
            accessResult = isInactiveUnitRuleValid(user, intygsTyp, vardenhet.getEnhetsid());
        }

        if (!accessResult.isPresent()) {
            accessResult = isRenewRuleValid(user, intygsTyp, vardenhet.getEnhetsid(), Collections.emptyList());
        }

        if (!accessResult.isPresent()) {
            accessResult = isSekretessRuleValid(intygsTyp, vardenhet.getEnhetsid(), user, personnummer);
        }

        if (!accessResult.isPresent()) {
            accessResult = isSJFRuleValid(intygsTyp, vardenhet, user, false);
        }

        return accessResult.isPresent() ? accessResult.get() : AccessResult.noProblem();
    }

    @Override
    public AccessResult allowToAnswerAdminQuestion(String intygsTyp, Vardenhet vardenhet, Personnummer personnummer) {
        return null;
    }

    @Override
    public AccessResult allowToReadQuestions(String intygsTyp, Vardenhet vardenhet, Personnummer personnummer) {
        final WebCertUser user = getUser();

        Optional<AccessResult> accessResult = isAuthorized(intygsTyp, user, AuthoritiesConstants.FEATURE_HANTERA_FRAGOR, null);

        if (!accessResult.isPresent()) {
            final List<String> excludeTypes = Arrays.asList(new String[] { LisjpEntryPoint.MODULE_ID, Fk7263EntryPoint.MODULE_ID });
            accessResult = isDeceasedRuleValid(user, intygsTyp, vardenhet.getEnhetsid(), personnummer, Collections.emptyList(),
                    excludeTypes);
        }

        if (!accessResult.isPresent()) {
            final List<String> excludeTypes = Arrays.asList(new String[] { LisjpEntryPoint.MODULE_ID, Fk7263EntryPoint.MODULE_ID });
            accessResult = isInactiveUnitRuleValid(user, intygsTyp, vardenhet.getEnhetsid(), excludeTypes);
        }

        if (!accessResult.isPresent()) {
            final List<String> excludeTypes = Arrays.asList(new String[] { LisjpEntryPoint.MODULE_ID, Fk7263EntryPoint.MODULE_ID });
            accessResult = isRenewRuleValid(user, intygsTyp, vardenhet.getEnhetsid(), excludeTypes);
        }

        if (!accessResult.isPresent()) {
            accessResult = isSekretessRuleValid(intygsTyp, vardenhet.getEnhetsid(), user, personnummer);
        }

        if (!accessResult.isPresent()) {
            accessResult = isSJFRuleValid(intygsTyp, vardenhet, user, true);
        }

        return accessResult.isPresent() ? accessResult.get() : AccessResult.noProblem();
    }

    @Override
    public AccessResult allowToForwardQuestions(String intygsTyp, Vardenhet vardenhet, Personnummer personnummer) {
        final WebCertUser user = getUser();
        Optional<AccessResult> accessResult = isAuthorized(intygsTyp, user, AuthoritiesConstants.FEATURE_HANTERA_FRAGOR,
                AuthoritiesConstants.PRIVILEGE_VIDAREBEFORDRA_FRAGASVAR);

        if (!accessResult.isPresent()) {
            accessResult = isDeceasedRuleValid(user, intygsTyp, vardenhet.getEnhetsid(), personnummer, Collections.emptyList());
        }

        if (!accessResult.isPresent()) {
            accessResult = isInactiveUnitRuleValid(user, intygsTyp, vardenhet.getEnhetsid());
        }

        if (!accessResult.isPresent()) {
            accessResult = isRenewRuleValid(user, intygsTyp, vardenhet.getEnhetsid(), Collections.emptyList());
        }

        if (!accessResult.isPresent()) {
            accessResult = isSekretessRuleValid(intygsTyp, vardenhet.getEnhetsid(), user, personnummer);
        }

        if (!accessResult.isPresent() && isUserLoggedInOnDifferentUnit(vardenhet.getEnhetsid())) {
            accessResult = Optional
                    .of(AccessResult.create(AccessResultCode.AUTHORIZATION_DIFFERENT_UNIT,
                            "User is logged in on a different unit than the draft/certificate"));
        }

        return accessResult.isPresent() ? accessResult.get() : AccessResult.noProblem();
    }
}
