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
import java.util.stream.Collectors;
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
    private static final String FK = "FK";

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
            formatPatientIds(patientIds),
            LocalDateTime.now().minusDays(maxDaysOfUnansweredCommunication)
        );

        if (arendenForPatients.isEmpty()) {
            return new UnansweredCommunicationResponse(Collections.emptyMap());
        }

        return new UnansweredCommunicationResponse(createUnansweredCommunication(arendenForPatients));
    }

    private static List<String> formatPatientIds(List<String> patientIds) {
        return patientIds.stream()
            .map(patientId -> patientId.replace("-", ""))
            .collect(Collectors.toList());
    }

    private Map<String, UnansweredQAs> createUnansweredCommunication(List<Arende> arenden) {
        final var unansweredCommunication = new HashMap<String, UnansweredQAs>();
        arenden.forEach(arende -> getUnansweredCommunication(arende, unansweredCommunication));
        return unansweredCommunication;
    }

    private void getUnansweredCommunication(Arende arende, Map<String, UnansweredQAs> unansweredCommunication) {
        final var status = convertStatus(arende);
        if (isNotUnansweredCommunication(arende, status)) {
            return;
        }
        final var unansweredQAs = getUnansweredQA(arende, unansweredCommunication);
        if (KOMPLT.equals(arende.getAmne())) {
            unansweredQAs.incrementComplement();
        } else {
            unansweredQAs.incrementOther();
        }
        unansweredCommunication.put(arende.getIntygsId(), unansweredQAs);
    }

    private static boolean isNotUnansweredCommunication(Arende arende, StatusType status) {
        return !OBESVARAD.equals(status) || arende.getAmne().equals(ArendeAmne.PAMINN) || !arende.getSkickatAv().equals(FK);
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
