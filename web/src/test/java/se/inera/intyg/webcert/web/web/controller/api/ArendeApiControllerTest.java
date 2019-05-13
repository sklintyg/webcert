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

package se.inera.intyg.webcert.web.web.controller.api;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import se.inera.intyg.clinicalprocess.healthcond.certificate.getcertificateadditions.v1.GetCertificateAdditionsResponseType;
import se.inera.intyg.clinicalprocess.healthcond.certificate.getcertificateadditions.v1.GetCertificateAdditionsType;
import se.inera.intyg.infra.security.common.model.AuthoritiesConstants;
import se.inera.intyg.infra.security.common.model.Privilege;
import se.inera.intyg.infra.security.common.model.RequestOrigin;
import se.inera.intyg.infra.security.common.model.UserOriginType;
import se.inera.intyg.webcert.persistence.arende.model.Arende;
import se.inera.intyg.webcert.persistence.model.Status;
import se.inera.intyg.webcert.web.service.arende.ArendeService;
import se.inera.intyg.webcert.web.service.user.WebCertUserService;
import se.inera.intyg.webcert.web.service.user.dto.WebCertUser;
import se.riv.clinicalprocess.healthcond.certificate.types.v3.IntygId;

import javax.ws.rs.core.Response;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.when;

/**
 * @author Magnus Ekstrand on 2019-05-13.
 */
@RunWith(MockitoJUnitRunner.class)
public class ArendeApiControllerTest {

    private static final List<Long> ARENDE_IDS = Arrays.asList(1234567L, 2345678L, 3456789L);

    private static final List<String> INTYG_IDS = Arrays.asList("ABC123", "DEF456", "GHI789");

    @Mock
    private WebCertUserService webCertUserService;

    @Mock
    private ArendeService arendeService;

    @InjectMocks
    ArendeApiController testee;

    @Before
    public void setUp() {
        when(webCertUserService.getUser()).thenReturn(mockUser());
    }

    @Test
    public void whenGettingKompletteringarSuccessfully() {
        when(arendeService.getKompletteringar(INTYG_IDS)).thenReturn(mockKompetteringar());

        GetCertificateAdditionsResponseType additions
                = (GetCertificateAdditionsResponseType) testee.getKompletteringar(buildRequest()).getEntity();
        assertEquals(3, additions.getAdditions().size());
    }

    @Test
    public void whenThereAreNoKompletteringar() {
        when(arendeService.getKompletteringar(INTYG_IDS)).thenReturn(new ArrayList<>());

        Response response = testee.getKompletteringar(buildRequest());
        assertEquals(ArendeApiController.NO_CONTENT, response.getStatus());

        GetCertificateAdditionsResponseType additions = (GetCertificateAdditionsResponseType) response.getEntity();
        assertEquals(0, additions.getAdditions().size());
    }

    @Test
    public void whenInvalidRequest() {
        Response response = null;

        response = testee.getKompletteringar(null);
        assertEquals(ArendeApiController.BAD_REQUEST, response.getStatus());

        response = testee.getKompletteringar(new GetCertificateAdditionsType());
        assertEquals(ArendeApiController.BAD_REQUEST, response.getStatus());

        response = testee.getKompletteringar(buildEmptyRequest());
        assertEquals(ArendeApiController.BAD_REQUEST, response.getStatus());
    }

    private GetCertificateAdditionsType buildEmptyRequest() {
        GetCertificateAdditionsType request = new GetCertificateAdditionsType();
        request.getIntygsId().addAll(new ArrayList<>());
        return request;
    }

    private GetCertificateAdditionsType buildRequest() {
        List<IntygId> identities = INTYG_IDS.stream()
                .map(s -> {
                    IntygId intygId = new IntygId();
                    intygId.setRoot("some-root-value");
                    intygId.setExtension(s);
                    return intygId;
                })
                .collect(Collectors.toList());

        GetCertificateAdditionsType request = new GetCertificateAdditionsType();
        request.getIntygsId().addAll(identities);

        return request;
    }

    private List<Arende> mockKompetteringar() {
        List<Arende> arenden = new ArrayList<>();

        for (int i = 0; i < ARENDE_IDS.size(); i++) {
            Arende arende = new Arende();
            arende.setId(ARENDE_IDS.get(i));
            arende.setStatus(Status.PENDING_INTERNAL_ACTION);
            arende.setTimestamp(LocalDateTime.now(ZoneId.systemDefault()));
            arende.setIntygsId(INTYG_IDS.get(i));

            arenden.add(arende);
        }

        return arenden;
    }

    private WebCertUser mockUser() {
        RequestOrigin requestOrigin = new RequestOrigin();
        requestOrigin.setName(UserOriginType.READONLY.name());

        Privilege privilege = new Privilege();
        privilege.setRequestOrigins(Arrays.asList(requestOrigin));

        WebCertUser user = new WebCertUser();
        user.setAuthorities(new HashMap<>());
        user.getAuthorities().put(AuthoritiesConstants.PRIVILEGE_VISA_INTYG, privilege);
        user.setOrigin(UserOriginType.READONLY.name());

        return user;
    }

}