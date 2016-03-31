/*
 * Copyright (C) 2016 Inera AB (http://www.inera.se)
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

package se.inera.intyg.webcert.web.service.notification;

import se.inera.intyg.webcert.persistence.arende.model.Arende;
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

    /**
     * New question received from FK (HAN6).
     */
    void sendNotificationForQuestionReceived(Arende arende);

    /**
     * Question from FK handled (HAN9).
     */
    void sendNotificationForQuestionHandled(Arende arende);

    /**
     * New question sent to FK (HAN8).
     */
    void sendNotificationForQuestionSent(Arende arende);

    /**
     * New answer received from FK (HAN7).
     */
    void sendNotificationForAnswerRecieved(Arende arende);

    /**
     * Answer from FK handled (HAN10).
     */
    void sendNotificationForAnswerHandled(Arende arende);
}
