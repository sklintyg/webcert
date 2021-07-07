/*
 * Copyright (C) 2021 Inera AB (http://www.inera.se)
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

import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import se.inera.intyg.common.support.facade.model.question.Question;
import se.inera.intyg.webcert.web.service.arende.ArendeService;

@Service
public class GetQuestionsFacadeServiceImpl implements GetQuestionsFacadeService {

    private final ArendeService arendeService;

    @Autowired
    public GetQuestionsFacadeServiceImpl(ArendeService arendeService) {
        this.arendeService = arendeService;
    }

    @Override
    public Question[] getQuestions(String certificateId) {
        final var arendenInternal = arendeService.getArendenInternal(certificateId);
        return arendenInternal.stream()
            .map(arende ->
                Question.builder()
                    .id(String.valueOf(arende.getId()))
                    .build()
            )
            .collect(Collectors.toList())
            .toArray(new Question[0]);
    }
}
