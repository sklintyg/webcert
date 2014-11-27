package se.inera.webcert.notifications.service;

import se.inera.webcert.persistence.intyg.model.Intyg;

public interface IntygRepositoryService {

    public abstract Intyg getIntygsUtkast(String intygsId);

}
