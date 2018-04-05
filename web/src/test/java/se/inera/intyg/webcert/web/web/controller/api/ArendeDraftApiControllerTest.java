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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;

import javax.ws.rs.core.Response;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import se.inera.intyg.webcert.persistence.arende.model.ArendeDraft;
import se.inera.intyg.webcert.web.service.arende.ArendeDraftService;
import se.inera.intyg.webcert.web.web.controller.api.dto.ArendeDraftEntry;

@RunWith(MockitoJUnitRunner.class)
public class ArendeDraftApiControllerTest {

    @InjectMocks
    ArendeDraftApiController controller;

    @Mock
    ArendeDraftService arendeDraftService;

    @Test
    public void testSaveOk() {
        final String intygId = "intygId";
        final String questionId = "questionId";
        final String text = "text";
        final String amne = "amne";

        when(arendeDraftService.saveDraft(intygId, questionId, text, amne)).thenReturn(true);

        Response res = controller.save(buildEntry(intygId, questionId, text, amne));

        assertNotNull(res);
        assertEquals(Response.Status.OK.getStatusCode(), res.getStatus());
        verify(arendeDraftService).saveDraft(intygId, questionId, text, amne);
        verifyNoMoreInteractions(arendeDraftService);
    }

    @Test
    public void testSaveNotOk() {
        final String intygId = "intygId";
        final String questionId = "questionId";
        final String text = "text";
        final String amne = "amne";

        when(arendeDraftService.saveDraft(intygId, questionId, text, amne)).thenReturn(false);

        Response res = controller.save(buildEntry(intygId, questionId, text, amne));

        assertNotNull(res);
        assertEquals(Response.Status.INTERNAL_SERVER_ERROR.getStatusCode(), res.getStatus());
        verify(arendeDraftService).saveDraft(intygId, questionId, text, amne);
        verifyNoMoreInteractions(arendeDraftService);
    }

    @Test
    public void testSaveEntryNotOk() {
        Response res = controller.save(buildEntry(null, null, null, null));

        assertNotNull(res);
        assertEquals(Response.Status.BAD_REQUEST.getStatusCode(), res.getStatus());
        verifyZeroInteractions(arendeDraftService);
    }

    @Test
    public void testDeleteOk() {
        final String intygId = "intygId";
        final String questionId = "questionId";

        when(arendeDraftService.delete(intygId, questionId)).thenReturn(true);

        Response res = controller.delete(intygId, questionId);

        assertNotNull(res);
        assertEquals(Response.Status.OK.getStatusCode(), res.getStatus());
        verify(arendeDraftService).delete(intygId, questionId);
        verifyNoMoreInteractions(arendeDraftService);
    }

    @Test
    public void testDeleteNotFound() {
        final String intygId = "intygId";
        final String questionId = "questionId";

        when(arendeDraftService.delete(intygId, questionId)).thenReturn(false);

        Response res = controller.delete(intygId, questionId);

        assertNotNull(res);
        assertEquals(Response.Status.NOT_FOUND.getStatusCode(), res.getStatus());
        verify(arendeDraftService).delete(intygId, questionId);
        verifyNoMoreInteractions(arendeDraftService);
    }

    @Test
    public void testGetQuestionDraftOk() {
        final String intygId = "intygId";
        final String text = "text";
        final String amne = "amne";

        when(arendeDraftService.getQuestionDraft(intygId)).thenReturn(buildArendeDraft(intygId, text, amne));

        Response res = controller.getQuestionDraft(intygId);
        assertNotNull(res);
        assertEquals(Response.Status.OK.getStatusCode(), res.getStatus());
        assertEquals(intygId, ((ArendeDraftEntry) res.getEntity()).getIntygId());
        assertEquals(text, ((ArendeDraftEntry) res.getEntity()).getText());
        assertEquals(amne, ((ArendeDraftEntry) res.getEntity()).getAmne());
        assertNull(((ArendeDraftEntry) res.getEntity()).getQuestionId());
        verify(arendeDraftService).getQuestionDraft(intygId);
    }

    @Test
    public void testGetQuestionDraftNotFound() {

        final String intygId = "intygId";

        when(arendeDraftService.getQuestionDraft(intygId)).thenReturn(null);

        Response res = controller.getQuestionDraft(intygId);
        assertNotNull(res);
        assertEquals(Response.Status.NO_CONTENT.getStatusCode(), res.getStatus());
        verify(arendeDraftService).getQuestionDraft(intygId);
    }

    private ArendeDraft buildArendeDraft(String intygId, String text, String amne) {
        ArendeDraft arendeDraft = new ArendeDraft();
        arendeDraft.setIntygId(intygId);
        arendeDraft.setAmne(amne);
        arendeDraft.setText(text);
        return arendeDraft;
    }

    private ArendeDraftEntry buildEntry(String intygId, String questionId, String text, String amne) {
        ArendeDraftEntry entry = new ArendeDraftEntry();
        entry.setIntygId(intygId);
        entry.setQuestionId(questionId);
        entry.setText(text);
        entry.setAmne(amne);
        return entry;
    }
}
