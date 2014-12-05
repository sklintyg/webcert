package se.inera.webcert.pu.services;

import se.inera.webcert.pu.model.PersonSvar;

public interface PUService {

    PersonSvar getPerson(String personId);
}
