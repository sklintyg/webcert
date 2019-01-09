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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import javax.ws.rs.core.Response;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import se.inera.intyg.infra.security.authorities.AuthoritiesException;
import se.inera.intyg.infra.security.common.model.AuthoritiesConstants;
import se.inera.intyg.infra.security.common.model.Privilege;
import se.inera.intyg.infra.security.common.model.RequestOrigin;
import se.inera.intyg.webcert.web.service.receiver.CertificateReceiverService;
import se.inera.intyg.webcert.web.service.user.WebCertUserService;
import se.inera.intyg.webcert.web.service.user.dto.WebCertUser;
import se.inera.intyg.webcert.web.web.controller.api.dto.IntygReceiver;

@RunWith(MockitoJUnitRunner.class)
public class ReceiverApiControllerTest {

    private static final String INTYG_ID = "intyg-123";

    @Mock
    private CertificateReceiverService certificateReceiverService;

    @Mock
    private WebCertUserService webcertUserService;

    @InjectMocks
    private ReceiverApiController receiverApiController;

    @Test
    public void testListPossibleWithApprovedReceivers() {
        when(certificateReceiverService.listPossibleReceiversWithApprovedInfo("LISJP", INTYG_ID)).thenReturn(buildReceiverList());
        Response resp = receiverApiController.listApprovedReceivers("LISJP", INTYG_ID);
        assertNotNull(resp);
        assertEquals(200, resp.getStatus());
        verify(certificateReceiverService, times(1)).listPossibleReceiversWithApprovedInfo("LISJP", INTYG_ID);
    }

    @Test
    public void testRegisterApprovedReceivers() {
        setupUser(AuthoritiesConstants.PRIVILEGE_GODKANNA_MOTTAGARE,"LISJP");
        Response resp = receiverApiController.registerApprovedReceivers("LISJP", INTYG_ID, Arrays.asList("FKASSA"));
        assertNotNull(resp);
        assertEquals(200, resp.getStatus());
        verify(certificateReceiverService, times(1)).registerApprovedReceivers(anyString(), anyString(), anyList());
    }

    @Test(expected = AuthoritiesException.class)
    public void testRegisterApprovedReceiversFailsIfNotAuth() {
        setupUser(AuthoritiesConstants.PRIVILEGE_GODKANNA_MOTTAGARE,"ts-bas");
        Response resp = receiverApiController.registerApprovedReceivers("LISJP", INTYG_ID, Arrays.asList("FKASSA"));
        assertNotNull(resp);
        assertEquals(200, resp.getStatus());
        verify(certificateReceiverService, times(1)).registerApprovedReceivers(anyString(), anyString(), anyList());
    }

    private List<IntygReceiver> buildReceiverList() {
        List<IntygReceiver> list = new ArrayList<>();
        list.add(IntygReceiver.IntygReceiverBuilder.anIntygReceiver()
                .withId("1")
                .withName("namnet")
                .withReceiverType("HUVUDMOTTAGARE")
                .withApprovalStatus(IntygReceiver.ApprovalStatus.UNDEFINED)
                .withLocked(false)
                .build());
        return list;
    }

    private void setupUser(String privilegeString, String intygType) {
        WebCertUser user = new WebCertUser();
        user.setAuthorities(new HashMap<>());

        Privilege privilege = new Privilege();
        privilege.setIntygstyper(Arrays.asList(intygType));
        RequestOrigin requestOrigin = new RequestOrigin();
        requestOrigin.setName("NORMAL");
        requestOrigin.setIntygstyper(privilege.getIntygstyper());
        privilege.setRequestOrigins(Arrays.asList(requestOrigin));
        user.getAuthorities().put(privilegeString, privilege);
        user.setOrigin("NORMAL");

        when(webcertUserService.getUser()).thenReturn(user);
    }
}
