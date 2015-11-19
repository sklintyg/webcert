package se.inera.webcert.integration.registry;

import se.inera.webcert.integration.registry.dto.IntegreradEnhetEntry;

public interface IntegreradeEnheterRegistry {

    boolean addIfNotExistsIntegreradEnhet(IntegreradEnhetEntry entry);

    boolean isEnhetIntegrerad(String enhetHsaId);

    void addIfSameVardgivareButDifferentUnits(String orgEnhetsHsaId, IntegreradEnhetEntry newEntry);
}
