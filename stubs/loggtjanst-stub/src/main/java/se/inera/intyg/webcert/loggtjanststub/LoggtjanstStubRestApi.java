package se.inera.intyg.webcert.loggtjanststub;

import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.Collection;
import java.util.concurrent.CopyOnWriteArrayList;

import org.springframework.beans.factory.annotation.Autowired;
import se.riv.ehr.log.v1.LogType;

/**
 * @author andreaskaltenbach
 */
public class LoggtjanstStubRestApi {

    @Autowired
    private CopyOnWriteArrayList<LogType> logEntries;

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Collection<LogType> getAllLogEntries() {
        return logEntries;
    }

    @DELETE
    public Response deleteMedarbetaruppdrag() {
        logEntries.clear();
        return Response.ok().build();
    }
}
