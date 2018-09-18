/*
 * Copyright (C) 2018 Inera AB (http://www.inera.se)
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

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.http.ResponseEntity;
import se.inera.intyg.infra.integration.hsa.model.Vardenhet;
import se.inera.intyg.infra.integration.hsa.model.Vardgivare;
import se.inera.intyg.infra.security.authorities.AuthoritiesResolverUtil;
import se.inera.intyg.infra.security.common.model.AuthoritiesConstants;
import se.inera.intyg.infra.security.common.model.Feature;
import se.inera.intyg.infra.security.common.model.Privilege;
import se.inera.intyg.infra.security.common.model.Role;
import se.inera.intyg.webcert.persistence.utkast.repository.UtkastRepository;
import se.inera.intyg.webcert.web.auth.bootstrap.AuthoritiesConfigurationTestSetup;
import se.inera.intyg.webcert.web.service.intyg.IntygService;
import se.inera.intyg.webcert.web.service.maillink.MailLinkService;
import se.inera.intyg.webcert.web.service.user.WebCertUserService;
import se.inera.intyg.webcert.web.service.user.dto.WebCertUser;
import se.inera.intyg.webcert.web.web.controller.api.dto.IntygTypeInfo;

import java.net.URI;
import java.util.Collections;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class PageControllerTest extends AuthoritiesConfigurationTestSetup {

    private static final String INTYG_ID = "intyg-123";
    private static final String INTYG_TYPE_VERSION = "1.0";
    private static final String INTYG_TYP_FK7263 = "fk7263";

    @Mock
    private WebCertUserService webCertUserService;
    @Mock
    private IntygService intygService;
    @Mock
    private UtkastRepository utkastRepository;
    @Mock
    private MailLinkService mailLinkService;

    @InjectMocks
    private PageController controller;

    private IntygTypeInfo intygTypeInfo;

    @Before
    public void setup() throws Exception {
        CONFIGURATION_LOADER.afterPropertiesSet();
        intygTypeInfo = new IntygTypeInfo(INTYG_ID,INTYG_TYP_FK7263,INTYG_TYPE_VERSION);
    }

    @Test
    public void testRedirectToIntygUserHasAccess() {
        when(webCertUserService.getUser()).thenReturn(createMockUser(false));
        when(intygService.getIssuingVardenhetHsaId(INTYG_ID, INTYG_TYP_FK7263)).thenReturn("ve-1");
        when(utkastRepository.findOne(INTYG_ID)).thenReturn(null);
        when(intygService.getIntygTypeInfo(INTYG_ID, null)).thenReturn(intygTypeInfo);
        when(mailLinkService.intygRedirect(INTYG_TYP_FK7263, INTYG_TYPE_VERSION, INTYG_ID)).thenReturn(buildMockURI());
        ResponseEntity<Object> result = controller.redirectToIntyg(INTYG_ID, INTYG_TYP_FK7263);
        assertEquals(303, result.getStatusCode().value());
    }

    @Test
    public void testRedirectToIntygNoUnitFoundForIntyg() {
        when(intygService.getIssuingVardenhetHsaId(INTYG_ID, INTYG_TYP_FK7263)).thenReturn(null);
        ResponseEntity<Object> result = controller.redirectToIntyg(INTYG_ID, INTYG_TYP_FK7263);
        assertEquals(404, result.getStatusCode().value());
    }

    @Test
    public void testRedirectToIntygMaillinkReturnsNull() {
        when(webCertUserService.getUser()).thenReturn(createMockUser(false));
        when(intygService.getIssuingVardenhetHsaId(INTYG_ID, INTYG_TYP_FK7263)).thenReturn("ve-1");
        when(utkastRepository.findOne(INTYG_ID)).thenReturn(null);
        when(intygService.getIntygTypeInfo(INTYG_ID, null)).thenReturn(intygTypeInfo);
        when(mailLinkService.intygRedirect(INTYG_TYP_FK7263, INTYG_TYPE_VERSION, INTYG_ID)).thenReturn(null);

        ResponseEntity<Object> result = controller.redirectToIntyg(INTYG_ID, INTYG_TYP_FK7263);
        assertEquals(404, result.getStatusCode().value());
    }

    private URI buildMockURI() {
        return URI.create("https://some.url/path/questions");
    }

    private WebCertUser createMockUser(boolean doctor, String... features) {
        Role role = AUTHORITIES_RESOLVER.getRole(AuthoritiesConstants.ROLE_LAKARE);

        if (!doctor) {
            role = AUTHORITIES_RESOLVER.getRole(AuthoritiesConstants.ROLE_ADMIN);
        }

        WebCertUser user = new WebCertUser();
        user.setRoles(AuthoritiesResolverUtil.toMap(role));
        user.setAuthorities(AuthoritiesResolverUtil.toMap(role.getPrivileges(), Privilege::getName));
        user.setVardgivare(Collections.singletonList(createMockVardgivare()));
        user.setFeatures(Stream.of(features).collect(Collectors.toMap(Function.identity(), s -> {
            Feature feature = new Feature();
            feature.setName(s);
            feature.setGlobal(true);
            feature.setIntygstyper(Collections.singletonList(INTYG_TYP_FK7263));
            return feature;
        })));
        return user;
    }

    private Vardgivare createMockVardgivare() {
        Vardgivare vg = new Vardgivare("vg-1", "Vårdgivare 1");
        Vardenhet ve = new Vardenhet("ve-1", "Vårdenhet 1");
        vg.setVardenheter(Collections.singletonList(ve));
        return vg;
    }

}
