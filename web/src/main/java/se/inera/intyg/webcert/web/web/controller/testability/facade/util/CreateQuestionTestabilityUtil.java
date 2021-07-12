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

package se.inera.intyg.webcert.web.web.controller.testability.facade.util;

import java.time.LocalDateTime;
import java.util.UUID;
import javax.validation.constraints.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import se.inera.intyg.common.support.facade.model.question.QuestionType;
import se.inera.intyg.webcert.persistence.arende.model.Arende;
import se.inera.intyg.webcert.persistence.arende.model.ArendeAmne;
import se.inera.intyg.webcert.persistence.arende.model.ArendeDraft;
import se.inera.intyg.webcert.persistence.arende.repository.ArendeDraftRepository;
import se.inera.intyg.webcert.persistence.arende.repository.ArendeRepository;
import se.inera.intyg.webcert.persistence.model.Status;
import se.inera.intyg.webcert.persistence.utkast.model.Utkast;
import se.inera.intyg.webcert.web.service.utkast.UtkastService;
import se.inera.intyg.webcert.web.web.controller.testability.facade.dto.CreateQuestionRequestDTO;

@Component
public class CreateQuestionTestabilityUtil {

    private final UtkastService utkastService;
    private final ArendeRepository arendeRepository;
    private final ArendeDraftRepository arendeDraftRepository;

    @Autowired
    public CreateQuestionTestabilityUtil(UtkastService utkastService,
        ArendeRepository arendeRepository, ArendeDraftRepository arendeDraftRepository) {
        this.utkastService = utkastService;
        this.arendeRepository = arendeRepository;
        this.arendeDraftRepository = arendeDraftRepository;
    }

    public String createNewQuestion(@NotNull String certificateId, CreateQuestionRequestDTO createQuestionRequest) {
        final var draft = utkastService.getDraft(certificateId, false);
        final var arende = createArende(createQuestionRequest, draft);
        arendeRepository.save(arende);
        return arende.getMeddelandeId();
    }

    public String createNewQuestionDraft(String certificateId, CreateQuestionRequestDTO createQuestionRequest) {
        final var questionDraft = new ArendeDraft();
        questionDraft.setText(createQuestionRequest.getMessage());
        questionDraft.setAmne(getAmne(createQuestionRequest.getType()));
        questionDraft.setIntygId(certificateId);
        final var createdQuestionDraft = arendeDraftRepository.save(questionDraft);
        return Long.toString(createdQuestionDraft.getId());
    }

    private String getAmne(QuestionType type) {
        switch (type) {
            case COORDINATION:
                return ArendeAmne.AVSTMN.toString();
            case CONTACT:
                return ArendeAmne.KONTKT.toString();
            case OTHER:
                return ArendeAmne.OVRIGT.toString();
            default:
                throw new IllegalArgumentException("Type not supported: " + type);
        }
    }

    private Arende createArende(CreateQuestionRequestDTO createQuestionRequest, Utkast draft) {
        final var arende = new Arende();
        arende.setIntygsId(draft.getIntygsId());
        arende.setIntygTyp(draft.getIntygsTyp());
        arende.setMeddelandeId(UUID.randomUUID().toString());

        arende.setPatientPersonId(draft.getPatientPersonnummer().getPersonnummer());

        arende.setSigneratAv(draft.getSkapadAv().getHsaId());
        arende.setSigneratAvName(draft.getSkapadAv().getNamn());

        arende.setTimestamp(LocalDateTime.now());
        arende.setSkickatTidpunkt(LocalDateTime.now());
        arende.setSenasteHandelse(LocalDateTime.now());

        arende.setAmne(getSubject(createQuestionRequest.getType()));
        // TODO: Don't have to set subject. No subject will use the "Amne-description" by default
//        arende.setRubrik(arende.getAmne().getDescription());

        // TODO: No need to set reference. Can probably skip this completely.
//        arende.setReferensId("referens");
        arende.setMeddelande(createQuestionRequest.getMessage());

        arende.setEnhetId(draft.getEnhetsId());
        arende.setEnhetName(draft.getEnhetsNamn());
        arende.setVardgivareName(draft.getVardgivarNamn());

        arende.setVidarebefordrad(false);

        arende.setStatus(Status.PENDING_INTERNAL_ACTION);

        // TODO: When receiving a question, the skickadAv is FK. If sending a question the skickadAv is WC.
        // TODO: When FK then no vardaktorName. When WC then it is the sending persons name.
        arende.setSkickatAv("FK");
        // arende.setVardaktorName(draft.getSkapadAv().getNamn());

        return arende;
    }

    private ArendeAmne getSubject(QuestionType type) {
        switch (type) {
            case COORDINATION:
                return ArendeAmne.AVSTMN;
            case CONTACT:
                return ArendeAmne.KONTKT;
            case OTHER:
            default:
                return ArendeAmne.OVRIGT;
        }
    }
}
