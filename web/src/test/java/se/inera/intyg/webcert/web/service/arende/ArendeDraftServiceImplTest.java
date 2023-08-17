/*
 * Copyright (C) 2023 Inera AB (http://www.inera.se)
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

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import se.inera.intyg.webcert.persistence.arende.model.ArendeDraft;
import se.inera.intyg.webcert.persistence.arende.repository.ArendeDraftRepository;

@ExtendWith(MockitoExtension.class)
public class ArendeDraftServiceImplTest {

    @Mock
    private ArendeDraftRepository repo;

    @InjectMocks
    private ArendeDraftServiceImpl arendeDraftService;

    private static final String intygId = "intygId";
    private static final String questionId = "questionId";
    private static final String text = "text";
    private static final String amne = "amne";
    private final ArendeDraft arendeDraft = buildArendeDraft(intygId, questionId, null, null);

    @Nested
    class NewDraft {

        @BeforeEach
        void setup() {
            when(repo.findByIntygIdAndQuestionId(intygId, questionId)).thenReturn(null);
        }

        @Test
        public void testSaveDraftNew() {
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
        public void testCreateNew() {
            arendeDraftService.create(intygId, amne, text, questionId);

            verify(repo).findByIntygIdAndQuestionId(intygId, questionId);

            ArgumentCaptor<ArendeDraft> captor = ArgumentCaptor.forClass(ArendeDraft.class);
            verify(repo).save(captor.capture());
            assertEquals(intygId, captor.getValue().getIntygId());
            assertEquals(questionId, captor.getValue().getQuestionId());
            assertEquals(text, captor.getValue().getText());
            assertEquals(amne, captor.getValue().getAmne());
            verifyNoMoreInteractions(repo);
        }
    }

    @Nested
    class UpdateDraft {
        @BeforeEach
        void setup() {
            when(repo.findByIntygIdAndQuestionId(intygId, questionId)).thenReturn(arendeDraft);
        }

        @Test
        public void testSaveDraftUpdate() {
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
        public void shouldReturnSavedArendeDraftFromCreate() {
            when(repo.findByIntygIdAndQuestionId(intygId, questionId)).thenReturn(arendeDraft);
            when(repo.save(any())).thenReturn(arendeDraft);

            final var result = arendeDraftService.create(intygId, amne, text, questionId);

            assertEquals(arendeDraft, result);
        }

        @Test
        public void testCreateDraftUpdate() {
            arendeDraftService.create(intygId, amne, text, questionId);

            verify(repo).findByIntygIdAndQuestionId(intygId, questionId);

            ArgumentCaptor<ArendeDraft> captor = ArgumentCaptor.forClass(ArendeDraft.class);
            verify(repo).save(captor.capture());
            assertEquals(intygId, captor.getValue().getIntygId());
            assertEquals(questionId, captor.getValue().getQuestionId());
            assertEquals(text, captor.getValue().getText());
            assertEquals(amne, captor.getValue().getAmne());
            verifyNoMoreInteractions(repo);
        }
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
