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

package se.inera.intyg.webcert.web.service.unansweredcommunication;

import static se.inera.intyg.clinicalprocess.healthcond.certificate.getcertificateadditions.v1.StatusType.BESVARAD;
import static se.inera.intyg.clinicalprocess.healthcond.certificate.getcertificateadditions.v1.StatusType.OBESVARAD;
import static se.inera.intyg.webcert.persistence.arende.model.ArendeAmne.KOMPLT;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Service;
import se.inera.intyg.clinicalprocess.healthcond.certificate.getcertificateadditions.v1.StatusType;
import se.inera.intyg.webcert.persistence.arende.model.Arende;
import se.inera.intyg.webcert.persistence.arende.model.ArendeAmne;
import se.inera.intyg.webcert.persistence.model.Status;
import se.inera.intyg.webcert.web.service.arende.ArendeService;
import se.inera.intyg.webcert.web.web.controller.internalapi.dto.UnansweredCommunicationRequest;
import se.inera.intyg.webcert.web.web.controller.internalapi.dto.UnansweredCommunicationResponse;
import se.inera.intyg.webcert.web.web.controller.internalapi.dto.UnansweredQAs;

@Service
public class UnansweredCommunicationServiceImpl implements UnansweredCommunicationService {

    private final ArendeService arendeService;

    public UnansweredCommunicationServiceImpl(ArendeService arendeService) {
        this.arendeService = arendeService;
    }

    @Override
    public UnansweredCommunicationResponse get(UnansweredCommunicationRequest request) {
        final var maxDaysOfUnansweredCommunication = request.getMaxDaysOfUnansweredCommunication();
        final var patientIds = request.getPatientIds();

        if (patientIds == null || patientIds.isEmpty() || maxDaysOfUnansweredCommunication == null) {
            throw new IllegalArgumentException(String.format(
                "Request is missing parameters required to make request. patientIds '%s' maxDaysOfUnansweredCommunication '%s'",
                patientIds,
                maxDaysOfUnansweredCommunication));
        }

        final var arendenForPatients = arendeService.getArendenForPatientsWithTimestampAfterDate(
            patientIds,
            LocalDateTime.now().minusDays(maxDaysOfUnansweredCommunication)
        );

        if (arendenForPatients.isEmpty()) {
            return new UnansweredCommunicationResponse(Collections.emptyMap());
        }

        return new UnansweredCommunicationResponse(getUnansweredCommunicationResponse(arendenForPatients));
    }

    private Map<String, UnansweredQAs> getUnansweredCommunicationResponse(List<Arende> arenden) {
        final var unansweredCommunicationResponse = new HashMap<String, UnansweredQAs>();
        arenden.forEach(arende -> getUnansweredQAs(arende, unansweredCommunicationResponse));
        return unansweredCommunicationResponse;
    }

    private void getUnansweredQAs(Arende arende, Map<String, UnansweredQAs> unansweredQAsMap) {
        final var status = convertStatus(arende);
        if (isNotUnansweredQA(arende, status)) {
            return;
        }
        final var unansweredQAs = getUnansweredQA(arende, unansweredQAsMap);
        if (KOMPLT.equals(arende.getAmne())) {
            unansweredQAs.incrementComplement();
        } else {
            unansweredQAs.incrementOther();
        }
        unansweredQAsMap.put(arende.getIntygsId(), unansweredQAs);
    }

    private static boolean isNotUnansweredQA(Arende arende, StatusType status) {
        return !OBESVARAD.equals(status) || ArendeAmne.PAMINN.equals(arende.getAmne());
    }

    private StatusType convertStatus(Arende arende) {
        if (isAnswerFromExternal(arende.getStatus(), arende.getSvarPaId())) {
            return BESVARAD;
        }
        switch (arende.getStatus()) {
            case CLOSED:
            case PENDING_EXTERNAL_ACTION:
                return BESVARAD;
            default:
                return OBESVARAD;
        }
    }

    private static boolean isAnswerFromExternal(Status status, String svarPaId) {
        return status == Status.ANSWERED && svarPaId != null && !svarPaId.isEmpty();
    }

    private static UnansweredQAs getUnansweredQA(Arende arende, Map<String, UnansweredQAs> unansweredQAsMap) {
        return unansweredQAsMap.containsKey(arende.getIntygsId()) ? unansweredQAsMap.get(arende.getIntygsId())
            : new UnansweredQAs(0, 0);
    }
}
