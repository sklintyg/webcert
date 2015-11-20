package se.inera.intyg.webcert.web.integration.registry;

import se.inera.intyg.webcert.web.integration.registry.dto.IntegreradEnhetEntry;

public interface IntegreradeEnheterRegistry {

    boolean addIfNotExistsIntegreradEnhet(IntegreradEnhetEntry entry);

    boolean isEnhetIntegrerad(String enhetHsaId);

    void addIfSameVardgivareButDifferentUnits(String orgEnhetsHsaId, IntegreradEnhetEntry newEntry);
}
