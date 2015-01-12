package se.inera.webcert.notifications.service;

import se.inera.webcert.persistence.utkast.model.Utkast;

public interface WebcertRepositoryService {

    Utkast getUtkast(String intygsId);

    String getModelFromUtkast(String intygsId);

    boolean isUtkastPresent(String intygsId);

    boolean isVardenhetIntegrerad(String vardenhetHsaId);

    Long countNbrOfQuestionsForIntyg(String intygsId);

    Long countNbrOfAnsweredQuestionsForIntyg(String intygsId);

    Long countNbrOfHandledQuestionsForIntyg(String intygsId);

    Long countNbrOfHandledAndAnsweredQuestionsForIntyg(String intygsId);

}
