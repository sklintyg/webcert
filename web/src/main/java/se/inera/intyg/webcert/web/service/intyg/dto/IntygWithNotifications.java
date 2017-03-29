package se.inera.intyg.webcert.web.service.intyg.dto;

import java.util.List;

import se.inera.intyg.common.support.modules.support.api.notification.ArendeCount;
import se.inera.intyg.webcert.persistence.handelse.model.Handelse;
import se.riv.clinicalprocess.healthcond.certificate.v3.Intyg;

public class IntygWithNotifications {
    private final Intyg intyg;
    private final List<Handelse> notifications;
    private final ArendeCount sentQuestions;
    private final ArendeCount receivedQuestions;

    public IntygWithNotifications(Intyg intyg, List<Handelse> notifications,
            ArendeCount sentQuestions, ArendeCount receivedQuestions) {
        this.intyg = intyg;
        this.notifications = notifications;
        this.sentQuestions = sentQuestions;
        this.receivedQuestions = receivedQuestions;
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
}
