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

package se.inera.intyg.webcert.web.csintegration.message;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import se.inera.intyg.common.support.facade.model.Certificate;
import se.inera.intyg.webcert.persistence.arende.model.ArendeAmne;
import se.inera.intyg.webcert.web.service.mail.MailNotification;
import se.inera.intyg.webcert.web.service.mail.MailNotificationService;
import se.riv.clinicalprocess.healthcond.certificate.sendMessageToCare.v2.SendMessageToCareType;

@Slf4j
@Service
@RequiredArgsConstructor
public class SendMailNotificationForReceivedMessageService {

    private final MailNotificationService mailNotificationService;

    public void send(SendMessageToCareType sendMessageToCare, Certificate certificate) {
        final var questionType = ArendeAmne.valueOf(sendMessageToCare.getAmne().getCode());
        final var isAnswer = sendMessageToCare.getSvarPa() != null;
        if (questionType.equals(ArendeAmne.PAMINN) || !isAnswer) {
            mailNotificationService.sendMailForIncomingQuestion(
                buildMailNotification(sendMessageToCare, certificate)
            );
        } else {
            mailNotificationService.sendMailForIncomingAnswer(
                buildMailNotification(sendMessageToCare, certificate)
            );
        }
    }

    private static MailNotification buildMailNotification(SendMessageToCareType sendMessageToCare, Certificate certificate) {
        return new MailNotification(
            sendMessageToCare.getMeddelandeId(),
            certificate.getMetadata().getId(),
            certificate.getMetadata().getType(),
            certificate.getMetadata().getUnit().getUnitId(),
            certificate.getMetadata().getUnit().getUnitName(),
            certificate.getMetadata().getIssuedBy().getPersonId()
        );
    }
}
