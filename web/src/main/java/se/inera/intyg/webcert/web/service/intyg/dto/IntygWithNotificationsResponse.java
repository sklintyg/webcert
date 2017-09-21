/*
 * Copyright (C) 2017 Inera AB (http://www.inera.se)
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
package se.inera.intyg.webcert.web.service.intyg.dto;

import java.util.ArrayList;
import java.util.List;

import se.inera.intyg.common.support.modules.support.api.notification.ArendeCount;
import se.inera.intyg.webcert.persistence.handelse.model.Handelse;
import se.riv.clinicalprocess.healthcond.certificate.v3.Intyg;

public class IntygWithNotificationsResponse {
    private final Intyg intyg;
    private final List<Handelse> notifications;
    private final ArendeCount sentQuestions;
    private final ArendeCount receivedQuestions;
    private final String ref;

    public IntygWithNotificationsResponse(Intyg intyg, List<Handelse> notifications,
            ArendeCount sentQuestions, ArendeCount receivedQuestions, String ref) {
        this.intyg = intyg;
        if (notifications == null) {
            this.notifications = new ArrayList<>();
        } else {
            this.notifications = notifications;
        }
        this.sentQuestions = sentQuestions;
        this.receivedQuestions = receivedQuestions;
        this.ref = ref;
    }

    public Intyg getIntyg() {
        return intyg;
    }

    public List<Handelse> getNotifications() {
        return notifications;
    }

    public ArendeCount getSentQuestions() {
        return sentQuestions;
    }

    public ArendeCount getReceivedQuestions() {
        return receivedQuestions;
    }

    public String getRef() {
        return ref;
    }
}
