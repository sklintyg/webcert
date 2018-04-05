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
package se.inera.intyg.webcert.web.service.arende;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.List;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import se.inera.intyg.webcert.persistence.arende.model.ArendeDraft;
import se.inera.intyg.webcert.persistence.arende.repository.ArendeDraftRepository;

@RunWith(MockitoJUnitRunner.class)
public class ArendeDraftServiceImplTest {

    @Mock
    private ArendeDraftRepository repo;

    @InjectMocks
    private ArendeDraftServiceImpl arendeDraftService;

    @Test
    public void testSaveDraftNew() {
        final String intygId = "intygId";
        final String questionId = "questionId";
        final String text = "text";
        final String amne = "amne";
        when(repo.findByIntygIdAndQuestionId(intygId, questionId)).thenReturn(null);

        boolean res = arendeDraftService.saveDraft(intygId, questionId, text, amne);

        assertTrue(res);
        verify(repo).findByIntygIdAndQuestionId(intygId, questionId);

        ArgumentCaptor<ArendeDraft> captor = ArgumentCaptor.forClass(ArendeDraft.class);
        verify(repo).save(captor.capture());
        assertEquals(intygId, captor.getValue().getIntygId());
        assertEquals(questionId, captor.getValue().getQuestionId());
        assertEquals(text, captor.getValue().getText());
        assertEquals(amne, captor.getValue().getAmne());
        verifyNoMoreInteractions(repo);
    }

    @Test
    public void testSaveDraftUpdate() {
        final String intygId = "intygId";
        final String questionId = "questionId";
        final String text = "text";
        final String amne = "amne";
        when(repo.findByIntygIdAndQuestionId(intygId, questionId)).thenReturn(buildArendeDraft(intygId, questionId, null, null));

        boolean res = arendeDraftService.saveDraft(intygId, questionId, text, amne);

        assertTrue(res);
        verify(repo).findByIntygIdAndQuestionId(intygId, questionId);

        ArgumentCaptor<ArendeDraft> captor = ArgumentCaptor.forClass(ArendeDraft.class);
        verify(repo).save(captor.capture());
        assertEquals(intygId, captor.getValue().getIntygId());
        assertEquals(questionId, captor.getValue().getQuestionId());
        assertEquals(text, captor.getValue().getText());
        assertEquals(amne, captor.getValue().getAmne());
        verifyNoMoreInteractions(repo);
    }

    @Test
    public void testDeleteExisting() {
        final String intygId = "intygId";
        final String questionId = "questionId";
        ArendeDraft draft = buildArendeDraft(intygId, questionId, null, null);
        when(repo.findByIntygIdAndQuestionId(intygId, questionId)).thenReturn(draft);

        boolean res = arendeDraftService.delete(intygId, questionId);

        assertTrue(res);
        verify(repo).findByIntygIdAndQuestionId(intygId, questionId);
        verify(repo).delete(draft);
        verifyNoMoreInteractions(repo);
    }

    @Test
    public void testDeleteNotExisting() {
        final String intygId = "intygId";
        final String questionId = "questionId";
        when(repo.findByIntygIdAndQuestionId(intygId, questionId)).thenReturn(null);

        boolean res = arendeDraftService.delete(intygId, questionId);

        assertFalse(res);
        verify(repo).findByIntygIdAndQuestionId(intygId, questionId);
        verifyNoMoreInteractions(repo);
    }

    @Test
    public void testListAnswerDraft() {
        final String intygId = "intygId";
        final String questionId = "questionId";
        when(repo.findByIntygId(intygId)).thenReturn(Arrays.asList(buildArendeDraft(intygId, questionId, null, null)));

        List<ArendeDraft> res = arendeDraftService.listAnswerDrafts(intygId);

        assertNotNull(res);
        assertEquals(1, res.size());
        assertEquals(intygId, res.get(0).getIntygId());
        assertEquals(questionId, res.get(0).getQuestionId());
        verify(repo).findByIntygId(intygId);
        verifyNoMoreInteractions(repo);
    }

    @Test
    public void testGetQuestionDraft() {
        final String intygId = "intygId";
        when(repo.findByIntygIdAndQuestionId(intygId, null)).thenReturn(buildArendeDraft(intygId, null, null, null));

        ArendeDraft res = arendeDraftService.getQuestionDraft(intygId);

        assertNotNull(res);
        assertEquals(intygId, res.getIntygId());
        verify(repo).findByIntygIdAndQuestionId(intygId, null);
        verifyNoMoreInteractions(repo);

    }

    private ArendeDraft buildArendeDraft(String intygId, String questionId, String text, String amne) {
        ArendeDraft draft = new ArendeDraft();
        draft.setIntygId(intygId);
        draft.setQuestionId(questionId);
        draft.setText(text);
        draft.setAmne(amne);
        return draft;
    }
}
