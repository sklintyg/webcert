package se.inera.certificate.mc2wc.rest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import se.inera.certificate.mc2wc.message.*;
import se.inera.certificate.mc2wc.service.MigrationService;
import se.inera.certificate.mc2wc.service.MigrationServiceException;

import javax.ws.rs.ServerErrorException;
import javax.ws.rs.core.Response.Status;

public class MigrationReceiverBean implements MigrationReceiver {

    private static Logger logger = LoggerFactory.getLogger(MigrationReceiverBean.class);

    @Autowired
    private MigrationService migrationService;

    public PingResponse ping(PingRequest request) {
        return new PingResponse();
    }

    public MigrationReply receive(MigrationMessage message) {
        try {
            MigrationResultType result = migrationService.processMigrationMessage(message);

            MigrationReply reply = new MigrationReply();
            reply.setResult(result);

            return reply;
        } catch (MigrationServiceException e) {
            logger.error("Could not store certificate with id {}" + message.getCertificateId());
            throw new ServerErrorException(Status.INTERNAL_SERVER_ERROR, e);
        }
    }
}