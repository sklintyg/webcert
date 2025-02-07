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
package se.inera.intyg.webcert.web.service.facade.question.impl;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import se.inera.intyg.common.fk7263.support.Fk7263EntryPoint;
import se.inera.intyg.common.support.facade.model.question.Question;
import se.inera.intyg.webcert.web.service.facade.question.GetQuestionsFacadeService;
import se.inera.intyg.webcert.web.service.intyg.IntygService;

@Service(value = "GetQuestionsFacadeServiceImpl")
public class GetQuestionsFacadeServiceImpl implements GetQuestionsFacadeService {

    private final IntygService intygService;
    private final GetQuestionsFacadeService arendeToQuestionFacadeService;
    private final GetQuestionsFacadeService fragaSvarToQuestionFacadeService;

    @Autowired
    public GetQuestionsFacadeServiceImpl(
        IntygService intygService,
        @Qualifier("ArendeToQuestionFacadeService") GetQuestionsFacadeService arendeToQuestionFacadeService,
        @Qualifier("FragaSvarToQuestionFacadeService") GetQuestionsFacadeService fragaSvarToQuestionFacadeService) {
        this.intygService = intygService;
        this.arendeToQuestionFacadeService = arendeToQuestionFacadeService;
        this.fragaSvarToQuestionFacadeService = fragaSvarToQuestionFacadeService;
    }

    @Override
    public List<Question> getQuestions(String certificateId) {
        if (useFragaSvarToQuestion(certificateId)) {
            return fragaSvarToQuestionFacadeService.getQuestions(certificateId);
        }
        return arendeToQuestionFacadeService.getQuestions(certificateId);
    }

    @Override
    public List<Question> getComplementQuestions(String certificateId) {
        if (useFragaSvarToQuestion(certificateId)) {
            return fragaSvarToQuestionFacadeService.getComplementQuestions(certificateId);
        }
        return arendeToQuestionFacadeService.getComplementQuestions(certificateId);
    }

    private boolean useFragaSvarToQuestion(String certificateId) {
        return Fk7263EntryPoint.MODULE_ID.equals(
            intygService.getIntygTypeInfo(certificateId).getIntygType()
        );
    }
}
