package se.inera.intyg.webcert.web.service.utkast.util;

import java.util.UUID;

import org.springframework.stereotype.Component;

/**
 * Strategy implementation that generates a Intyg id a a UUID.
 *
 * @author nikpet
 */
@Component
public class CreateIntygsIdAsUUIDStrategy implements CreateIntygsIdStrategy {

    public CreateIntygsIdAsUUIDStrategy() {

    }

    @Override
    public String createId() {
        return UUID.randomUUID().toString();
    }

}
