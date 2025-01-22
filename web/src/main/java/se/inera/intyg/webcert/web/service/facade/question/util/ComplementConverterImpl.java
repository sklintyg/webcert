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

import com.google.common.collect.ImmutableMap;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.apache.commons.collections.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import se.inera.intyg.common.services.texts.CertificateTextProvider;
import se.inera.intyg.common.support.facade.model.question.Complement;
import se.inera.intyg.common.support.model.common.internal.Utlatande;
import se.inera.intyg.common.support.modules.registry.IntygModuleRegistry;
import se.inera.intyg.common.support.modules.registry.ModuleNotFoundException;
import se.inera.intyg.common.support.modules.support.api.ModuleApi;
import se.inera.intyg.common.support.modules.support.api.exception.ModuleException;
import se.inera.intyg.webcert.common.service.exception.WebCertServiceErrorCodeEnum;
import se.inera.intyg.webcert.common.service.exception.WebCertServiceException;
import se.inera.intyg.webcert.persistence.arende.model.Arende;
import se.inera.intyg.webcert.persistence.arende.model.MedicinsktArende;
import se.inera.intyg.webcert.web.service.certificate.GetCertificateService;

@Component
public class ComplementConverterImpl implements ComplementConverter {

    private final GetCertificateService getCertificateService;
    private final IntygModuleRegistry intygModuleRegistry;

    private static final Logger LOG = LoggerFactory.getLogger(ComplementConverterImpl.class);

    @Autowired
    public ComplementConverterImpl(GetCertificateService getCertificateService, IntygModuleRegistry intygModuleRegistry) {
        this.getCertificateService = getCertificateService;
        this.intygModuleRegistry = intygModuleRegistry;
    }

    @Override
    public Complement[] convert(Arende complementQuestion) {
        final var certificateAsUtlatande = getCertificateService.getCertificateAsUtlatande(
            complementQuestion.getIntygsId(), complementQuestion.getIntygTyp()
        );

        final var certificateTextProvider = getCertificateTextProvider(certificateAsUtlatande);

        return getComplements(complementQuestion, certificateAsUtlatande, certificateTextProvider);
    }

    @Override
    public Map<String, Complement[]> convert(List<Arende> complementQuestions) {
        final var firstComplementQuestion = complementQuestions.stream().findFirst().orElseThrow();

        final var certificateAsUtlatande = getCertificateService.getCertificateAsUtlatande(
            firstComplementQuestion.getIntygsId(), firstComplementQuestion.getIntygTyp()
        );

        final var certificateTextProvider = getCertificateTextProvider(certificateAsUtlatande);

        return complementQuestions.stream()
            .map(complementQuestion ->
                Map.of(
                    complementQuestion.getMeddelandeId(),
                    getComplements(complementQuestion, certificateAsUtlatande, certificateTextProvider)
                )
            )
            .reduce((complementMap, complementMap2) ->
                ImmutableMap.<String, Complement[]>builder()
                    .putAll(complementMap)
                    .putAll(complementMap2)
                    .build()
            )
            .orElse(Collections.emptyMap());
    }

    private Complement[] getComplements(Arende complementQuestion, Utlatande certificateAsUtlatande,
        CertificateTextProvider certificateTextProvider) {
        final var questionIds = complementQuestion.getKomplettering().stream()
            .map(MedicinsktArende::getFrageId)
            .distinct()
            .collect(Collectors.toList());

        final var jsonPropertiesMap = getJsonPropertiesMap(certificateAsUtlatande, questionIds);

        return complementQuestion.getKomplettering().stream()
            .map(complement ->
                Complement.builder()
                    .questionId(complement.getFrageId())
                    .questionText(certificateTextProvider.get(complement.getFrageId()))
                    .valueId(getValueId(complement, jsonPropertiesMap))
                    .message(complement.getText())
                    .build()
            )
            .toArray(Complement[]::new);
    }

    private String getValueId(MedicinsktArende medicinsktArende, Map<String, List<String>> arendeParameters) {
        return getJsonProperty(
            medicinsktArende,
            getListPositionForInstanceId(medicinsktArende),
            arendeParameters
        );
    }

    private int getListPositionForInstanceId(MedicinsktArende medicinsktArende) {
        final var instanceId = medicinsktArende.getInstans();
        return instanceId != null && instanceId > 0 ? instanceId : 0;
    }

    private String getJsonProperty(MedicinsktArende arende, Integer position, Map<String, List<String>> arendeParameters) {
        final var jsonProperties = arendeParameters.get(arende.getFrageId());
        if (CollectionUtils.isEmpty(jsonProperties)) {
            LOG.warn("Cannot find jsonProperties for question '{}'", arende.getFrageId());
            return "";
        }

        return jsonProperties.get(position < jsonProperties.size() ? position : 0);
    }

    private Map<String, List<String>> getJsonPropertiesMap(Utlatande certificateAsUtlatande, List<String> questionIds) {
        try {
            final var moduleApi = getModuleApi(certificateAsUtlatande.getTyp(), certificateAsUtlatande.getTextVersion());
            return moduleApi.getModuleSpecificArendeParameters(certificateAsUtlatande, questionIds);
        } catch (ModuleException ex) {
            throw new WebCertServiceException(
                WebCertServiceErrorCodeEnum.INTERNAL_PROBLEM,
                String.format("Could not retrieve jsonProperties for certificate '%s'", certificateAsUtlatande.getId()),
                ex
            );
        }
    }

    private CertificateTextProvider getCertificateTextProvider(Utlatande certificateAsUtlatande) {
        final var moduleApi = getModuleApi(certificateAsUtlatande.getTyp(), certificateAsUtlatande.getTextVersion());
        return moduleApi.getTextProvider(certificateAsUtlatande.getTyp(), certificateAsUtlatande.getTextVersion());
    }

    private ModuleApi getModuleApi(String certificateType, String certificateVersion) {
        try {
            return intygModuleRegistry.getModuleApi(certificateType, certificateVersion);
        } catch (ModuleNotFoundException ex) {
            throw new WebCertServiceException(
                WebCertServiceErrorCodeEnum.INTERNAL_PROBLEM,
                String.format("Could not find ModuleAPI for certificate type '%s' and version %s", certificateType, certificateVersion),
                ex
            );
        }
    }
}
