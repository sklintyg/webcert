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
import se.inera.intyg.common.db.support.DbModuleEntryPoint;
import se.inera.intyg.common.doi.support.DoiModuleEntryPoint;
import se.inera.intyg.common.fk7263.support.Fk7263EntryPoint;
import se.inera.intyg.common.lisjp.support.LisjpEntryPoint;
import se.inera.intyg.common.luae_fs.support.LuaefsEntryPoint;
import se.inera.intyg.common.luae_na.support.LuaenaEntryPoint;
import se.inera.intyg.common.luse.support.LuseEntryPoint;
import se.inera.intyg.common.support.modules.registry.IntygModuleRegistry;
import se.inera.intyg.common.ts_bas.support.TsBasEntryPoint;
import se.inera.intyg.common.ts_diabetes.support.TsDiabetesEntryPoint;
import se.inera.intyg.infra.security.authorities.CommonAuthoritiesResolver;
import se.inera.intyg.infra.security.common.model.AuthoritiesConstants;
import se.inera.intyg.infra.security.common.model.Privilege;
import se.inera.intyg.infra.security.common.model.RequestOrigin;
import se.inera.intyg.infra.security.common.model.UserOriginType;
import se.inera.intyg.webcert.common.model.WebcertFeature;
import se.inera.intyg.webcert.web.service.patient.PatientDetailsResolver;
import se.inera.intyg.webcert.web.service.user.dto.WebCertUser;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.when;

/**
 * Created by eriklupander on 2017-09-19.
 */
public abstract class BaseCreateDraftCertificateValidatorImplTest {


    private static List<String> ALL_INTYG_TYPES = Arrays.asList(Fk7263EntryPoint.MODULE_ID,
            TsBasEntryPoint.MODULE_ID, TsDiabetesEntryPoint.MODULE_ID,
            LisjpEntryPoint.MODULE_ID, LuaefsEntryPoint.MODULE_ID, LuseEntryPoint.MODULE_ID,
            LuaenaEntryPoint.MODULE_ID, DbModuleEntryPoint.MODULE_ID, DoiModuleEntryPoint.MODULE_ID);

    protected  static final String FK7263 = Fk7263EntryPoint.MODULE_ID;
    protected static final String TSBAS = TsBasEntryPoint.MODULE_ID;

    protected WebCertUser user;

    @Mock
    protected IntygModuleRegistry moduleRegistry;

    @Mock
    protected CommonAuthoritiesResolver commonAuthoritiesResolver;

    @Mock
    protected PatientDetailsResolver patientDetailsResolver;
    //
    // @Mock
    // protected WebcertUserDetailsService webcertUserDetailsService;

    @Before
    public void setup() {
        when(moduleRegistry.getModuleIdFromExternalId(anyString()))
                .thenAnswer(invocation -> ((String) invocation.getArguments()[0]).toLowerCase());
        when(moduleRegistry.moduleExists(Fk7263EntryPoint.MODULE_ID)).thenReturn(true);
        when(moduleRegistry.moduleExists(TsBasEntryPoint.MODULE_ID)).thenReturn(true);
        when(commonAuthoritiesResolver.getSekretessmarkeringAllowed())
                .thenReturn(ALL_INTYG_TYPES);
        user = buildUser();
        // when(webcertUserDetailsService.loadUserByHsaId(anyString())).thenReturn(user);
    }

    protected WebCertUser buildUser() {
        WebCertUser user = new WebCertUser();
        user.setAuthorities(new HashMap<>());

        user.getAuthorities().put(AuthoritiesConstants.PRIVILEGE_HANTERA_SEKRETESSMARKERAD_PATIENT,
                createPrivilege(AuthoritiesConstants.PRIVILEGE_HANTERA_SEKRETESSMARKERAD_PATIENT));
        user.getAuthorities().put(AuthoritiesConstants.PRIVILEGE_SKRIVA_INTYG,
                createPrivilege(AuthoritiesConstants.PRIVILEGE_SKRIVA_INTYG));
        user.setFeatures(ImmutableSet
                .of(WebcertFeature.HANTERA_INTYGSUTKAST.getName(), WebcertFeature.HANTERA_INTYGSUTKAST.getName() + "." + Fk7263EntryPoint.MODULE_ID,
                        WebcertFeature.HANTERA_INTYGSUTKAST.getName() + "." + TsBasEntryPoint.MODULE_ID));
        user.setOrigin(UserOriginType.DJUPINTEGRATION.name());
        return user;
    }

    protected WebCertUser buildUserUnauthorized() {
        WebCertUser user = new WebCertUser();
        user.setAuthorities(new HashMap<>());

        user.getAuthorities().put(AuthoritiesConstants.PRIVILEGE_HANTERA_SEKRETESSMARKERAD_PATIENT,
                createPrivilege(AuthoritiesConstants.PRIVILEGE_HANTERA_SEKRETESSMARKERAD_PATIENT, ALL_INTYG_TYPES));
        user.getAuthorities().put(AuthoritiesConstants.PRIVILEGE_SKRIVA_INTYG,
                createPrivilege(AuthoritiesConstants.PRIVILEGE_SKRIVA_INTYG, ALL_INTYG_TYPES));
        user.setFeatures(ImmutableSet
                .of(WebcertFeature.HANTERA_INTYGSUTKAST.getName(),
                        WebcertFeature.HANTERA_INTYGSUTKAST.getName() + "." + Fk7263EntryPoint.MODULE_ID,
                        WebcertFeature.HANTERA_INTYGSUTKAST.getName() + "." + TsBasEntryPoint.MODULE_ID,
                        WebcertFeature.HANTERA_INTYGSUTKAST.getName() + "." + TsDiabetesEntryPoint.MODULE_ID,
                        WebcertFeature.HANTERA_INTYGSUTKAST.getName() + "." + LisjpEntryPoint.MODULE_ID,
                        WebcertFeature.HANTERA_INTYGSUTKAST.getName() + "." + LuaefsEntryPoint.MODULE_ID,
                        WebcertFeature.HANTERA_INTYGSUTKAST.getName() + "." + LuseEntryPoint.MODULE_ID,
                        WebcertFeature.HANTERA_INTYGSUTKAST.getName() + "." + LuaenaEntryPoint.MODULE_ID,
                        WebcertFeature.HANTERA_INTYGSUTKAST.getName() + "." + DbModuleEntryPoint.MODULE_ID,
                        WebcertFeature.HANTERA_INTYGSUTKAST.getName() + "." + DoiModuleEntryPoint.MODULE_ID));
        user.setOrigin(UserOriginType.DJUPINTEGRATION.name());
        return user;
    }
    protected Privilege createPrivilege(String privilege) {
        return createPrivilege(privilege, Arrays.asList(Fk7263EntryPoint.MODULE_ID, TsBasEntryPoint.MODULE_ID));
    }

    protected Privilege createPrivilege(String privilege, List<String> intygstyper) {
        Privilege priv = new Privilege();
        priv.setName(privilege);
        RequestOrigin requestOrigin = new RequestOrigin();
        requestOrigin.setName(UserOriginType.DJUPINTEGRATION.name());
        requestOrigin.setIntygstyper(intygstyper);
        priv.setRequestOrigins(Arrays.asList(requestOrigin));
        priv.setIntygstyper(intygstyper);
        return priv;
    }
}
