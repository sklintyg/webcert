/*
 * Copyright (C) 2016 Inera AB (http://www.inera.se)
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
package se.inera.intyg.webcert.web.converter.util;

import static se.inera.intyg.intygstyper.fkparent.model.converter.RespConstants.GRUNDFORMEDICINSKTUNDERLAG_SVAR_ID_1;
import static se.inera.intyg.intygstyper.fkparent.model.converter.RespConstants.TILLAGGSFRAGOR_SVAR_JSON_ID;

import java.util.*;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.google.common.base.Throwables;

import se.inera.intyg.common.support.model.common.internal.Utlatande;
import se.inera.intyg.common.support.modules.registry.IntygModuleRegistryImpl;
import se.inera.intyg.common.support.modules.registry.ModuleNotFoundException;
import se.inera.intyg.common.support.modules.support.api.ModuleApi;
import se.inera.intyg.intygstyper.fkparent.model.converter.RespConstants;
import se.inera.intyg.webcert.persistence.arende.model.*;
import se.inera.intyg.webcert.web.service.intyg.IntygServiceImpl;
import se.inera.intyg.webcert.web.web.controller.api.dto.*;
import se.inera.intyg.webcert.web.web.controller.api.dto.ArendeView.ArendeType;

@Component
public class ArendeViewConverter {
    private static final Logger LOG = LoggerFactory.getLogger(ArendeViewConverter.class);
    private static final int TILLAGGSFRAGA_START = 9000;

    @Autowired
    private IntygModuleRegistryImpl moduleRegistry;

    @Autowired
    private IntygServiceImpl intygService;

    public ArendeView convert(Arende arende) {
        List<MedicinsktArendeView> kompletteringar = convertToMedicinsktArendeView(arende.getKomplettering(), arende.getIntygsId(),
                arende.getIntygTyp());
        ArendeView.Builder template = ArendeView.builder();
        template.setAmne(arende.getAmne());
        template.setArendeType(getArendeType(arende));
        template.setEnhetsnamn(arende.getEnhetName());
        template.setExternaKontakter(arende.getKontaktInfo());
        template.setFrageStallare(arende.getSkickatAv());
        template.setInternReferens(arende.getMeddelandeId());
        template.setIntygId(arende.getIntygsId());
        template.setKompletteringar(kompletteringar);
        template.setMeddelande(arende.getMeddelande());
        template.setMeddelandeRubrik(arende.getRubrik());
        template.setPaminnelseMeddelandeId(arende.getPaminnelseMeddelandeId());
        template.setSistaDatumForSvar(arende.getSistaDatumForSvar());
        template.setStatus(arende.getStatus());
        template.setSvarPaId(arende.getSvarPaId());
        template.setSvarSkickadDatum(arende.getSkickatTidpunkt());
        template.setTimestamp(arende.getTimestamp());
        template.setSigneratAv(arende.getSigneratAvName());
        template.setVidarebefordrad(arende.getVidarebefordrad());
        template.setVardaktorNamn(arende.getVardaktorName());
        template.setVardgivarnamn(arende.getVardgivareName());

        return template.build();
    }

    public ArendeConversationView convertToArendeConversationView(Arende fraga, Arende svar, List<Arende> paminnelser) {
        ArendeView arendeViewQuestion = convert(fraga);
        ArendeView arendeViewAnswer = null;
        if (svar != null) {
            arendeViewAnswer = convert(svar);
        }
        List<ArendeView> arendeViewPaminnelser = new ArrayList<>();
        if (paminnelser != null) {
            arendeViewPaminnelser = paminnelser.stream().map(a -> convert(a)).sorted(Comparator.comparing(ArendeView::getTimestamp).reversed())
                    .collect(Collectors.toList());
        }
        return ArendeConversationView.create(arendeViewQuestion, arendeViewAnswer, fraga.getSenasteHandelse(), arendeViewPaminnelser);
    }

    public List<ArendeConversationView> buildArendeConversations(List<Arende> list) {
        List<ArendeConversationView> arendeConversations = new ArrayList<>();
        Map<String, List<Arende>> threads = new HashMap<>();
        String meddelandeId = null;
        for (Arende arende : list) { // divide into threads
            meddelandeId = getMeddelandeId(arende);
            if (threads.get(meddelandeId) == null) {
                threads.put(meddelandeId, new ArrayList<>());
            }
            threads.get(meddelandeId).add(arende);
        }

        for (String meddelandeIdd : threads.keySet()) {
            List<Arende> arendeConversationContent = threads.get(meddelandeIdd);

            Optional<Arende> fraga = arendeConversationContent.stream().filter(a -> getArendeType(a) == ArendeType.FRAGA).findAny();
            // Since fraga is required to be nonNull by AutoValue_ArendeConversationView need to make sure this is
            // enforced to avoid throwing an exception and showing nothing at all
            if (!fraga.isPresent()) {
                continue;
            }
            Optional<Arende> svar = arendeConversationContent.stream().filter(a -> getArendeType(a) == ArendeType.SVAR).findAny();
            List<Arende> paminnelser = arendeConversationContent.stream().filter(a -> getArendeType(a) == ArendeType.PAMINNELSE)
                    .collect(Collectors.toList());

            arendeConversations.add(convertToArendeConversationView(fraga.get(), svar.orElse(null), paminnelser));
        }
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

    private String getMeddelandeId(Arende arende) {
        String referenceId = (arende.getSvarPaId() != null) ? arende.getSvarPaId() : arende.getPaminnelseMeddelandeId();
        return (referenceId != null) ? referenceId : arende.getMeddelandeId();
    }

    private List<MedicinsktArendeView> convertToMedicinsktArendeView(List<MedicinsktArende> medicinskaArenden, String intygsId, String intygsTyp) {
        List<MedicinsktArendeView> medicinskaArendenViews = new ArrayList<>();
        for (MedicinsktArende arende : medicinskaArenden) {
            String jsonPropertyHandle = getJsonPropertyHandle(arende, intygsId, intygsTyp);
            Integer position = getListPositionForInstanceId(arende);
            MedicinsktArendeView view = MedicinsktArendeView.builder().setFrageId(arende.getFrageId()).setInstans(arende.getInstans())
                    .setText(arende.getText()).setPosition(position).setJsonPropertyHandle(jsonPropertyHandle).build();
            medicinskaArendenViews.add(view);
        }
        return medicinskaArendenViews;
    }

    private String getJsonPropertyHandle(MedicinsktArende arende, String intygsId, String intygsTyp) {
        String frageId = arende.getFrageId();

        if (isTillaggsFraga(frageId)) {
            return TILLAGGSFRAGOR_SVAR_JSON_ID;
        }
        switch (frageId) {
        case GRUNDFORMEDICINSKTUNDERLAG_SVAR_ID_1:
            return calculateFrageIdHandleForGrundForMU(arende, intygsId, intygsTyp);
        default:
            return RespConstants.getJsonPropertyFromFrageId(frageId);
        }
    }

    private boolean isTillaggsFraga(String frageId) {
        try {
            return StringUtils.isNumeric(frageId) && Integer.parseInt(frageId) >= TILLAGGSFRAGA_START;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    private String calculateFrageIdHandleForGrundForMU(MedicinsktArende arende, String intygsId, String intygsTyp) {
        ModuleApi moduleApi = null;
        try {
            moduleApi = moduleRegistry.getModuleApi(intygsTyp);
        } catch (ModuleNotFoundException e) {
            LOG.error("Module not found for certificate of type {}", intygsTyp);
            Throwables.propagate(e);
        }
        Utlatande utlatande = intygService.fetchIntygData(intygsId, intygsTyp, false).getUtlatande();
        Map<String, List<String>> arendeParameters = moduleApi.getModuleSpecificArendeParameters(utlatande);

        List<String> filledPositions = arendeParameters.get(GRUNDFORMEDICINSKTUNDERLAG_SVAR_ID_1);
        if (filledPositions != null) {
            try {
                return filledPositions.get(getListPositionForInstanceId(arende));
            } catch (ClassCastException e) {
                LOG.error("List does not contain string json properties as expected.");
                Throwables.propagate(e);
                return null;
            } catch (IndexOutOfBoundsException e) {
                LOG.error("The instance number in MedicinsktArende must be an integer > 0.");
                return null;
            }
        }
        throw new IllegalArgumentException("The supplied Arende information for conversion to json parameters for Fraga1 must be a list of Strings.");
    }

    private int getListPositionForInstanceId(MedicinsktArende arende) {
        Integer instanceId = arende.getInstans();
        int result = (instanceId != null) ? instanceId : 0;
        return Math.max(result - 1, 0);
    }

    private static ArendeType getArendeType(Arende arende) {
        if (ArendeAmne.PAMINN == arende.getAmne()) {
            return ArendeType.PAMINNELSE;
        } else if (arende.getSvarPaId() != null) {
            return ArendeType.SVAR;
        } else {
            return ArendeType.FRAGA;
        }
    }
}
