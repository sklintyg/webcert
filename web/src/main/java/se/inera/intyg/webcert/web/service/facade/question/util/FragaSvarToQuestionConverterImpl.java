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

import static java.util.Comparator.comparing;
import static se.inera.intyg.webcert.web.service.facade.question.util.QuestionUtil.getAuthor;
import static se.inera.intyg.webcert.web.service.facade.question.util.QuestionUtil.getLastUpdate;
import static se.inera.intyg.webcert.web.service.facade.question.util.QuestionUtil.getMessage;
import static se.inera.intyg.webcert.web.service.facade.question.util.QuestionUtil.getSubject;
import static se.inera.intyg.webcert.web.service.facade.question.util.QuestionUtil.getType;

import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.springframework.stereotype.Component;
import se.inera.intyg.common.support.facade.model.CertificateRelationType;
import se.inera.intyg.common.support.facade.model.CertificateStatus;
import se.inera.intyg.common.support.facade.model.metadata.CertificateRelation;
import se.inera.intyg.common.support.facade.model.question.Complement;
import se.inera.intyg.common.support.facade.model.question.Question;
import se.inera.intyg.common.support.facade.model.question.Reminder;
import se.inera.intyg.webcert.persistence.fragasvar.model.Amne;
import se.inera.intyg.webcert.persistence.fragasvar.model.FragaSvar;
import se.inera.intyg.webcert.persistence.fragasvar.model.Komplettering;
import se.inera.intyg.webcert.persistence.model.Status;
import se.inera.intyg.webcert.web.service.facade.GetCertificateFacadeService;

@Component
public class FragaSvarToQuestionConverterImpl implements FragaSvarToQuestionConverter {

    private final GetCertificateFacadeService getCertificateFacadeService;

    public FragaSvarToQuestionConverterImpl(GetCertificateFacadeService getCertificateFacadeService) {
        this.getCertificateFacadeService = getCertificateFacadeService;
    }

    @Override
    public Question convert(FragaSvar fragaSvar) {
        if (fragaSvar == null) {
            return null;
        }

        return Question.builder()
            .id(String.valueOf(fragaSvar.getInternReferens()))
            .author(getAuthor(fragaSvar.getFrageStallare(), fragaSvar.getVardperson().getNamn()))
            .sent(fragaSvar.getFrageSkickadDatum())
            .lastUpdate(getLastUpdate(fragaSvar))
            .message(getMessage(fragaSvar))
            .subject(getSubject(fragaSvar))
            .type(getType(fragaSvar.getAmne()))
            .isHandled(fragaSvar.getStatus() == Status.CLOSED)
            .isForwarded(fragaSvar.getVidarebefordrad())
            .complements(new Complement[0])
            .reminders(new Reminder[0])
            .answeredByCertificate(
                fragaSvar.getAmne() == Amne.KOMPLETTERING_AV_LAKARINTYG ? getAnsweredByCertificate(fragaSvar,
                    getAnswersByCertificate(fragaSvar.getIntygsReferens().getIntygsId(),
                        fragaSvar.getKompletteringar())) : null)
            .certificateId(fragaSvar.getIntygsReferens().getIntygsId())
            .build();
    }

    private List<CertificateRelation> getAnswersByCertificate(String certificateId, Set<Komplettering> kompletteringSet) {
        if (kompletteringSet.isEmpty()) {
            return Collections.emptyList();
        }

        final var certificateRelations = getCertificateFacadeService
            .getCertificate(certificateId, false, true)
            .getMetadata()
            .getRelations();
        if (certificateRelations == null) {
            return Collections.emptyList();
        }

        final var childrenRelations = certificateRelations.getChildren();
        if (childrenRelations == null) {
            return Collections.emptyList();
        }

        return Stream.of(childrenRelations)
            .filter(childRelation -> childRelation.getType() == CertificateRelationType.COMPLEMENTED
                && childRelation.getStatus() != CertificateStatus.REVOKED)
            .collect(Collectors.toList());
    }

    private CertificateRelation getAnsweredByCertificate(FragaSvar question, List<CertificateRelation> answersByCertificate) {
        return answersByCertificate.stream()
            .filter(certificateRelation -> certificateRelation.getCreated().isAfter(question.getSvarSkickadDatum()))
            .min(comparing(CertificateRelation::getCreated))
            .orElse(null);
    }
}
