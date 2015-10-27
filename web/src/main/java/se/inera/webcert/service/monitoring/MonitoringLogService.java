package se.inera.webcert.service.monitoring;

import se.inera.certificate.modules.support.api.dto.Personnummer;

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
