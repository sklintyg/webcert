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
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import javax.ws.rs.core.Response;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import se.inera.intyg.common.support.model.UtkastStatus;
import se.inera.intyg.webcert.web.service.utkast.UtkastService;
import se.inera.intyg.webcert.web.service.utkast.dto.SaveDraftResponse;
import se.inera.intyg.webcert.web.web.controller.api.dto.CopyFromCandidateRequest;
import se.inera.intyg.webcert.web.web.controller.moduleapi.UtkastModuleApiController;

@RunWith(MockitoJUnitRunner.class)
public class UtkastModuleApiControllerTest {

    @Mock
    private UtkastService utkastService;

    @InjectMocks
    private UtkastModuleApiController utkastModuleApiController;

    @Test
    public void testCopyFromCandidate() {
        String utkastId = "abc123";
        String utkastType = "ag7804";
        String intygTypeVersion = "1.0";
        String intygIdCandidate = "klm789";
        String intygTypeCandidate = "lisjp"; //fk7804

        long utkastVersion = 0L;

        when(utkastService.updateDraftFromCandidate(anyString(), anyString(), anyString(), anyString()))
            .thenReturn(createResponse(utkastVersion, UtkastStatus.DRAFT_INCOMPLETE));

        CopyFromCandidateRequest request = new CopyFromCandidateRequest();
        request.setCandidateId(intygIdCandidate);
        request.setCandidateType(intygTypeCandidate);
        request.setCandidateTypeVersion(intygTypeVersion);

        Response response = utkastModuleApiController.copyFromCandidate(utkastType, utkastId, request);

        assertNotNull(response);

        verify(utkastService, times(1))
            .updateDraftFromCandidate(anyString(), anyString(), anyString(), anyString());

        SaveDraftResponse res = (SaveDraftResponse) response.getEntity();
        assertEquals(UtkastStatus.DRAFT_INCOMPLETE, res.getStatus());
        assertEquals(utkastVersion, res.getVersion());
    }

    private SaveDraftResponse createResponse(long version, UtkastStatus status) {
        return new SaveDraftResponse(version, status);
    }

}
