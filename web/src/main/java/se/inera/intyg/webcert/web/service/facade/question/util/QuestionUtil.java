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
package se.inera.intyg.webcert.web.service.facade.question.util;

import java.time.LocalDateTime;
import java.util.function.Predicate;
import se.inera.intyg.common.support.facade.model.question.QuestionType;
import se.inera.intyg.webcert.persistence.arende.model.Arende;
import se.inera.intyg.webcert.persistence.arende.model.ArendeAmne;
import se.inera.intyg.webcert.persistence.arende.model.ArendeDraft;
import se.inera.intyg.webcert.persistence.fragasvar.model.Amne;
import se.inera.intyg.webcert.persistence.fragasvar.model.FragaSvar;
import se.inera.intyg.webcert.persistence.fragasvar.model.Komplettering;

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

    public static QuestionType getType(Amne amne) {
        if (amne == null) {
            return null;
        }
        switch (amne) {
            case AVSTAMNINGSMOTE:
            case PAMINNELSE:
            case ARBETSTIDSFORLAGGNING:
            case MAKULERING_AV_LAKARINTYG:
                return QuestionType.COORDINATION;
            case KONTAKT:
                return QuestionType.CONTACT;
            case OVRIGT:
                return QuestionType.OTHER;
            case KOMPLETTERING_AV_LAKARINTYG:
                return QuestionType.COMPLEMENT;
            default:
                throw new IllegalArgumentException("The type is not supported: " + amne);
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

    public static String getSubject(FragaSvar fragaSvar) {
        if (fragaSvar.getAmne() == null) {
            return null;
        }
        final var subjectBuilder = new StringBuilder();
        subjectBuilder.append(getSubjectAsString(fragaSvar.getAmne()));
        if (fragaSvar.getMeddelandeRubrik() != null && !fragaSvar.getMeddelandeRubrik().isBlank()) {
            subjectBuilder.append(" - ");
            subjectBuilder.append(fragaSvar.getMeddelandeRubrik());
        }
        return subjectBuilder.toString();
    }

    public static String getMessage(FragaSvar fragaSvar) {
        final var frageText = fragaSvar.getFrageText();
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append(frageText);

        final var kompletteringar = fragaSvar.getKompletteringar();
        if (kompletteringar == null || kompletteringar.isEmpty()) {
            return stringBuilder.toString();
        }

        for (Komplettering komplettering : kompletteringar) {
            stringBuilder.append("\n\n").append(komplettering.getFalt()).append("\n\n").append(komplettering.getText());
        }

        return stringBuilder.toString();
    }

    private static String getSubjectAsString(Amne amne) {
        switch (amne) {
            case KOMPLETTERING_AV_LAKARINTYG:
                return "Komplettering";
            case MAKULERING_AV_LAKARINTYG:
                return "Makulering";
            case AVSTAMNINGSMOTE:
                return "Avstämningsmöte";
            case KONTAKT:
                return "Kontakt";
            case ARBETSTIDSFORLAGGNING:
                return "Arbetstidsförläggning";
            case PAMINNELSE:
                return "Påminnelse";
            case OVRIGT:
                return "Övrigt";
            default:
                throw new IllegalArgumentException("The type is not supported: " + amne);
        }
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

    public static String getAuthor(String frageStallare, String vardperson) {
        if (frageStallare == null) {
            return null;
        }
        switch (frageStallare) {
            case "FK":
                return "Försäkringskassan";
            case "WC":
                return vardperson;
            default:
                return null;
        }
    }

    public static LocalDateTime getLastUpdate(FragaSvar fragaSvar) {
        if (fragaSvar.getSvarSkickadDatum() != null) {
            return fragaSvar.getSvarSkickadDatum();
        } else {
            return fragaSvar.getFrageSkickadDatum();
        }
    }
}
