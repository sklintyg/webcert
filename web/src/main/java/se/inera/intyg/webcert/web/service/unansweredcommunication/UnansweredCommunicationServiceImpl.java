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

package se.inera.intyg.webcert.web.service.unansweredcommunication;

import static se.inera.intyg.clinicalprocess.healthcond.certificate.getcertificateadditions.v1.StatusType.BESVARAD;
import static se.inera.intyg.clinicalprocess.healthcond.certificate.getcertificateadditions.v1.StatusType.OBESVARAD;
import static se.inera.intyg.webcert.persistence.arende.model.ArendeAmne.KOMPLT;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;
import se.inera.intyg.clinicalprocess.healthcond.certificate.getcertificateadditions.v1.StatusType;
import se.inera.intyg.webcert.persistence.arende.model.Arende;
import se.inera.intyg.webcert.persistence.arende.model.ArendeAmne;
import se.inera.intyg.webcert.persistence.model.Status;
import se.inera.intyg.webcert.web.service.arende.ArendeService;
import se.inera.intyg.webcert.web.service.fragasvar.dto.FrageStallare;
import se.inera.intyg.webcert.web.web.controller.internalapi.dto.UnansweredCommunicationRequest;
import se.inera.intyg.webcert.web.web.controller.internalapi.dto.UnansweredCommunicationResponse;
import se.inera.intyg.webcert.web.web.controller.internalapi.dto.UnansweredQAs;

@Service("getUnansweredCommunicationFromWC")
public class UnansweredCommunicationServiceImpl implements UnansweredCommunicationService {

    private final ArendeService arendeService;

    public UnansweredCommunicationServiceImpl(ArendeService arendeService) {
        this.arendeService = arendeService;
    }

    @Override
    public UnansweredCommunicationResponse get(UnansweredCommunicationRequest request) {
        assertPatientIds(request.getPatientIds());
        assertMaxDaysOfUnansweredCommunication(request.getMaxDaysOfUnansweredCommunication());

        final var arendenForPatients = arendeService.getArendenForPatientsWithTimestampAfterDate(
            formatPatientIds(request.getPatientIds()),
            LocalDateTime.now().minusDays(request.getMaxDaysOfUnansweredCommunication()).toLocalDate().atStartOfDay()
        );

        return new UnansweredCommunicationResponse(createUnansweredCommunication(arendenForPatients));
    }

    private void assertMaxDaysOfUnansweredCommunication(Integer maxDaysOfUnansweredCommunication) {
        if (maxDaysOfUnansweredCommunication == null) {
            throw new IllegalArgumentException("Request is missing parameter maxDaysOfUnansweredCommunication");
        }
    }

    private void assertPatientIds(List<String> patientIds) {
        if (patientIds == null || patientIds.isEmpty()) {
            throw new IllegalArgumentException("Request is missing parameter patientIds or is empty");
        }
    }

    private static List<String> formatPatientIds(List<String> patientIds) {
        return patientIds.stream()
            .map(patientId -> patientId.replace("-", ""))
            .collect(Collectors.toList());
    }

    private Map<String, UnansweredQAs> createUnansweredCommunication(List<Arende> arenden) {
        return arenden.stream()
            .filter(this::isUnansweredCommunication)
            .collect(
                Collectors.toMap(
                    Arende::getIntygsId,
                    this::createUnansweredQAs,
                    UnansweredQAs::add
                )
            );
    }

    private UnansweredQAs createUnansweredQAs(Arende arende) {
        final var unansweredQAs = new UnansweredQAs();
        if (KOMPLT.equals(arende.getAmne())) {
            unansweredQAs.incrementComplement();
        } else {
            unansweredQAs.incrementOther();
        }
        return unansweredQAs;
    }

    private boolean isUnansweredCommunication(Arende arende) {
        return status(arende) == OBESVARAD && arende.getAmne() != ArendeAmne.PAMINN
            && arende.getSkickatAv().equals(FrageStallare.FORSAKRINGSKASSAN.getKod());
    }

    private StatusType status(Arende arende) {
        if (isAnsweredFromExternal(arende.getStatus(), arende.getSvarPaId())) {
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

    private static boolean isAnsweredFromExternal(Status status, String svarPaId) {
        return status == Status.ANSWERED && svarPaId != null && !svarPaId.isEmpty();
    }
}
