/*
 * Copyright (C) 2024 Inera AB (http://www.inera.se)
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

package se.inera.intyg.webcert.web.csintegration.certificate;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import se.inera.intyg.webcert.web.service.fragasvar.dto.FrageStallare;
import se.inera.intyg.webcert.web.web.controller.facade.dto.QuestionDTO;

class FragorOchSvarFactoryTest {

    private FragorOchSvarFactory fragorOchSvarFactory;

    @BeforeEach
    void setUp() {
        fragorOchSvarFactory = new FragorOchSvarFactory();
    }

    @Test
    void shallReturnCorrectAmountOfAnswers() {
        final var question = QuestionDTO.builder()
            .author(FrageStallare.WEBCERT.getKod())
            .build();

        assertEquals(1, fragorOchSvarFactory.calculate(List.of(question)).getAntalSvar());
    }

    @Test
    void shallReturnCorrectAmountOfHandledAnswers() {
        final var question = QuestionDTO.builder()
            .author(FrageStallare.WEBCERT.getKod())
            .isHandled(true)
            .build();

        assertEquals(1, fragorOchSvarFactory.calculate(List.of(question)).getAntalHanteradeSvar());
    }

    @Test
    void shallReturnCorrectAmountOfQuestions() {
        final var question = QuestionDTO.builder()
            .author(FrageStallare.FORSAKRINGSKASSAN.getKod())
            .build();

        assertEquals(1, fragorOchSvarFactory.calculate(List.of(question)).getAntalFragor());
    }

    @Test
    void shallReturnCorrectAmountOfHandledQuestions() {
        final var question = QuestionDTO.builder()
            .author(FrageStallare.FORSAKRINGSKASSAN.getKod())
            .isHandled(true)
            .build();

        assertEquals(1, fragorOchSvarFactory.calculate(List.of(question)).getAntalHanteradeFragor());
    }
}
