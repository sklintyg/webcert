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

package se.inera.intyg.webcert.web.service.facade.question.util;

import se.inera.intyg.common.support.facade.model.question.QuestionType;
import se.inera.intyg.webcert.persistence.arende.model.Arende;
import se.inera.intyg.webcert.persistence.arende.model.ArendeAmne;

public final class QuestionUtil {

    private QuestionUtil() {
    }

    public static QuestionType getType(String amne) {
        final var arendeAmne = ArendeAmne.valueOf(amne);
        switch (arendeAmne) {
            case AVSTMN:
                return QuestionType.COORDINATION;
            case KONTKT:
                return QuestionType.CONTACT;
            case OVRIGT:
                return QuestionType.OTHER;
            default:
                throw new IllegalArgumentException("The type is not yet supported: " + arendeAmne);
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
}
