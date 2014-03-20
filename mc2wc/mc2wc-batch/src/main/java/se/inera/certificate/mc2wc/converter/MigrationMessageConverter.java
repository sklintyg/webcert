package se.inera.certificate.mc2wc.converter;

import se.inera.certificate.mc2wc.jpa.model.Certificate;
import se.inera.certificate.mc2wc.message.MigrationMessage;

public interface MigrationMessageConverter {

    public abstract MigrationMessage toMigrationMessage(Certificate mcCert, boolean migrateCert);

}
