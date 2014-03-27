package se.inera.certificate.mc2wc.batch.writer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.inera.certificate.mc2wc.message.*;
import se.inera.certificate.mc2wc.rest.MigrationReceiver;

import javax.ws.rs.BadRequestException;
import javax.ws.rs.ServerErrorException;
import javax.ws.rs.core.Response.Status;
import java.util.ArrayList;
import java.util.List;

public class MockMigrationRecieverBean implements MigrationReceiver {

    public static final String HTTP_500 = "badCertificateHttp500";
    public static final String HTTP_400 = "badCertificateHttp400";

    public static Logger log = LoggerFactory.getLogger(MockMigrationRecieverBean.class);

    private List<MigrationMessage> messages = new ArrayList<MigrationMessage>();

    public MigrationReply receive(MigrationMessage message) {

        String certificateId = message.getCertificateId();

        log.info("-- Received MigrationMessage for certificate {} --", certificateId);

        messages.add(message);

        if (HTTP_500.equals(certificateId)) {
            throw new ServerErrorException(Status.INTERNAL_SERVER_ERROR);
        } else if (HTTP_400.equals(certificateId)) {
            MigrationReply reply = new MigrationReply();
            reply.setResult(MigrationResultType.ERROR);
            reply.setMessage("Something was bad in the request");
            throw new BadRequestException();
        }

        MigrationReply reply = new MigrationReply();
        reply.setResult(MigrationResultType.OK);
        return reply;
    }

    public PingResponse ping(PingRequest request) {
        return new PingResponse();
    }

    @Override
    public StatisticsResponse getStatistics(StatisticsRequest request) {
        return new StatisticsResponse();
    }

    public List<MigrationMessage> getMessages() {
        return messages;
    }

    public void clearMessages() {
        messages.clear();
    }

}
