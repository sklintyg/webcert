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
package se.inera.intyg.webcert.web.integration.interactions.createdraftcertificate;

import org.mockito.Mock;
import se.inera.intyg.infra.integration.hsa.model.Vardenhet;
import se.inera.intyg.infra.integration.hsa.model.Vardgivare;
import se.inera.intyg.infra.security.common.model.AuthoritiesConstants;
import se.inera.intyg.infra.security.common.model.Feature;
import se.inera.intyg.infra.security.common.model.Privilege;
import se.inera.intyg.infra.security.common.model.RequestOrigin;
import se.inera.intyg.infra.security.common.model.UserOriginType;
import se.inera.intyg.webcert.web.auth.WebcertUserDetailsService;
import se.inera.intyg.webcert.web.service.user.dto.WebCertUser;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.mockito.Mockito.when;

/**
 * Created by eriklupander on 2017-09-27.
 */
public abstract class BaseCreateDraftCertificateTest {

    public static final String FULLSTANDIGT_NAMN = "Abel Baker";
    public static final String INVARTES_MEDICIN = "Invärtes medicin";
    public static final String TITLE_CODE = "203010";
    public static final String TITLE_NAME = "Läkare";
    public static final String ALLMAN_MEDICIN = "Allmänmedicin";
    public static final String MEDARBETARUPPDRAG = "Vård och behandling";
    protected static final String FK7263 = "fk7263";
    protected static final String TSBAS = "ts-bas";
    protected static final String LOGICAL_ADDR = "1234567890";
    protected static final String USER_HSAID = "SE1234567890";
    protected static final String UNIT_HSAID = "SE0987654321";
    protected static final String CAREGIVER_HSAID = "SE0000112233";
    protected static final String UTKAST_ID = "abc123";
    protected static final String UTKAST_VERSION = "1";
    protected static final String UTKAST_TYPE = "fk7263";
    protected static final String UTKAST_JSON = "A bit of text representing json";
    @Mock
    protected WebcertUserDetailsService webcertUserDetailsService;

    public void setup() {
        when(webcertUserDetailsService.loadUserByHsaId(USER_HSAID)).thenReturn(buildWebCertUser());
    }

    protected WebCertUser buildWebCertUser() {
        WebCertUser user = new WebCertUser();
        user.setAuthorities(new HashMap<>());

        user.getAuthorities().put(AuthoritiesConstants.PRIVILEGE_HANTERA_SEKRETESSMARKERAD_PATIENT,
                createPrivilege(AuthoritiesConstants.PRIVILEGE_HANTERA_SEKRETESSMARKERAD_PATIENT));
        user.getAuthorities().put(AuthoritiesConstants.PRIVILEGE_SKRIVA_INTYG,
                createPrivilege(AuthoritiesConstants.PRIVILEGE_SKRIVA_INTYG));
        // TODO
        user.setFeatures(Stream.of(AuthoritiesConstants.FEATURE_HANTERA_INTYGSUTKAST, AuthoritiesConstants.FEATURE_TAK_KONTROLL)
                .collect(Collectors.toMap(Function.identity(), s -> {
                    Feature feature = new Feature();
                    feature.setName(s);
                    feature.setGlobal(true);
                    feature.setIntygstyper(Arrays.asList(FK7263, TSBAS));
                    return feature;
                })));
        user.setOrigin(UserOriginType.DJUPINTEGRATION.name());
        user.setBefattningar(Arrays.asList(TITLE_CODE));
        user.setSpecialiseringar(Arrays.asList(ALLMAN_MEDICIN, INVARTES_MEDICIN));
        user.setTitel(TITLE_NAME);
        user.setVardgivare(Arrays.asList(createVardgivare()));
        user.setMiuNamnPerEnhetsId(createMiuNamnPerEnhetsId());
        return user;
    }

    private Map<String, String> createMiuNamnPerEnhetsId() {
        Map<String, String> map = new HashMap<>();
        map.put(UNIT_HSAID, MEDARBETARUPPDRAG);
        return map;
    }

    private Privilege createPrivilege(String privilege) {
        Privilege priv = new Privilege();
        priv.setName(privilege);
        RequestOrigin requestOrigin = new RequestOrigin();
        requestOrigin.setName(UserOriginType.DJUPINTEGRATION.name());
        requestOrigin.setIntygstyper(Arrays.asList(FK7263, TSBAS));
        priv.setRequestOrigins(Arrays.asList(requestOrigin));
        priv.setIntygstyper(Arrays.asList(FK7263, TSBAS));
        return priv;
    }

    private Vardenhet createVardenhet(Vardgivare vardgivare) {
        Vardenhet vardenhet = new Vardenhet();
        vardenhet.setId(UNIT_HSAID);
        vardenhet.setNamn("Vardenheten");
        vardenhet.setVardgivareHsaId(vardgivare.getId());
        vardenhet.setArbetsplatskod("12345");
        vardenhet.setPostadress("Gatan 1");
        vardenhet.setPostnummer("54321");
        vardenhet.setPostort("Vardmåla");
        vardenhet.setTelefonnummer("123-456789");
        return vardenhet;
    }

    private Vardgivare createVardgivare() {
        Vardgivare vardgivare = new Vardgivare();
        vardgivare.setId(CAREGIVER_HSAID);
        vardgivare.setNamn("Vardgivaren");
        vardgivare.setVardenheter(Arrays.asList(createVardenhet(vardgivare)));
        return vardgivare;
    }
}
