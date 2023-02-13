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
package se.inera.intyg.webcert.web.service.facade.question.impl;

import java.util.Collections;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import se.inera.intyg.common.support.facade.model.question.Question;
import se.inera.intyg.infra.integration.hsatk.services.legacy.HsaOrganizationsService;
import se.inera.intyg.schemas.contract.Personnummer;
import se.inera.intyg.webcert.web.service.access.AccessEvaluationParameters;
import se.inera.intyg.webcert.web.service.access.CertificateAccessServiceHelper;
import se.inera.intyg.webcert.web.service.arende.ArendeService;
import se.inera.intyg.webcert.web.service.facade.question.GetQuestionsAvailableFunctionsService;
import se.inera.intyg.webcert.web.service.facade.question.GetQuestionsResourceLinkService;
import se.inera.intyg.webcert.web.service.fragasvar.FragaSvarService;
import se.inera.intyg.webcert.web.util.UtkastUtil;
import se.inera.intyg.webcert.web.web.controller.facade.dto.ResourceLinkDTO;
import se.inera.intyg.webcert.web.web.controller.facade.dto.ResourceLinkTypeDTO;

@Service
public class GetQuestionsResourceLinkServiceImpl implements GetQuestionsResourceLinkService {

    private final GetQuestionsAvailableFunctionsService getQuestionsAvailableFunctionsService;
    private final CertificateAccessServiceHelper certificateAccessServiceHelper;
    private final ArendeService arendeService;
    private final HsaOrganizationsService hsaOrganizationsService;
    private final FragaSvarService fragaSvarService;

    @Autowired
    public GetQuestionsResourceLinkServiceImpl(GetQuestionsAvailableFunctionsService getQuestionsAvailableFunctionsService,
        CertificateAccessServiceHelper certificateAccessServiceHelper,
        ArendeService arendeService, HsaOrganizationsService hsaOrganizationsService, FragaSvarService fragaSvarService) {
        this.getQuestionsAvailableFunctionsService = getQuestionsAvailableFunctionsService;
        this.certificateAccessServiceHelper = certificateAccessServiceHelper;
        this.arendeService = arendeService;
        this.hsaOrganizationsService = hsaOrganizationsService;
        this.fragaSvarService = fragaSvarService;
    }

    @Override
    public List<ResourceLinkDTO> get(Question question) {
        if (question.getSent() == null) {
            return Collections.emptyList();
        }

        final var accessEvaluationParameters = createAccessEvaluationParameters(question);
        final var availableFunctions = getQuestionsAvailableFunctionsService.get(question);
        final var functions = getAccessFunctions();
        return availableFunctions.stream()
            .filter(availableFunction -> {
                final var accessFunction = functions.get(availableFunction.getType());
                if (accessFunction == null) {
                    return true;
                }
                return accessFunction.hasAccess(accessEvaluationParameters);
            })
            .collect(Collectors.toList());
    }

    @Override
    public Map<Question, List<ResourceLinkDTO>> get(List<Question> questions) {
        final var questionResourceLinkDTOHashMap = new HashMap<Question, List<ResourceLinkDTO>>();
        for (Question question : questions) {
            final var resourceLinkDTOS = get(question);
            questionResourceLinkDTOHashMap.put(question, resourceLinkDTOS);
        }
        return questionResourceLinkDTOHashMap;
    }

    private AccessEvaluationParameters createAccessEvaluationParameters(Question question) {
        final var arende = arendeService.getArende(question.getId());
        if (arende == null) {
            final var fragaSvar = fragaSvarService.getFragaSvarById(Long.parseLong(question.getId()));
            return getAccessEvaluationParameters(
                fragaSvar.getVardperson().getEnhetsId(),
                fragaSvar.getIntygsReferens().getIntygsTyp(),
                fragaSvar.getIntygsReferens().getPatientId()
            );
        }
        return getAccessEvaluationParameters(
            arende.getEnhetId(),
            arende.getIntygTyp(),
            Personnummer.createPersonnummer(arende.getPatientPersonId()).orElseThrow()
        );
    }

    private AccessEvaluationParameters getAccessEvaluationParameters(String unitId, String certificateType, Personnummer patientId) {
        final var careProviderId = hsaOrganizationsService.getVardgivareOfVardenhet(unitId);
        return AccessEvaluationParameters.create(
            certificateType,
            null,
            UtkastUtil.getCareUnit(careProviderId, unitId),
            patientId,
            false);
    }

    private Map<ResourceLinkTypeDTO, GetQuestionsResourceLinkServiceImpl.AccessCheck> getAccessFunctions() {
        final var functions = new EnumMap<ResourceLinkTypeDTO, AccessCheck>(ResourceLinkTypeDTO.class);

        functions.put(ResourceLinkTypeDTO.ANSWER_QUESTION,
            certificateAccessServiceHelper::isAllowToAnswerAdminQuestion
        );

        functions.put(ResourceLinkTypeDTO.HANDLE_QUESTION,
            certificateAccessServiceHelper::isAllowToSetQuestionAsHandled
        );

        functions.put(ResourceLinkTypeDTO.COMPLEMENT_CERTIFICATE,
            accessEvaluationParameters -> certificateAccessServiceHelper
                .isAllowToAnswerComplementQuestion(accessEvaluationParameters, true)
        );

        functions.put(ResourceLinkTypeDTO.CANNOT_COMPLEMENT_CERTIFICATE,
            accessEvaluationParameters -> certificateAccessServiceHelper
                .isAllowToAnswerComplementQuestion(accessEvaluationParameters, true)
        );
        return functions;
    }

    private interface AccessCheck {

        boolean hasAccess(AccessEvaluationParameters accessEvaluationParameters);
    }
}
