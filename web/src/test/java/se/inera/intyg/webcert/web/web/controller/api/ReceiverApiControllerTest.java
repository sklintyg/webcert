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
package se.inera.intyg.webcert.web.web.controller.api;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import se.inera.intyg.webcert.web.service.receiver.CertificateReceiverService;
import se.inera.intyg.webcert.web.web.controller.api.dto.IntygReceiver;

import javax.ws.rs.core.Response;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class ReceiverApiControllerTest {

    private static final String INTYG_ID = "intyg-123";

    @Mock
    private CertificateReceiverService certificateReceiverService;

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
        Response resp = receiverApiController.registerApprovedReceivers(INTYG_ID, Arrays.asList("FKASSA"));
        assertNotNull(resp);
        assertEquals(200, resp.getStatus());
        verify(certificateReceiverService, times(1)).registerApprovedReceivers(anyString(), anyList());
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

}
