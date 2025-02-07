/*
 * Copyright (C) 2025 Inera AB (http://www.inera.se)
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
package se.inera.intyg.webcert.web.service.facade.question;

import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.verify;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import se.inera.intyg.webcert.persistence.arende.model.ArendeDraft;
import se.inera.intyg.webcert.web.service.arende.ArendeDraftService;
import se.inera.intyg.webcert.web.service.facade.question.impl.DeleteQuestionFacadeServiceImpl;

@ExtendWith(MockitoExtension.class)
class DeleteQuestionFacadeServiceImplTest {

    private final String CERTIFICATE_ID = "certificateId";
    private final String ID_AS_STRING = "1000";
    private final long ID_AS_LONG = 1000L;

    @Mock
    ArendeDraftService arendeDraftService;

    @InjectMocks
    private DeleteQuestionFacadeServiceImpl deleteQuestionDraftFacadeService;

    private ArendeDraft arendeDraft;

    @BeforeEach
    void setup() {
        arendeDraft = new ArendeDraft();
        arendeDraft.setIntygId(CERTIFICATE_ID);
        arendeDraft.setId(ID_AS_LONG);

        doReturn(arendeDraft)
            .when(arendeDraftService)
            .getQuestionDraftById(ID_AS_LONG);
    }

    @Test
    void shallDeleteDraftForCertificate() {
        deleteQuestionDraftFacadeService.delete(ID_AS_STRING);
        // Delete should always be called with questionId as null, because question drafts don't have an id. They will get it at the
        // time the question is sent
        verify(arendeDraftService).delete(CERTIFICATE_ID, null);
    }
}
