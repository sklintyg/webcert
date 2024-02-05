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
package se.inera.intyg.webcert.web.converter;

import com.google.common.collect.ImmutableList;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.apache.commons.collections.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import se.inera.intyg.common.support.model.common.internal.Utlatande;
import se.inera.intyg.common.support.modules.registry.IntygModuleRegistryImpl;
import se.inera.intyg.common.support.modules.registry.ModuleNotFoundException;
import se.inera.intyg.common.support.modules.support.api.ModuleApi;
import se.inera.intyg.common.support.modules.support.api.exception.ModuleException;
import se.inera.intyg.webcert.persistence.arende.model.Arende;
import se.inera.intyg.webcert.persistence.arende.model.ArendeAmne;
import se.inera.intyg.webcert.persistence.arende.model.ArendeDraft;
import se.inera.intyg.webcert.persistence.arende.model.MedicinsktArende;
import se.inera.intyg.webcert.web.converter.util.AnsweredWithIntygUtil;
import se.inera.intyg.webcert.web.service.fragasvar.dto.FrageStallare;
import se.inera.intyg.webcert.web.service.intyg.IntygService;
import se.inera.intyg.webcert.web.web.controller.api.dto.AnsweredWithIntyg;
import se.inera.intyg.webcert.web.web.controller.api.dto.ArendeConversationView;
import se.inera.intyg.webcert.web.web.controller.api.dto.ArendeView;
import se.inera.intyg.webcert.web.web.controller.api.dto.ArendeView.ArendeType;
import se.inera.intyg.webcert.web.web.controller.api.dto.MedicinsktArendeView;

@Component
@RequiredArgsConstructor
public class ArendeViewConverter {

    private static final Logger LOG = LoggerFactory.getLogger(ArendeViewConverter.class);

    private IntygModuleRegistryImpl moduleRegistry;
    private IntygService intygService;

    private static String getThreadRootMessageId(Arende arende) {
        String referenceId = (arende.getSvarPaId() != null) ? arende.getSvarPaId() : arende.getPaminnelseMeddelandeId();
        return (referenceId != null) ? referenceId : arende.getMeddelandeId();
    }

    private static boolean conversationContainsFraga(List<Arende> thread) {
        return thread.stream().anyMatch(a -> getArendeType(a) == ArendeType.FRAGA);
    }

    public static ArendeType getArendeType(Arende arende) {
        if (ArendeAmne.PAMINN == arende.getAmne()) {
            return ArendeType.PAMINNELSE;
        } else if (arende.getSvarPaId() != null) {
            return ArendeType.SVAR;
        } else {
            return ArendeType.FRAGA;
        }
    }

    public ArendeView convertToDto(Arende arende) {
        if (arende == null) {
            return null;
        }
        return ArendeView.builder()
            .amne(arende.getAmne())
            .arendeType(getArendeType(arende))
            .enhetsnamn(arende.getEnhetName())
            .frageStallare(arende.getSkickatAv())
            .internReferens(arende.getMeddelandeId())
            .intygId(arende.getIntygsId())
            .meddelande(arende.getMeddelande())
            .meddelandeRubrik(arende.getRubrik())
            .paminnelseMeddelandeId(arende.getPaminnelseMeddelandeId())
            .sistaDatumForSvar(arende.getSistaDatumForSvar())
            .status(arende.getStatus())
            .timestamp(arende.getTimestamp())
            .signeratAv(arende.getSigneratAvName())
            .vidarebefordrad(arende.getVidarebefordrad())
            .vardaktorNamn(arende.getVardaktorName())
            .svarSkickadDatum(arende.getSkickatTidpunkt())
            .vardgivarnamn(arende.getVardgivareName())
            .externaKontakter(ImmutableList.copyOf(arende.getKontaktInfo()))
            .kompletteringar(
                ImmutableList.copyOf(
                    convertToMedicinsktArendeView(
                        arende.getKomplettering(),
                        arende.getIntygsId(),
                        arende.getIntygTyp()
                    )
                )
            )
            .svarPaId(arende.getSvarPaId())
            .build();
    }

    public ArendeConversationView convertToArendeConversationView(Arende fraga, Arende svar, AnsweredWithIntyg komplt,
        List<Arende> paminnelser, String draftText) {
        return ArendeConversationView.builder()
            .fraga(convertToDto(fraga))
            .svar(convertToDto(svar))
            .answeredWithIntyg(komplt)
            .paminnelser(
                paminnelser.stream()
                    .map(this::convertToDto)
                    .sorted(Comparator.comparing(ArendeView::getTimestamp).reversed())
                    .collect(Collectors.collectingAndThen(Collectors.toList(), ImmutableList::copyOf)))
            .senasteHandelse(fraga.getSenasteHandelse())
            .draftText(draftText)
            .build();
    }

    /**
     * Creates a list of ArendeConversationView for the given list of messages (Arende), for the specific intyg. All
     * messages will get grouped together in pairs of question-answer where such relations exist. If the specified intyg
     * has been answered with another intyg, that information will be added to the ArendeConversationView.
     *
     * Note that all Arende must belong to the same intyg.
     *
     * @param intygsId the id of the intyg to which all the messages belong to
     * @param intygMessages a list of messages (Arende) relating to the same intyg
     * @param kompltToIntyg a list of kompletterande intyg for the given intyg, if any (empty list is allowed, null is not)
     * @return A list of ArendeConversationView meant for frontend consumption or undefined if messages are not for the
     * same intyg
     */
    public List<ArendeConversationView> buildArendeConversations(String intygsId, List<Arende> intygMessages,
        List<AnsweredWithIntyg> kompltToIntyg, List<ArendeDraft> arendeDrafts) {
        Objects.requireNonNull(kompltToIntyg);
        Objects.requireNonNull(intygMessages);
        // Group by conversation thread.
        Map<String, List<Arende>> threads = intygMessages.stream()
            .collect(Collectors.groupingBy(ArendeViewConverter::getThreadRootMessageId));

        List<ArendeConversationView> arendeConversations = threads.values().stream()
            .filter(ArendeViewConverter::conversationContainsFraga)
            .map(conversation -> createConversationViewFromArendeList(conversation, kompltToIntyg, arendeDrafts))
            .collect(Collectors.toList());

        Collections.sort(arendeConversations, (a, b) -> {
            boolean aIsEmpty = a.getPaminnelser().isEmpty();
            boolean bIsEmpty = b.getPaminnelser().isEmpty();
            if (aIsEmpty == bIsEmpty) {
                return b.getSenasteHandelse().compareTo(a.getSenasteHandelse());
            } else if (aIsEmpty) {
                return 1;
            } else {
                return -1;
            }
        });
        return arendeConversations;
    }

    private ArendeConversationView createConversationViewFromArendeList(List<Arende> messagesInThread,
        List<AnsweredWithIntyg> kompltForIntyg, List<ArendeDraft> arendeDrafts) {
        Optional<Arende> fraga = messagesInThread.stream()
            .filter(a -> getArendeType(a) == ArendeType.FRAGA)
            .reduce((element, otherElement) -> {
                throw new IllegalArgumentException("More than 1 fraga found.");
            });
        Optional<Arende> svar = messagesInThread.stream()
            .filter(a -> getArendeType(a) == ArendeType.SVAR)
            .reduce((element, otherElement) -> {
                throw new IllegalArgumentException("More than 1 svar found.");
            });
        List<Arende> paminnelser = messagesInThread.stream()
            .filter(a -> getArendeType(a) == ArendeType.PAMINNELSE)
            .collect(Collectors.toList());

        if (!fraga.isPresent()) {
            throw new IllegalArgumentException("No fraga found for the given message thread.");
        }

        String draftText = null;
        if (!svar.isPresent() && !FrageStallare.WEBCERT.getKod().equals(fraga.get().getSkickatAv())) {
            draftText = arendeDrafts.stream()
                .filter(d -> d.getQuestionId().equals(fraga.get().getMeddelandeId()))
                .findAny()
                .map(ArendeDraft::getText)
                .orElse(null);
        }

        // Find oldest intyg among kompletterande intyg, that's newer than the fraga
        AnsweredWithIntyg komplt = null;
        if (!svar.isPresent() && fraga.get().getAmne() == ArendeAmne.KOMPLT) {
            komplt = AnsweredWithIntygUtil.returnOldestKompltOlderThan(fraga.get().getTimestamp(), kompltForIntyg);
        }
        return convertToArendeConversationView(fraga.get(), svar.orElse(null), komplt, paminnelser, draftText);
    }

    private List<MedicinsktArendeView> convertToMedicinsktArendeView(List<MedicinsktArende> medicinskaArenden, String intygsId,
        String intygsTyp) {
        List<MedicinsktArendeView> medicinskaArendenViews = new ArrayList<>();
        if (CollectionUtils.isEmpty(medicinskaArenden)) {
            return medicinskaArendenViews;
        }
        List<String> frageIds = medicinskaArenden.stream().map(MedicinsktArende::getFrageId).distinct().collect(Collectors.toList());

        Utlatande utlatande = intygService.fetchIntygData(intygsId, intygsTyp).getUtlatande();
        ModuleApi moduleApi;

        try {
            moduleApi = moduleRegistry.getModuleApi(intygsTyp, utlatande.getTextVersion());
        } catch (ModuleNotFoundException e) {
            LOG.error("Module not found for certificate of type {}", intygsTyp);
            throw new RuntimeException(e);
        }

        Map<String, List<String>> arendeParameters = null;
        try {
            arendeParameters = moduleApi.getModuleSpecificArendeParameters(utlatande, frageIds);
        } catch (ModuleException e) {
            throw new IllegalArgumentException(e);
        }
        for (MedicinsktArende arende : medicinskaArenden) {
            Integer position = getListPositionForInstanceId(arende);
            String jsonPropertyHandle = getJsonPropertyHandle(arende, position, arendeParameters);
            MedicinsktArendeView view = MedicinsktArendeView.builder()
                .frageId(arende.getFrageId())
                .instans(arende.getInstans())
                .text(arende.getText())
                .position(Math.max(position - 1, 0))
                .jsonPropertyHandle(jsonPropertyHandle)
                .build();
            medicinskaArendenViews.add(view);
        }
        return medicinskaArendenViews;
    }

    private String getJsonPropertyHandle(MedicinsktArende arende, Integer position, Map<String, List<String>> arendeParameters) {
        List<String> filledPositions = arendeParameters.get(arende.getFrageId());
        if (CollectionUtils.isNotEmpty(filledPositions)) {
            return filledPositions.get(position < filledPositions.size() ? position : 0);
        }
        LOG.error(
            "The supplied Arende information for conversion to json parameters for Fraga {} must be a intygMessages of Strings.",
            arende.getFrageId()
        );
        return "";
    }

    private int getListPositionForInstanceId(MedicinsktArende arende) {
        Integer instanceId = arende.getInstans();
        return instanceId != null && instanceId > 0 ? instanceId : 0;
    }
}
