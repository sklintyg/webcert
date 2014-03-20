package se.inera.certificate.mc2wc.schema;

import se.inera.certificate.mc2wc.message.MigrationMessage;

public interface MigrationMessageSchemaValidator {

    public abstract void validateMigrationMessage(MigrationMessage migrationMessage) throws SchemaValidatorException;

}
