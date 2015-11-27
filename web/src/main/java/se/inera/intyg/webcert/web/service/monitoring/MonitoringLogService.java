/**
 * Copyright (C) 2015 Inera AB (http://www.inera.se)
 *
 * This file is part of statistik (https://github.com/sklintyg/statistik).
 *
 * statistik is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * statistik is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package se.inera.intyg.webcert.web.service.monitoring;

import se.inera.intyg.common.support.modules.support.api.dto.Personnummer;

/**
 * Service that writes messages to the monitoring log.
 *
 * @author npet
 *
 */
public interface MonitoringLogService {

    void logMailSent(String unitHsaId, String reason);

    void logMailMissingAddress(String unitHsaId, String reason);

    void logUserLogin(String userHsaId, String authScheme);

    void logUserLogout(String userHsaId, String authScheme);

    void logUserSessionExpired(String userHsaId, String authScheme);

    void logMissingMedarbetarUppdrag(String userHsaId);

    void logMissingMedarbetarUppdrag(String userHsaId, String enhetsId);

    void logQuestionReceived(String fragestallare, String intygsId, String externReferens, Long internReferens, String enhet, String amne);

    void logAnswerReceived(String externReferens, Long internReferens, String intygsId, String enhet, String amne);

    void logQuestionSent(String externReferens, Long internReferens, String intygsId, String enhet, String amne);

    void logAnswerSent(String externReferens, Long internReferens, String intygsId, String enhet, String amne);

    void logIntygRead(String intygsId, String intygsTyp);

    void logIntygPrintPdf(String intygsId, String intygsTyp);

    void logIntygSigned(String intygsId, String userHsaId, String authScheme);

    void logIntygRegistered(String intygsId, String intygsTyp);

    void logIntygSent(String intygsId, String recipient);

    void logIntygRevoked(String intygsId, String hsaId);

    void logIntygCopied(String copyIntygsId, String originalIntygId);

    void logUtkastCreated(String intygsId, String intygsTyp, String unitHsaId, String userHsaId);

    void logUtkastEdited(String intygsId, String intygsTyp);

    void logUtkastConcurrentlyEdited(String intygsId, String intygsTyp);

    void logUtkastDeleted(String intygsId, String intygsTyp);

    void logUtkastRead(String intygsId, String intygsTyp);

    void logUtkastPrint(String intygsId, String intygsTyp);

    void logPULookup(Personnummer personNummer, String result);

    void logPrivatePractitionerTermsApproved(String userId, String personId, Integer avtalVersion);

    void logNotificationSent(String hanType, String unitId);
}
