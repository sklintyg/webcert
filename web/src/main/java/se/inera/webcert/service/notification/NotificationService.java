package se.inera.webcert.service.notification;

import se.inera.webcert.persistence.fragasvar.model.FragaSvar;
import se.inera.webcert.persistence.utkast.model.Utkast;

/**
 * Service that notifies a unit care of incoming changes.
 *
 * @author Magnus Ekstrand
 */
public interface NotificationService {

    /**
     * Utkast created (HAN1).
     *
     * @param utkast
     * @return
     */
    void sendNotificationForDraftCreated(Utkast utkast);

    /**
     * Utkast signed (HAN2).
     *
     * @param utkast
     * @return
     */
    void sendNotificationForDraftSigned(Utkast utkast);

    /**
     * Utkast changed (HAN11).
     *
     * @param utkast
     * @return
     */
    void sendNotificationForDraftChanged(Utkast utkast);

    /**
     * Utkast deleted (HAN4).
     *
     * @param utkast
     * @return
     */
    void sendNotificationForDraftDeleted(Utkast utkast);

    /**
     * Signed intyg sent to recipient (HAN3).
     *
     * @param utkast
     * @return
     */
    void sendNotificationForIntygSent(String intygsId);

    /**
     * Signed intyg revoked (HAN5).
     *
     * @param utkast
     * @return
     */
    void sendNotificationForIntygRevoked(String intygsId);

    /**
     * New question received from FK (HAN6).
     *
     * @param fragaSvar
     */
    void sendNotificationForQuestionReceived(FragaSvar fragaSvar);

    /**
     * Question from FK handled (HAN9).
     *
     * @param fragaSvar
     */
    void sendNotificationForQuestionHandled(FragaSvar fragaSvar);

    /**
     * New question sent to FK (HAN8).
     *
     * @param fragaSvar
     */
    void sendNotificationForQuestionSent(FragaSvar fragaSvar);

    /**
     * New answer received from FK (HAN7).
     *
     * @param fragaSvar
     */
    void sendNotificationForAnswerRecieved(FragaSvar fragaSvar);

    /**
     * Answer from FK handled (HAN10).
     *
     * @param fragaSvar
     */
    void sendNotificationForAnswerHandled(FragaSvar fragaSvar);

}
