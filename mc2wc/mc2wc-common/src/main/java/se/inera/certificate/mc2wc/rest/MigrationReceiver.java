package se.inera.certificate.mc2wc.rest;

import se.inera.certificate.mc2wc.message.*;

import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;

@Path("/migration")
public interface MigrationReceiver {

    @POST
    @Path("/receive")
    @Consumes("application/xml")
    @Produces("application/xml")
    public MigrationReply receive(MigrationMessage message);

    @POST
    @Path("/ping")
    @Consumes("application/xml")
    @Produces("application/xml")
    public PingResponse ping(PingRequest request);

    @POST
    @Path("/statistics")
    @Consumes("application/xml")
    @Produces("application/xml")
    public StatisticsResponse getStatistics(StatisticsRequest request);
}
