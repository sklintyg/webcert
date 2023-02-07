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
package se.inera.intyg.webcert.web.service.facade.question.impl;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import se.inera.intyg.common.support.facade.model.question.Question;
import se.inera.intyg.webcert.web.service.facade.question.GetQuestionsFacadeService;

@Service(value = "GetQuestionsFacadeServiceImpl")
public class GetQuestionsFacadeServiceImpl implements GetQuestionsFacadeService {

    private final ArendeToQuestionFacadeServiceImpl arendeToQuestionFacadeService;

    private final FragaSvarToQuestionFacadeServiceImpl fragaSvarToQuestionFacadeService;

    @Autowired
    public GetQuestionsFacadeServiceImpl(ArendeToQuestionFacadeServiceImpl arendeToQuestionFacadeService,
        FragaSvarToQuestionFacadeServiceImpl fragaSvarToQuestionFacadeService) {
        this.arendeToQuestionFacadeService = arendeToQuestionFacadeService;
        this.fragaSvarToQuestionFacadeService = fragaSvarToQuestionFacadeService;
    }

    @Override
    public List<Question> getComplementQuestions(String certificateId) {
        final var complementQuestions = arendeToQuestionFacadeService.getComplementQuestions(certificateId);
        complementQuestions.addAll(fragaSvarToQuestionFacadeService.getComplementQuestions(certificateId));
        return complementQuestions;
    }

    @Override
    public List<Question> getQuestions(String certificateId) {
        var questions = arendeToQuestionFacadeService.getQuestions(certificateId);
        questions.addAll(fragaSvarToQuestionFacadeService.getQuestions(certificateId));
        return questions;
    }
}
