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

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import lombok.Getter;
import org.springframework.stereotype.Component;
import se.inera.intyg.common.support.modules.support.api.notification.FragorOchSvar;
import se.inera.intyg.webcert.web.service.fragasvar.dto.FrageStallare;
import se.inera.intyg.webcert.web.web.controller.facade.dto.QuestionDTO;

@Component
public class FragorOchSvarFactory {

    public FragorOchSvar calculate(List<QuestionDTO> questions) {
        final var answersFromCare = new QuestionCount();
        final var questionsFromRecipient = new QuestionCount();

        questions.forEach(question -> {
            if (fromCare(question)) {
                answersFromCare.increment(question.isHandled());
            } else if (fromRecipient(question)) {
                questionsFromRecipient.increment(question.isHandled());
            }
        });

        return new FragorOchSvar(
            questionsFromRecipient.getTotal().get(),
            answersFromCare.getTotal().get(),
            questionsFromRecipient.getHandled().get(),
            answersFromCare.getHandled().get()
        );
    }

    private boolean fromCare(QuestionDTO question) {
        return FrageStallare.WEBCERT.isKodEqual(question.getAuthor());
    }

    private boolean fromRecipient(QuestionDTO question) {
        return FrageStallare.FORSAKRINGSKASSAN.isKodEqual(question.getAuthor());
    }

    @Getter
    private static class QuestionCount {

        private final AtomicInteger total = new AtomicInteger(0);
        private final AtomicInteger handled = new AtomicInteger(0);

        public void increment(boolean isHandled) {
            total.incrementAndGet();
            if (isHandled) {
                handled.incrementAndGet();
            }
        }
    }
}
