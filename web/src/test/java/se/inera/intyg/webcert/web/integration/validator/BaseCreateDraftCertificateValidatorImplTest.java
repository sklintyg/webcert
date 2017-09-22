/*
 * Copyright (C) 2017 Inera AB (http://www.inera.se)
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
package se.inera.intyg.webcert.web.integration.validator;

import com.google.common.collect.ImmutableSet;
import org.junit.Before;
import org.mockito.Mock;
import se.inera.intyg.common.support.modules.registry.IntygModuleRegistry;
import se.inera.intyg.infra.security.authorities.CommonAuthoritiesResolver;
import se.inera.intyg.infra.security.common.model.AuthoritiesConstants;
import se.inera.intyg.infra.security.common.model.Privilege;
import se.inera.intyg.infra.security.common.model.RequestOrigin;
import se.inera.intyg.webcert.web.auth.WebcertUserDetailsService;
import se.inera.intyg.webcert.web.security.WebCertUserOriginType;
import se.inera.intyg.webcert.web.service.feature.WebcertFeature;
import se.inera.intyg.webcert.web.service.patient.PatientDetailsResolver;
import se.inera.intyg.webcert.web.service.user.dto.WebCertUser;

import java.util.Arrays;
import java.util.HashMap;

import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.when;

/**
 * Created by eriklupander on 2017-09-19.
 */
public abstract class BaseCreateDraftCertificateValidatorImplTest {

    protected static final String FK7263 = "fk7263";
    protected static final String TSBAS = "ts-bas";

    @Mock
    protected IntygModuleRegistry moduleRegistry;

    @Mock
    protected CommonAuthoritiesResolver commonAuthoritiesResolver;

    @Mock
    protected PatientDetailsResolver patientDetailsResolver;

    @Mock
    protected WebcertUserDetailsService webcertUserDetailsService;

    @Before
    public void setup() {
        when(moduleRegistry.getModuleIdFromExternalId(anyString()))
                .thenAnswer(invocation -> ((String) invocation.getArguments()[0]).toLowerCase());
        when(moduleRegistry.moduleExists(FK7263)).thenReturn(true);
        when(moduleRegistry.moduleExists(TSBAS)).thenReturn(true);
        when(commonAuthoritiesResolver.getSekretessmarkeringAllowed())
                .thenReturn(Arrays.asList("fk7263", "lisjp", "luse", "luae_na", "luae_fs", "db", "doi"));
        when(webcertUserDetailsService.loadUserByHsaId(anyString())).thenReturn(buildUser());
    }

    protected WebCertUser buildUser() {
        WebCertUser user = new WebCertUser();
        user.setAuthorities(new HashMap<>());

        user.getAuthorities().put(AuthoritiesConstants.PRIVILEGE_HANTERA_SEKRETESSMARKERAD_PATIENT,
                createPrivilege(AuthoritiesConstants.PRIVILEGE_HANTERA_SEKRETESSMARKERAD_PATIENT));
        user.getAuthorities().put(AuthoritiesConstants.PRIVILEGE_SKRIVA_INTYG,
                createPrivilege(AuthoritiesConstants.PRIVILEGE_SKRIVA_INTYG));
        user.setFeatures(ImmutableSet
                .of(WebcertFeature.HANTERA_INTYGSUTKAST.getName(), WebcertFeature.HANTERA_INTYGSUTKAST.getName() + "." + FK7263,
                        WebcertFeature.HANTERA_INTYGSUTKAST.getName() + "." + TSBAS));
        user.setOrigin(WebCertUserOriginType.DJUPINTEGRATION.name());
        return user;
    }

    protected WebCertUser buildUserUnauthorized() {
        WebCertUser user = new WebCertUser();
        user.setAuthorities(new HashMap<>());

        user.getAuthorities().put(AuthoritiesConstants.PRIVILEGE_HANTERA_SEKRETESSMARKERAD_PATIENT,
                createPrivilege(AuthoritiesConstants.PRIVILEGE_HANTERA_SEKRETESSMARKERAD_PATIENT));
        user.setFeatures(ImmutableSet
                .of(WebcertFeature.HANTERA_INTYGSUTKAST.getName(), WebcertFeature.HANTERA_INTYGSUTKAST.getName() + "." + FK7263,
                        WebcertFeature.HANTERA_INTYGSUTKAST.getName() + "." + TSBAS));
        user.setOrigin(WebCertUserOriginType.DJUPINTEGRATION.name());
        return user;
    }


    protected Privilege createPrivilege(String privilege) {
        Privilege priv = new Privilege();
        priv.setName(privilege);
        RequestOrigin requestOrigin = new RequestOrigin();
        requestOrigin.setName(WebCertUserOriginType.DJUPINTEGRATION.name());
        requestOrigin.setIntygstyper(Arrays.asList(FK7263, TSBAS));
        priv.setRequestOrigins(Arrays.asList(requestOrigin));
        priv.setIntygstyper(Arrays.asList(FK7263, TSBAS));
        return priv;
    }
}
