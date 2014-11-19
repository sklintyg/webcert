package se.inera.webcert.integration.registry;

import org.springframework.transaction.annotation.Transactional;

import se.inera.webcert.integration.registry.dto.IntegreradEnhetEntry;

public interface IntegreradeEnheterRegistry {

    public abstract boolean addIfNotExistsIntegreradEnhet(IntegreradEnhetEntry entry);

    public abstract boolean isEnhetIntegrerad(String enhetHsaId);

}
