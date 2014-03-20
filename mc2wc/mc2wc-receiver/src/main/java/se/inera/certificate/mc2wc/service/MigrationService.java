package se.inera.certificate.mc2wc.service;

import se.inera.certificate.mc2wc.message.MigrationMessage;
import se.inera.certificate.mc2wc.message.MigrationResultType;

public interface MigrationService {

    MigrationResultType processMigrationMessage(MigrationMessage message) throws MigrationServiceException;

}
