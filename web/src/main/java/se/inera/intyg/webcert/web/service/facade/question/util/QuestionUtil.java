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
package se.inera.intyg.webcert.web.service.facade.question.util;

import java.util.function.Predicate;
import se.inera.intyg.common.support.facade.model.question.QuestionType;
import se.inera.intyg.webcert.persistence.arende.model.Arende;
import se.inera.intyg.webcert.persistence.arende.model.ArendeAmne;
import se.inera.intyg.webcert.persistence.arende.model.ArendeDraft;

public final class QuestionUtil {

    private QuestionUtil() {
    }

    public static QuestionType getTypeFromAmneAsString(String amne) {
        if (amne == null || amne.isBlank()) {
            return QuestionType.MISSING;
        }
        final var arendeAmne = ArendeAmne.valueOf(amne);
        return getType(arendeAmne);
    }

    public static QuestionType getType(ArendeAmne arendeAmne) {
        switch (arendeAmne) {
            case AVSTMN:
                return QuestionType.COORDINATION;
            case KONTKT:
                return QuestionType.CONTACT;
            case OVRIGT:
                return QuestionType.OTHER;
            case KOMPLT:
                return QuestionType.COMPLEMENT;
            default:
                throw new IllegalArgumentException("The type is not supported: " + arendeAmne);
        }
    }

    public static String getSubject(Arende arende) {
        final var subjectBuilder = new StringBuilder();
        subjectBuilder.append(arende.getAmne().getDescription());
        if (arende.getRubrik() != null && !arende.getRubrik().isBlank()) {
            subjectBuilder.append(" - ");
            subjectBuilder.append(arende.getRubrik());
        }
        return subjectBuilder.toString();
    }

    public static String getSubjectAsString(QuestionType type) {
        if (QuestionType.MISSING.equals(type)) {
            return "";
        }

        return getSubject(type).toString();
    }

    public static ArendeAmne getSubject(QuestionType type) {
        switch (type) {
            case COORDINATION:
                return ArendeAmne.AVSTMN;
            case CONTACT:
                return ArendeAmne.KONTKT;
            case OTHER:
                return ArendeAmne.OVRIGT;
            default:
                throw new IllegalArgumentException("Type not supported: " + type);
        }
    }

    public static Predicate<Arende> isQuestion() {
        return arende -> (arende.getSvarPaId() == null || arende.getSvarPaId().isBlank())
            && (arende.getPaminnelseMeddelandeId() == null || arende.getPaminnelseMeddelandeId().isBlank());
    }

    public static Predicate<Arende> isComplementQuestion() {
        return arende -> arende.getAmne() == ArendeAmne.KOMPLT;
    }

    public static Predicate<ArendeDraft> isAnswerDraft() {
        return arendeDraft -> arendeDraft.getQuestionId() != null && !arendeDraft.getQuestionId().isBlank();
    }

    public static Predicate<Arende> isAnswer() {
        return arende -> arende.getSvarPaId() != null && !arende.getSvarPaId().isBlank();
    }

    public static Predicate<Arende> isReminder() {
        return arende -> arende.getPaminnelseMeddelandeId() != null && !arende.getPaminnelseMeddelandeId().isBlank();
    }
}
