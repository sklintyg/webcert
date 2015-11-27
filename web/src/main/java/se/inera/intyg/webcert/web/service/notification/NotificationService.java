package se.inera.intyg.webcert.web.service.notification;

import se.inera.intyg.webcert.persistence.fragasvar.model.FragaSvar;
import se.inera.intyg.webcert.persistence.utkast.model.Utkast;

/**
 * Service that notifies a unit care of incoming changes.
 *
 * @author Magnus Ekstrand
 */
public interface NotificationService {

    /**
     * Utkast created (HAN1).
     */
    void sendNotificationForDraftCreated(Utkast utkast);

    /**
     * Utkast signed (HAN2).
     */
    void sendNotificationForDraftSigned(Utkast utkast);

    /**
     * Utkast changed (HAN11).
     */
    void sendNotificationForDraftChanged(Utkast utkast);

    /**
     * Utkast deleted (HAN4).
     */
    void sendNotificationForDraftDeleted(Utkast utkast);

    /**
     * Signed intyg sent to recipient (HAN3).
     */
    void sendNotificationForIntygSent(String intygsId);

    /**
     * Signed intyg revoked (HAN5).
     */
    void sendNotificationForIntygRevoked(String intygsId);

    /**
     * New question received from FK (HAN6).
     */
    void sendNotificationForQuestionReceived(FragaSvar fragaSvar);

    /**
     * Question from FK handled (HAN9).
     */
    void sendNotificationForQuestionHandled(FragaSvar fragaSvar);

    /**
     * New question sent to FK (HAN8).
     */
    void sendNotificationForQuestionSent(FragaSvar fragaSvar);

    /**
     * New answer received from FK (HAN7).
     */
    void sendNotificationForAnswerRecieved(FragaSvar fragaSvar);

    /**
     * Answer from FK handled (HAN10).
     */
    void sendNotificationForAnswerHandled(FragaSvar fragaSvar);

}
