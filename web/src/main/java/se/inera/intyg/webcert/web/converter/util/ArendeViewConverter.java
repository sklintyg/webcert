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

import static se.inera.certificate.modules.fkparent.model.converter.RespConstants.GRUNDFORMEDICINSKTUNDERLAG_SVAR_ID_1;
import static se.inera.certificate.modules.fkparent.model.converter.RespConstants.TILLAGGSFRAGOR_SVAR_JSON_ID;

import java.util.*;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.google.common.base.Throwables;

import se.inera.certificate.modules.fkparent.model.converter.RespConstants;
import se.inera.intyg.common.support.model.common.internal.Utlatande;
import se.inera.intyg.common.support.modules.registry.IntygModuleRegistryImpl;
import se.inera.intyg.common.support.modules.registry.ModuleNotFoundException;
import se.inera.intyg.common.support.modules.support.api.ModuleApi;
import se.inera.intyg.common.support.modules.support.api.exception.ModuleException;
import se.inera.intyg.webcert.persistence.arende.model.Arende;
import se.inera.intyg.webcert.persistence.arende.model.MedicinsktArende;
import se.inera.intyg.webcert.web.service.intyg.IntygServiceImpl;
import se.inera.intyg.webcert.web.service.intyg.dto.IntygContentHolder;
import se.inera.intyg.webcert.web.web.controller.api.dto.ArendeView;
import se.inera.intyg.webcert.web.web.controller.api.dto.ArendeView.ArendeType;
import se.inera.intyg.webcert.web.web.controller.api.dto.MedicinsktArendeView;

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
        template.setEnhetsnamn(arende.getEnhet());
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
        template.setSigneratAv(arende.getSigneratAv());
        template.setVidarebefordrad(arende.getVidarebefordrad());
        template.setVardaktorNamn(arende.getVardaktorName());

        return template.build();
    }

    private List<MedicinsktArendeView> convertToMedicinsktArendeView(List<MedicinsktArende> medicinskaArenden, String intygsId, String intygsTyp) {
        List<MedicinsktArendeView> medicinskaArendenViews = new ArrayList<>();
        for (MedicinsktArende arende : medicinskaArenden) {
            try {
                String jsonPropertyHandle = getJsonPropertyHandle(arende, intygsId, intygsTyp);
                Integer position = getListPositionForInstanceId(arende);
                MedicinsktArendeView view = MedicinsktArendeView.builder().setFrageId(arende.getFrageId()).setInstans(arende.getInstans())
                        .setText(arende.getText()).setPosition(position).setJsonPropertyHandle(jsonPropertyHandle).build();
                medicinskaArendenViews.add(view);

            } catch (ModuleNotFoundException | ModuleException e) {
                LOG.error("Module not found for certificate of type {}", intygsTyp);
                Throwables.propagate(e);
            }
        }
        return medicinskaArendenViews;
    }

    private String getJsonPropertyHandle(MedicinsktArende arende, String intygsId, String intygsTyp)
            throws ModuleNotFoundException, ModuleException {
        ModuleApi moduleApi = moduleRegistry.getModuleApi(intygsTyp);
        IntygContentHolder content = intygService.fetchIntygData(intygsId, intygsTyp);
        Utlatande utlatande = content.getUtlatande();
        String frageId = arende.getFrageId();

        if (isTillaggsFraga(frageId)) {
            return TILLAGGSFRAGOR_SVAR_JSON_ID;
        }
        switch (frageId) {
        case GRUNDFORMEDICINSKTUNDERLAG_SVAR_ID_1:
            return calculateFrageIdHandleForGrundForMU(arende, intygsTyp, utlatande, moduleApi);
        default:
            return RespConstants.getJsonPropertyFromFrageId(frageId);
        }
    }

    private boolean isTillaggsFraga(String frageId) {
        try {
            if (StringUtils.isNumeric(frageId) && Integer.parseInt(frageId) >= TILLAGGSFRAGA_START) {
                return true;
            }
            return false;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    private String calculateFrageIdHandleForGrundForMU(MedicinsktArende arende, String intygsTyp, Utlatande utlatande, ModuleApi moduleApi) {
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
        if (arende.getPaminnelseMeddelandeId() != null) {
            return ArendeType.PAMINNELSE;
        } else if (arende.getSvarPaId() != null) {
            return ArendeType.SVAR;
        } else {
            return ArendeType.FRAGA;
        }
    }
}
